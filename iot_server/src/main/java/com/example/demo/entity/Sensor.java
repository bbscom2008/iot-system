package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {

    private Long id;
    private Long parentId;          // 所属父设备ID
    private String sensorCode;      // 传感器编号，格式：sen-{sensor_type_id}-{从1001开始递增}
    private Integer sensorTypeId;   // 传感器类型ID：5-温度 6-湿度 7-气体
    private String sensorName;      // 传感器名称：温度1、温度2等
    private Double sensorValue;     // 传感器值（Double类型，精度足够，性能更好）
    private Double adjustValue; // 校正值（范围：-1024.00 到 1024.00）
    private String unit;            // 单位：°C, %, ppm等
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

