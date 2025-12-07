package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 传感器数据记录实体
 * 用于记录传感器每10秒采集一次的历史数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorData {

    private Long id;
    private Long sensorId;           // 传感器ID，关联sensor表的id
    private Long deviceId;           // 设备ID，关联device表的id
    private String deviceNum;        // 设备编号
    private String sensorCode;       // 传感器编号
    private Integer sensorTypeId;    // 传感器类型ID：5-温度 6-湿度 7-气体
    private String sensorName;       // 传感器名称
    private Double sensorValue;      // 传感器值（Double类型，精度足够，性能更好）
    private String unit;             // 单位：°C, %, ppm等
    private LocalDateTime recordTime; // 记录时间（每10秒记录一次）

    // 构造函数 - 从Sensor对象创建
    public SensorData(Sensor sensor, LocalDateTime recordTime) {
        this.sensorId = sensor.getId();
        this.deviceId = sensor.getParentId();
        this.sensorCode = sensor.getSensorCode();
        this.sensorTypeId = sensor.getSensorTypeId();
        this.sensorName = sensor.getSensorName();
        this.sensorValue = sensor.getSensorValue();
        this.unit = sensor.getUnit();
        this.recordTime = recordTime;
    }
}

