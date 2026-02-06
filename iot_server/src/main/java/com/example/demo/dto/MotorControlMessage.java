package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 电机控制消息DTO
 * 用于RabbitMQ通信
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MotorControlMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 电机ID
     */
    private Long motorId;

    /**
     * 设备ID（父设备）
     */
    private Long deviceId;

    /**
     * 设备编号  如  mt1  mt2   imt1  imt2
     */
    private String deviceNum;

    /**
     * 父设备编号
     */
    private String parentDeviceNum;

    /**
     * 电机状态: 0 = 停止, 1 = 运行
     */
    private Integer state;

    /**
     * 控制模式: 1 = 温度控制, 2 = 循环, 3 = 湿度控制, 
     * 4 = 气体控制, 5 = 定时
     */
    private Integer controlMode;

    /**
     * 自动模式: 1 = 自动, 2 = 始终开, 3 = 始终关
     */
    private Integer autoMode;

    /**
     * 延时时间（毫秒），用于延时消息队列
     * 0表示无延时
     */
    private Integer delayTime;

    /**
     * 消息时间戳 , 单位毫秒，表示消息生成的时间
     */
    private Long timestamp;

    /**
     * 运行时间，单位毫秒，表示计划执行的时间
     */
    private Long runningTime;

    /**
     * 备注信息
     */
    private String remarks;
}
