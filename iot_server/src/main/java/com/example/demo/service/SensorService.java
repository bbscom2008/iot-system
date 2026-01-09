package com.example.demo.service;

import com.example.demo.dto.SensorListDTO;
import com.example.demo.entity.Sensor;
import com.example.demo.mapper.SensorMapper;
import com.example.demo.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorMapper sensorMapper;

    /**
     * 根据设备ID查询所有传感器
     */
    public List<Sensor> findByDeviceId(Long deviceId) {
        return sensorMapper.findByDeviceId(deviceId);
    }

    /**
     * 根据设备编号查询所有传感器
     */
    public List<Sensor> findByDeviceNum(String deviceNum) {
        return sensorMapper.findByDeviceNum(deviceNum);
    }

    /**
     * 根据ID查询传感器
     */
    public Sensor findById(Long id) {
        return sensorMapper.findById(id);
    }

    /**
     * 新增传感器
     * 如果传入了 sensor_code，则使用传入的编码；否则自动生成 sensor_code 格式：sen-{sensor_type_id}-{从1001开始递增}
     */
    public void addSensor(Sensor sensor) {
        // 如果 sensor_code 为空或null，才自动生成
        if (sensor.getSensorCode() == null || sensor.getSensorCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor code cannot be empty when add sensor");
        }
        
        // 检查 sensor_code 是否已存在
        Sensor existingSensor = sensorMapper.findBySensorCode(sensor.getParentId(), sensor.getSensorCode());
        if (existingSensor != null) {
            throw new RuntimeException("传感器编号已存在：" + sensor.getSensorCode());
        }
        sensorMapper.insert(sensor);
    }

//    /**
//     * 生成传感器编号
//     * 格式：sen-{sensor_type_id}-{从1001开始递增}
//     * 每个 sensor_type_id 独立计数，都从 1001 开始
//     */
//    private String generateSensorCode(Integer sensorTypeId) {
//        // 查询当前 sensor_type_id 的最大编号
//        String maxDeviceNum = sensorMapper.findMaxDeviceNumByType(sensorTypeId);
//
//        int nextNumber = 1001; // 默认从 1001 开始
//
//        if (maxDeviceNum != null && maxDeviceNum.matches("^sen-\\d+-(\\d+)$")) {
//            // 提取当前最大编号
//            try {
//                String[] parts = maxDeviceNum.split("-");
//                if (parts.length == 3) {
//                    int maxNumber = Integer.parseInt(parts[2]);
//                    nextNumber = maxNumber + 1;
//                }
//            } catch (NumberFormatException e) {
//                // 如果解析失败，使用默认值 1001
//                nextNumber = 1001;
//            }
//        }
//
//        return String.format("sen-%d-%d", sensorTypeId, nextNumber);
//    }

    /**
     * 更新传感器值
     */
    public void updateSensorValue(Long sensorId, Double sensorValue) {
        sensorMapper.updateValue(sensorId, sensorValue);
    }

    /**
     * 更新传感器信息
     */
    public void updateSensor(Long deviceId, Sensor sensor) {
        // 如果修改了 sensor_code，需要检查是否与其他传感器重复
        if (sensor.getSensorCode() != null && !sensor.getSensorCode().isEmpty()) {
            Sensor existingSensor = sensorMapper.findBySensorCode(deviceId, sensor.getSensorCode());
            if (existingSensor != null && !existingSensor.getId().equals(sensor.getId())) {
                throw new RuntimeException("传感器编号已存在：" + sensor.getSensorCode());
            }
        }
        
        sensorMapper.update(sensor);
    }

    /**
     * 删除传感器
     */
    public void deleteSensor(Long sensorId) {
        sensorMapper.deleteById(sensorId);
    }

    /**
     * 删除设备的所有传感器
     */
    public void deleteByDeviceId(Long deviceId) {
        sensorMapper.deleteByDeviceId(deviceId);
    }

    /**
     * 分页查询传感器列表（关联设备和用户信息）
     */
    public List<SensorListDTO> findListWithDeviceAndUser(Map<String, Object> params) {
        return sensorMapper.findListWithDeviceAndUser(params);
    }

    /**
     * 查询传感器总数（关联设备和用户信息）
     */
    public Long countListWithDeviceAndUser(Map<String, Object> params) {
        return sensorMapper.countListWithDeviceAndUser(params);
    }

    public void updateValueByParentAndCode(Long parentId, String sensorCode, Double sensorValue) {
        sensorMapper.updateValueByParentAndCode(parentId, sensorCode, sensorValue);
    }

    public void batchUpdateValueByParentId(Long parentId, List<JsonUtils.KV<Double>> skvList) {
        if (skvList == null || skvList.isEmpty()) {
            return;
        }
        List<Sensor> existing = sensorMapper.findByDeviceId(parentId);
        Set<String> existingCodes = new HashSet<>();
        if (existing != null) {
            for (Sensor s : existing) {
                existingCodes.add(s.getSensorCode());
            }
        }
        for (JsonUtils.KV<Double> e : skvList) {
            String tsKey = e.getKey(); // ts1  ts2
            if (!existingCodes.contains(tsKey)) {
                Sensor sensor = generateSensor(parentId, tsKey, e.getValue());
                addSensor(sensor);
            }
        }
//        Map<String, Object> params = new HashMap<>();
//        params.put("parentId", parentId);
//        params.put("valuesList", skvList);
        sensorMapper.batchUpdateValueByParentId(parentId, skvList);
    }

    /**
     * 根据 parentId ts1  value 创建 sensor 对象
     * @param parentId
     * @param tsKey
     * @param value
     * @return
     */
    private static Sensor generateSensor(Long parentId, String tsKey, Double value) {
        Sensor sensor = new Sensor();
        sensor.setParentId(parentId);
        sensor.setSensorCode(tsKey);
        sensor.setSensorTypeId(5);
        String name = "温度";
        try {
            String idxStr = tsKey.replaceAll("[^0-9]", "");
            if (!idxStr.isEmpty()) {
                name = name + idxStr;
            }
        } catch (Exception ignored) {
        }
        sensor.setSensorName(name);
        sensor.setSensorValue(value);
        sensor.setAdjustValue(0.0);
        sensor.setUnit("°C");
        return sensor;
    }
}

