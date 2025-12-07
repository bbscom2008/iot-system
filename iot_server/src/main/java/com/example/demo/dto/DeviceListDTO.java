package com.example.demo.dto;

import com.example.demo.util.DateUtils;
import com.example.demo.entity.Device;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备列表项数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceListDTO {
    
    private Long id;
    private Long userId;
    private String userName;         // 用户名称
    private String userPhone;        // 用户手机号
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
    
    private List<SensorDTO> sensors; // 传感器列表
}

