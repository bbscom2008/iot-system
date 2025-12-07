package com.example.demo.dto;

import com.example.demo.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 风机数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotorFanDTO {
    
    private Long id;
    private String fanName;              // 风扇名称
    private Long deviceId;               // 设备ID
    private String deviceNum;            // 设备编号
    private Integer isRunning;           // 运行状态：0-停止 1-运行
    private Integer controlMode;         // 控制模式：1-温控 2-循环 3-湿控 4-气体 5-定时
    private Integer autoMode;            // 自动模式：1-自动 2-开 3-关
    private Long probeSensorId;          // 探头传感器ID
    
    // 温控/循环共用字段
    private Double tempUpper;            // 温度上限/启动温度（°C）
    private Double tempLower;            // 温度下限/停止温度（°C）
    private Integer runTime;             // 运行时间/低温运行（秒）
    private Integer pauseTime;           // 暂停时间/低温暂停（秒）
    
    // 湿控字段
    private Double humidityUpper;        // 湿度上限（%）
    private Double humidityLower;        // 湿度下限（%）
    
    // 气体字段
    private Integer gasUpper;            // 气体上限（ppm）
    private Integer gasLower;            // 气体下限（ppm）
    
    // 定时1字段
    private Integer timer1Enabled;       // 定时1开关：0-关闭 1-开启
    private String timer1StartTime;      // 定时1开启时间（HH:mm）
    private String timer1EndTime;        // 定时1关闭时间（HH:mm）
    private Long timer1ProbeSensorId;    // 定时1探头传感器ID
    private Double timer1StartTemp;      // 定时1启动温度（°C）
    private Double timer1StopTemp;       // 定时1停止温度（°C）
    
    // 定时2字段
    private Integer timer2Enabled;       // 定时2开关：0-关闭 1-开启
    private String timer2StartTime;      // 定时2开启时间（HH:mm）
    private String timer2EndTime;        // 定时2关闭时间（HH:mm）
    private Long timer2ProbeSensorId;    // 定时2探头传感器ID
    private Double timer2StartTemp;      // 定时2启动温度（°C）
    private Double timer2StopTemp;       // 定时2停止温度（°C）
    
    // 定时3字段
    private Integer timer3Enabled;       // 定时3开关：0-关闭 1-开启
    private String timer3StartTime;      // 定时3开启时间（HH:mm）
    private String timer3EndTime;        // 定时3关闭时间（HH:mm）
    private Long timer3ProbeSensorId;    // 定时3探头传感器ID
    private Double timer3StartTemp;      // 定时3启动温度（°C）
    private Double timer3StopTemp;       // 定时3停止温度（°C）
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime createdTime;    // 创建时间
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime updatedTime;    // 更新时间
}

