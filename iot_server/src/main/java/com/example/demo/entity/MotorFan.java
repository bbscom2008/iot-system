package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotorFan {

    private Long id;
    private String fanName;              // 风扇名称
    private Long deviceId;               // 父设备的ID
    private String deviceNum;            // 风机编码
    private Integer isRunning;           // 运行状态：0-停止 1-运行
    private Integer wm;                  // 工作模式：0-温控 1-循环 2-湿控 3-氨气 4-定时
    private Integer autoMode;            // 自动模式：1-自动 2-开 3-关
    
    // 温控字段
    private Integer tcps;                // 温控探头选择：0~7
    private Double tcat;                 // 温控启动温度
    private Double tcot;                 // 温控停止温度
    private Integer tcltrm;              // 温控低温运行时间-分
    private Integer tcltrs;              // 温控低温运行时间-秒
    private Integer tcltpm;              // 温控低温暂停时间-分
    private Integer tcltps;              // 温控低温暂停时间-秒
    private Integer tctcm;               // 温控模式：0-降温 1-升温

    // 循环字段
    private Integer ccps;                // 循环探头选择：0~7
    private Double cctu;                 // 循环温度上限
    private Double cctd;                 // 循环温度下限
    private Integer ccrm;                // 循环运行时间-分
    private Integer ccrs;                // 循环运行时间-秒
    private Integer ccpm;                // 循环暂停时间-分
    private Integer ccpss;               // 循环暂停时间-秒
    private Integer cccm;                // 循环模式：0-降温 1-升温 2-时间

    // 湿控字段
    private Double hchu;                 // 湿控湿度上限
    private Double hchd;                 // 湿控湿度下限
    private Integer hcrm;                // 湿控运行时间-分
    private Integer hcrs;                // 湿控运行时间-秒
    private Integer hcpm;                // 湿控暂停时间-分
    private Integer hcps;                // 湿控暂停时间-秒
    private Integer hchcm;               // 湿控模式：0-除湿 1-加湿

    // 氨气字段
    private Integer ncnu;                // NH3上限
    private Integer ncnd;                // NH3下限
    private Integer ncrm;                // NH3运行时间-分
    private Integer ncrs;                // NH3运行时间-秒
    private Integer ncpm;                // NH3暂停时间-分
    private Integer ncps;                // NH3暂停时间-秒

    // 定时字段
    private Integer tict1nf;             // 定时1，0开1关
    private Integer tict1nh;             // 定时1开-时
    private Integer tict1nm;             // 定时1开-分
    private Integer tict1fh;             // 定时1关-时
    private Integer tict1fm;             // 定时1关-分

    private Integer tict2nf;             // 定时2，0开1关
    private Integer tict2nh;             // 定时2开-时
    private Integer tict2nm;             // 定时2开-分
    private Integer tict2fh;             // 定时2关-时
    private Integer tict2fm;             // 定时2关-分

    private Integer tict3nf;             // 定时3，0开1关
    private Integer tict3nh;             // 定时3开-时
    private Integer tict3nm;             // 定时3开-分
    private Integer tict3fh;             // 定时3关-时
    private Integer tict3fm;             // 定时3关-分

    private Integer ticps;               // 定时探头选择：0~7
    private Double ticat;                // 定时启动温度
    private Double ticot;                // 定时停止温度
    private Integer tictitm;             // 定时温控：0-降温 1-升温

    // 关联的设备和用户信息
    private String deviceName;           // 设备名称
    private String userName;             // 用户名称
    private String userPhone;            // 用户手机号

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}