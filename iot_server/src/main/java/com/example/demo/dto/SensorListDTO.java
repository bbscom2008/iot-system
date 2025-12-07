package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorListDTO {
    private Long id;
    private Long parentId;
    private String sensorCode;
    private Integer sensorTypeId;
    private String sensorName;
    private Double sensorValue;
    private Double adjustValue;
    private String unit;
    private String createdAt;
    private String updatedAt;
    
    // 设备信息
    private String deviceName;
    private String deviceNum;
    
    // 用户信息
    private String userName;
    private String userPhone;
}