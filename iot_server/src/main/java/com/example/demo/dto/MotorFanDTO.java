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
    private Integer wm;                  // 工作模式：0温控，1循环，2湿控，3氨气，4定时
    private Integer autoMode;            // 自动模式：1-自动 2-开 3-关
    
    // 温控字段
    private Integer tcps;
    private Double tcat;
    private Double tcot;
    private Integer tcltrm;
    private Integer tcltrs;
    private Integer tcltpm;
    private Integer tcltps;
    private Integer tctcm;               // 温控模式：0-降温 1-升温
    // 循环字段
    private Integer ccps;
    private Double cctu;
    private Double cctd;
    private Integer ccrm;
    private Integer ccrs;
    private Integer ccpm;
    private Integer ccpss;
    private Integer cccm;                // 循环模式：0-降温 1-升温 2-时间
    
    // 湿控字段
    private Double hchu;
    private Double hchd;
    private Integer hcrm;
    private Integer hcrs;
    private Integer hcpm;
    private Integer hcps;
    private Integer hchcm;               // 湿控模式：0-除湿 1-加湿
    
    // 氨气字段
    private Integer ncnu;
    private Integer ncnd;
    private Integer ncrm;
    private Integer ncrs;
    private Integer ncpm;
    private Integer ncps;
    
    // 定时字段
    private Integer tict1nf;
    private Integer tict1nh;
    private Integer tict1nm;
    private Integer tict1fh;
    private Integer tict1fm;
    private Integer tict2nf;
    private Integer tict2nh;
    private Integer tict2nm;
    private Integer tict2fh;
    private Integer tict2fm;
    private Integer tict3nf;
    private Integer tict3nh;
    private Integer tict3nm;
    private Integer tict3fh;
    private Integer tict3fm;
    private Integer ticps;
    private Double ticat;
    private Double ticot;
    private Integer tictitm;             // 定时温控：0-降温 1-升温
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime createdTime;    // 创建时间
    
    @JsonFormat(pattern = DateUtils.DATE_TIME_FORMAT, timezone = DateUtils.TIME_ZONE)
    private LocalDateTime updatedTime;    // 更新时间
}

