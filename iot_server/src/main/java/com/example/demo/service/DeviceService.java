package com.example.demo.service;

import com.example.demo.dto.DeviceDetailDTO;
import com.example.demo.dto.DeviceListDTO;
import com.example.demo.dto.DeviceStatistics;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.SensorDTO;
import com.example.demo.entity.Device;
import com.example.demo.entity.DeviceWarning;
import com.example.demo.entity.FrequencyMotor;
import com.example.demo.entity.MotorFan;
import com.example.demo.entity.Sensor;
import com.example.demo.enums.PlatformType;
import com.example.demo.mapper.DeviceWarningMapper;
import com.example.demo.mapper.DeviceMapper;
import com.example.demo.mapper.FrequencyMotorMapper;
import com.example.demo.mapper.MotorFanMapper;
import com.example.demo.mapper.SensorMapper;
import com.example.demo.mapper.SensorDataMapper;
import com.example.demo.util.DtoConverter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceMapper deviceMapper;
    private final SensorMapper sensorMapper;
    private final MotorFanMapper motorFanMapper;
    private final FrequencyMotorMapper frequencyMotorMapper;
    private final DeviceWarningMapper deviceWarningMapper;
    private final SensorDataMapper sensorDataMapper;
    private final DtoConverter dtoConverter;

    /**
     * 获取设备列表（分页）
     *
     * @param userId 用户ID，为null时查询所有设备
     */
    public PageResult<Device> getDeviceList(Long userId, Integer pageNum, Integer pageSize,
            String search, Integer deviceType) {
        Map<String, Object> params = new HashMap<>();
        // userId为null时不添加过滤条件，查询所有设备
        if (userId != null) {
            params.put("userId", userId);
        }
        params.put("search", search);
        params.put("deviceType", deviceType);

        // 分页参数
        if (pageNum != null && pageSize != null) {
            int offset = (pageNum - 1) * pageSize;
            params.put("offset", offset);
            params.put("pageSize", pageSize);
        }

        List<Device> list = deviceMapper.findList(params);
        Long total = deviceMapper.countDevice(params);

        return PageResult.of(list, total, pageNum, pageSize);
    }

    /**
     * 获取设备统计
     *
     * @param userId 用户ID，为null时统计所有设备
     */
    public DeviceStatistics getStatistics(Long userId) {
        Long totalDevices;
        Long onlineDevices;
        Long alarmDevices;

        if (userId == null) {
            // web端：查询所有设备的统计
            totalDevices = deviceMapper.countAll();
            onlineDevices = deviceMapper.countOnline();
            alarmDevices = deviceMapper.countWarning();
        } else {
            // mobile端：只查询当前用户的设备
            totalDevices = deviceMapper.countAllByUserId(userId);
            onlineDevices = deviceMapper.countOnlineByUserId(userId);

            // 按“每个设备最近一条报警记录”判断设备是否报警
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            List<Device> userDevices = deviceMapper.findList(params);

            long warningDeviceCount = userDevices.stream()
                    .filter(device -> StringUtils.hasText(device.getDeviceNum()))
                    .filter(device -> {
                        DeviceWarning latestWarning = deviceWarningMapper
                                .findLatestByDeviceNumAndUserId(device.getDeviceNum(), userId);
                        return isDeviceAlarmedByLatestWarning(latestWarning);
                    })
                    .count();
            alarmDevices = warningDeviceCount;
        }

        // 计算离线设备数
        Long offlineDevices = totalDevices - onlineDevices;

        return new DeviceStatistics(totalDevices, onlineDevices, offlineDevices, alarmDevices);
    }

    private boolean isDeviceAlarmedByLatestWarning(DeviceWarning warning) {
        if (warning == null) {
            return false;
        }
        return isAlarm(warning.getTa1())
                || isAlarm(warning.getTa2())
                || isAlarm(warning.getTa3())
                || isAlarm(warning.getTa4())
                || isAlarm(warning.getHa())
                || isAlarm(warning.getNa());
    }

    private boolean isAlarm(Integer alarmFlag) {
        return alarmFlag != null && alarmFlag != 0;
    }

    /**
     * 获取设备详情（返回 Entity）
     */
    public Device getDeviceDetail(Long deviceId) {
        Device device = deviceMapper.findById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        return device;
    }

    /**
     * 获取设备详情 DTO（包含所有关联数据）
     */
    public DeviceDetailDTO getDeviceDetailDTO(Long deviceId) {
        Device device = getDeviceDetail(deviceId);
        List<Sensor> sensors = sensorMapper.findByDeviceId(deviceId);
        List<MotorFan> motorFans = motorFanMapper.findByParentId(deviceId);
        List<FrequencyMotor> frequencyMotors = frequencyMotorMapper.findByParentId(deviceId);

        return dtoConverter.toDeviceDetailDTO(device, sensors, motorFans, frequencyMotors);
    }

    /**
     * 获取设备列表 DTO（批量查询传感器）
     */
    public PageResult<DeviceListDTO> getDeviceListDTO(Long userId, Integer pageNum, Integer pageSize,
            String deviceName, String deviceNum,
            String userName, String userPhone,
            Integer warningStatus) {
        Map<String, Object> params = new HashMap<>();
        // userId为null时不添加过滤条件，查询所有设备
        if (userId != null) {
            params.put("userId", userId);
        }
        params.put("deviceName", deviceName);
        params.put("deviceNum", deviceNum);
        params.put("userName", userName);
        params.put("userPhone", userPhone);
        params.put("warningStatus", warningStatus);

        // 分页参数
        if (pageNum != null && pageSize != null) {
            int offset = (pageNum - 1) * pageSize;
            params.put("offset", offset);
            params.put("pageSize", pageSize);
        }

        List<DeviceListDTO> list = deviceMapper.findListWithUser(params);
        Long total = deviceMapper.countDeviceWithUser(params);

        // 批量查询传感器
        list.forEach(device -> {
            List<Sensor> sensors = sensorMapper.findByDeviceId(device.getId());
            List<SensorDTO> sensorDTOs = sensors.stream()
                    .map(sensor -> dtoConverter.toSensorDTO(sensor))
                    .collect(Collectors.toList());
            device.setSensors(sensorDTOs);
        });

        return PageResult.of(list, total, pageNum, pageSize);
    }

    /**
     * 绑定设备
     */
    @Transactional
    public void bindDevice(Long userId, String deviceNum, String deviceName, Integer deviceType) {
        // 检查设备是否已被绑定
        Device existDevice = deviceMapper.findByDeviceNum(deviceNum);
        if (existDevice != null) {
            throw new RuntimeException("设备已被其他用户绑定");
        }

        // 创建新设备
        Device device = new Device();
        device.setUserId(userId); // userId 可以为 null
        device.setDeviceNum(deviceNum);
        device.setDeviceName(deviceName);
        device.setDeviceType(deviceType); // 默认类型
        device.setDeviceLineState(1); // 默认离线

        deviceMapper.insert(device);
    }

    /**
     * 解绑设备
     */
    @Transactional
    public void unbindDevice(Long deviceId, Long userId) {
        Device device = deviceMapper.findById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        // 验证设备归属
        if (!Objects.equals(device.getUserId(), userId)) {
            throw new RuntimeException("无权操作此设备");
        }

        deviceMapper.deleteById(deviceId);
    }

    /**
     * 更新设备设置
     */
    @Transactional
    public void updateDeviceSettings(Long deviceId, Long userId, String platform, Device device) {
        Device existDevice = deviceMapper.findById(deviceId);
        if (existDevice == null) {
            throw new RuntimeException("设备不存在");
        }

        // 验证设备归属（web 端管理后台跳过权限验证）
        if (!PlatformType.WEB.getValue().equals(platform) && !Objects.equals(existDevice.getUserId(), userId)) {
            throw new RuntimeException("无权操作此设备");
        }

        // 设置设备ID
        device.setId(deviceId);

        // 执行更新
        int rows = deviceMapper.update(device);
        if (rows == 0) {
            throw new RuntimeException("更新失败");
        }
    }

    /**
     * 删除设备（级联删除所有关联数据）
     */
    @Transactional
    public void deleteDevice(Long deviceId, Long userId, String platform) {
        Device device = deviceMapper.findById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        // 验证设备归属（web 端管理后台跳过权限验证）
        if (!"web".equals(platform) && !Objects.equals(device.getUserId(), userId)) {
            throw new RuntimeException("无权操作此设备");
        }

        // 1. 删除传感器历史数据
        sensorDataMapper.deleteByDeviceId(deviceId);

        // 2. 删除传感器
        sensorMapper.deleteByDeviceId(deviceId);

        // 3. 删除风机
        motorFanMapper.deleteByParentId(deviceId);

        // 4. 删除变频电机
        frequencyMotorMapper.deleteByParentId(deviceId);

        // 5. 删除设备本身
        deviceMapper.deleteById(deviceId);
    }

    public void updateDeviceOnlineState(String deviceNum, int state) {
        deviceMapper.updateDeviceOnlineState(deviceNum, state);
    }

    public void updateDevice(String deviceNum, String imei, String iccid,
            Integer deviceLineState, Integer signal, Integer power) {
        if (!StringUtils.hasText(deviceNum)) {
            return;
        }
        Device update = new Device();
        update.setDeviceNum(deviceNum);
        update.setImei(imei);
        update.setIccid(iccid);
        update.setDeviceLineState(deviceLineState);
        update.setSignal(signal);
        update.setPower(power);
        deviceMapper.updateDeviceByDeviceNum(update);
    }

    public void updateDeviceIdentity(String deviceNum, String imei, String iccid) {
        if (!StringUtils.hasText(deviceNum)) {
            return;
        }
        if (!StringUtils.hasText(imei) && !StringUtils.hasText(iccid)) {
            return;
        }
        deviceMapper.updateDeviceIdentity(deviceNum, imei, iccid);
    }

    public Device findByDeviceNum(String deviceNum) {
        return deviceMapper.findByDeviceNum(deviceNum);
    }

    public Device findByDeviceId(Long deviceId) {
        return deviceMapper.findById(deviceId);
    }
}
