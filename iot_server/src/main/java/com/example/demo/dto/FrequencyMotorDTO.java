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
    private Integer fcm;            // 变频模式：0手动，1自动温控，2自动湿控，3自动气体

    private Double ms;              // 手动转速
    private Integer mrtm;           // 手动运行时间-分
    private Integer mrts;           // 手动运行时间-秒
    private Integer mptm;           // 手动暂停时间-分
    private Integer mpts;           // 手动暂停时间-秒

    private Integer atps;           // 自动温控探头选择(0~6)
    private Double atls;            // 自动温控最低转速
    private Double atul;            // 自动温控温度上限
    private Double atdl;            // 自动温控温度下限
    private Double aswt;            // 自动温控停止工作温度
    private Integer atrtm;          // 自动温控运行时间-分
    private Integer atrts;          // 自动温控运行时间-秒
    private Integer atptm;          // 自动温控暂停时间-分
    private Integer atpts;          // 自动温控暂停时间-秒

    private Double ahls;            // 自动湿控最低转速
    private Double ahul;            // 自动湿控湿度上限
    private Double ahdl;            // 自动湿控湿度下限
    private Integer ahrtm;          // 自动湿控运行时间-分
    private Integer ahrts;          // 自动湿控运行时间-秒
    private Integer ahptm;          // 自动湿控暂停时间-分
    private Integer ahpts;          // 自动湿控暂停时间-秒

    private Double anls;            // 自动NH3最低转速
    private Double anul;            // 自动NH3上限
    private Double andl;            // 自动NH3下限
    private Integer anrtm;          // 自动NH3运行时间-分
    private Integer anrts;          // 自动NH3运行时间-秒
    private Integer anptm;          // 自动NH3暂停时间-分
    private Integer anpts;          // 自动NH3暂停时间-秒

    private Integer value;          // 当前值
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime createdTime;     // 创建时间
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime updatedTime;     // 更新时间
}

