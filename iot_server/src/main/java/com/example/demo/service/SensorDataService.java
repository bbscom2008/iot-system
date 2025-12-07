package com.example.demo.service;

import com.example.demo.entity.Device;
import com.example.demo.entity.Sensor;
import com.example.demo.entity.SensorData;
import com.example.demo.mapper.DeviceMapper;
import com.example.demo.mapper.SensorDataMapper;
import com.example.demo.mapper.SensorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SensorDataService {

    private final SensorDataMapper sensorDataMapper;
    private final SensorMapper sensorMapper;
    private final DeviceMapper deviceMapper;
    
    // 缓存设备编号，避免重复查询
    private final Map<Long, String> deviceNumCache = new HashMap<>();

    /**
     * 记录传感器数据（每10秒执行一次）
     */
//    @Scheduled(fixedRate = 10000) // 10秒执行一次
    public void recordSensorData() {
        // 查询所有在线设备的所有传感器
        List<Sensor> allSensors = sensorMapper.findAll();
        
        if (allSensors != null && !allSensors.isEmpty()) {
            List<SensorData> sensorDataList = new ArrayList<>();
            LocalDateTime recordTime = LocalDateTime.now();
            
            for (Sensor sensor : allSensors) {
                SensorData sensorData = new SensorData(sensor, recordTime);
                // 获取设备编号
                String deviceNum = getDeviceNum(sensor.getParentId());
                sensorData.setDeviceNum(deviceNum);
                sensorDataList.add(sensorData);
            }
            
            // 批量插入数据
            if (!sensorDataList.isEmpty()) {
                batchInsert(sensorDataList);
            }
        }
    }

    /**
     * 批量插入传感器数据
     */
    @Transactional
    public void batchInsert(List<SensorData> sensorDataList) {
        sensorDataMapper.batchInsert(sensorDataList);
    }

    /**
     * 根据传感器ID查询历史数据
     */
    public List<SensorData> getSensorHistory(Long sensorId, LocalDateTime startTime, LocalDateTime endTime) {
        return sensorDataMapper.findBySensorId(sensorId, startTime, endTime);
    }

    /**
     * 根据设备ID查询所有传感器的历史数据
     */
    public List<SensorData> getDeviceSensorHistory(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return sensorDataMapper.findByDeviceId(deviceId, startTime, endTime);
    }

    /**
     * 清理历史数据（保留最近30天的数据）
     */
    @Transactional
    public void cleanOldData() {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(30);
        sensorDataMapper.deleteBeforeTime(beforeTime);
    }
    
    /**
     * 获取设备编号（带缓存）
     */
    private String getDeviceNum(Long deviceId) {
        // 先从缓存中获取
        if (deviceNumCache.containsKey(deviceId)) {
            return deviceNumCache.get(deviceId);
        }
        
        // 从数据库查询
        Device device = deviceMapper.findById(deviceId);
        if (device != null && device.getDeviceNum() != null) {
            deviceNumCache.put(deviceId, device.getDeviceNum());
            return device.getDeviceNum();
        }
        
        return null;
    }
}

