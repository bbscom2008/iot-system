package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FrequencyMotor {

    private Long id;
    private Long deviceId;          // 父设备的ID
    private String deviceNum;       // 变频器设备编码
    private String deviceName;      // 变频电机名称：变频1、变频2
    private Double protectSpeed;    // 保护转速（Double类型）
    private Integer isAuto;         // 运行模式：0-手动 1-自动
    private Double manualSpeed;     // 手动转速（Double类型）
    private Integer value;           // 当前值（整数，范围0-256）
    private Integer runTime;        // 运行时间（秒）
    private Integer pauseTime;      // 暂停时间（秒）
    private Integer controlType;    // 控制类型：1-温控 2-湿控 3-气体
    private Long tempSensorId;      // 变频器选择的温度传感器ID
    private Double tempUpper;       // 温度上限（Double类型）
    private Double tempLower;       // 温度下限（Double类型）
    private Double humidityUpper;   // 湿度上限（Double类型）
    private Double humidityLower;   // 湿度下限（Double类型）
    private Double gasUpper;        // 气体上限（Double类型）
    private Double gasLower;        // 气体下限（Double类型）
    
    // 关联的设备和用户信息
    private String parentDeviceName; // 父设备名称
    private String parentDeviceNum;  // 父设备编号
    private String userName;         // 用户名称
    private String userPhone;        // 用户手机号

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}