package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    private Long id;
    private Long userId;
    private String deviceNum;
    private String imei;
    private String iccid;
    private String deviceName;
    private Integer deviceType;      // 设备类型ID
    private Integer deviceLineState; // 0-离线 1-在线
    private Integer signal;          // 信号强度
    private Integer power; // 电量（0-100）
    private Integer warningStatus;   // 报警状态：0-正常 1-报警
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private LocalDateTime lastOnlineTime; // 最后在线时间（心跳时间）
    
    // 设备设置相关字段
    private Integer levelTime;              // 阶梯时间（秒），大于等于0的整数
    private Integer gasUpperLimit;         // 气体上限（ppm）
    private Integer gasLowerLimit;         // 气体下限（ppm）
    private Double tempUpperLimit;         // 温度上限（°C）
    private Double tempLowerLimit;         // 温度下限（°C）
    private Double humidityUpperLimit;     // 湿度上限（%）
    private Double humidityLowerLimit;     // 湿度下限（%）
    private Integer tof1;                   // 温度开关1：0-开 1-关
    private Integer tof2;                   // 温度开关2：0-开 1-关
    private Integer tof3;                   // 温度开关3：0-开 1-关
    private Integer tof4;                   // 温度开关4：0-开 1-关
    private Integer hr;                     // 湿度里程
    private Integer nr;                     // 氨气里程
    private Integer tb;                     // 温度回差
    private Integer hb;                     // 湿度回差
    private Integer nb;                     // 氨气回差
    private Integer dialingMethod;          // 拨打方式：1-同时拨打 2-依次拨打
    private Integer alarmMethod;            // 报警方式：1-打电话 2-发短信 3-打电话和发短信
    private List<String> alarmPhones;       // 报警电话列表，最多10个电话
}

