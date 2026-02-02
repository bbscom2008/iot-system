package com.example.demo.service;

import com.example.demo.dto.DeviceDetailDTO;
import com.example.demo.dto.DeviceListDTO;
import com.example.demo.dto.DeviceStatistics;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.SensorDTO;
import com.example.demo.entity.Device;
import com.example.demo.entity.FrequencyMotor;
import com.example.demo.entity.MotorFan;
import com.example.demo.entity.Sensor;
import com.example.demo.enums.PlatformType;
import com.example.demo.mapper.DeviceMapper;
import com.example.demo.mapper.FrequencyMotorMapper;
import com.example.demo.mapper.MotorFanMapper;
import com.example.demo.mapper.SensorMapper;
import com.example.demo.mapper.SensorDataMapper;
import com.example.demo.util.DtoConverter;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceMapper deviceMapper;
    private final SensorMapper sensorMapper;
    private final MotorFanMapper motorFanMapper;
    private final FrequencyMotorMapper frequencyMotorMapper;
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
            alarmDevices = deviceMapper.countWarningByUserId(userId);
        }

        // 计算离线设备数
        Long offlineDevices = totalDevices - onlineDevices;

        return new DeviceStatistics(totalDevices, onlineDevices, offlineDevices, alarmDevices);
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

    // 在应用启动完成后，将所有设备的在线状态和报警状态初始化为 0，并更新 updated_time
    @EventListener(ApplicationReadyEvent.class)
    public void initDeviceStatesOnStartup() {
        try {
            deviceMapper.resetAllDeviceStates();
            logger.info("Reset all device states on startup (device_line_state=0, warning_status=0, updated_time=now)");
        } catch (Exception e) {
            logger.error("Failed to reset device states on startup", e);
        }
    }

    // 每 10 分钟检测一次设备更新时间，超过 1 分钟则标记为离线
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void scheduledCheckDeviceOnlineStatus() {
        try {
            List<Device> devices = deviceMapper.findList(new HashMap<>());
            LocalDateTime now = LocalDateTime.now();
            for (Device d : devices) {
                LocalDateTime updated = d.getUpdatedTime();
                int state = 0;
                if (updated != null) {
                    Duration diff = Duration.between(updated, now);
                    if (Math.abs(diff.getSeconds()) <= 60) {
                        state = 1;
                    } else {
                        state = 0;
                    }
                } else {
                    state = 0;
                }

                try {
                    // 如果新状态和原状态不同，就更新状态
                    if (!Objects.equals(d.getDeviceLineState(), state)) {
                        deviceMapper.updateDeviceOnlineState(d.getDeviceNum(), state);
                    }
                } catch (Exception ex) {
                    logger.error("Failed to update device online state for deviceNum={}", d.getDeviceNum(), ex);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to check device online status", e);
        }
    }

    /**
     * 更新设备状态，包含在线状态和报警状态
     * 
     * @param device
     * @param node
     */
    public void updateDeviceState(Device device, JsonNode node) {

        // 在线状态此时为 1 （因为刚收到消息）
        Integer onlineState = 1;
        // 报警状态，根据 node 中的温度数据，和 device 中的阈值比较，
        // 温度数据有 ts1 ts2 ts3 ts4 四个温度传感器
        Integer warningStatus = 0; // 默认正常

        List<JsonNode> tempNodes = List.of(
                node.get("ts1"),
                node.get("ts2"),
                node.get("ts3"),
                node.get("ts4"));
        // 只要四个温度有一个超过阈值，就设置为报警状态，不需要全部判断4个
        for (JsonNode tempNode : tempNodes) {
            if (tempNode != null && tempNode.isNumber()) {
                double tempValue = tempNode.asDouble();
                if (tempValue > device.getTempUpperLimit() || tempValue < device.getTempLowerLimit()) {
                    warningStatus = 1; // 报警
                    break;
                }
            }
        }
        deviceMapper.updateDeviceState(device.getDeviceNum(), onlineState, warningStatus);
    }

    public Device findByDeviceNum(String deviceNum) {
        return deviceMapper.findByDeviceNum(deviceNum);
    }

    public Device findByDeviceId(Long deviceId) {
        return deviceMapper.findById(deviceId);
    }
}
