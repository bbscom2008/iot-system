package com.example.demo.dto;

import com.example.demo.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备详情数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDetailDTO {
    
    private Long id;
    private Long userId;
    private String deviceNum;
    private String deviceName;
    private Integer deviceType;      // 设备类型ID
    private Integer deviceLineState; // 0-离线 1-在线
    private Integer signal;          // 信号强度
    private Integer electricQuantity; // 电量（0-100）
    private Integer warningStatus;   // 报警状态：0-正常 1-报警
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime createdTime;      // 创建时间
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime updatedTime;      // 更新时间
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime lastOfflineTime;  // 最后离线时间
    
    // 设备设置相关字段
    private Integer levelTime;              // 阶梯时间（秒），大于等于0的整数
    private Integer gasUpperLimit;          // 气体上限（ppm）
    private Integer gasLowerLimit;          // 气体下限（ppm）
    private Double tempUpperLimit;          // 温度上限（°C）
    private Double tempLowerLimit;          // 温度下限（°C）
    private Double humidityUpperLimit;      // 湿度上限（%）
    private Double humidityLowerLimit;      // 湿度下限（%）
    private Integer masterSwitch;           // 总开关：0-关闭 1-开启
    private Integer powerOffSwitch;         // 断电开关：0-关闭 1-开启
    private Integer tempSwitch;             // 温度开关：0-关闭 1-开启
    private Integer humiditySwitch;         // 湿度开关：0-关闭 1-开启
    private Integer gasSwitch;              // 气体开关：0-关闭 1-开启
    private Integer dialingMethod;          // 拨打方式：1-同时拨打 2-依次拨打
    private Integer alarmMethod;            // 报警方式：1-打电话 2-发短信 3-打电话和发短信
    private List<String> alarmPhones;       // 报警电话列表，最多10个电话
    
    private List<SensorDTO> sensors;         // 传感器列表
    private List<MotorFanDTO> motorFans;     // 风机列表
    private List<FrequencyMotorDTO> frequencyMotors; // 变频电机列表
}

