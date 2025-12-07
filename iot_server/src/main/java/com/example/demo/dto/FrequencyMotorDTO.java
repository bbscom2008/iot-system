package com.example.demo.dto;

import com.example.demo.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 变频电机数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FrequencyMotorDTO {
    
    private Long id;
    private Long deviceId;          // 设备ID
    private String deviceNum;       // 设备编号
    private String deviceName;      // 变频电机名称
    private Double protectSpeed;    // 保护转速（Double类型）
    private Integer isAuto;         // 运行模式：0-手动 1-自动
    private Double manualSpeed;     // 手动转速（Double类型）
    private Integer value;           // 当前值
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
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime createdTime;     // 创建时间
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime updatedTime;     // 更新时间
}

