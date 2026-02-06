package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotorFanTimerTask implements Comparable<MotorFanTimerTask>{

    public static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("[H:mm][HH:mm][H:m][HH:m]");

    private Integer isEnable;
    private String startTime; //HH:mm 类型的时间
    private String endTime;
    private Long probeSensorId; //传感器ID
    private Double startTemp; // 启动温度
    private Double stopTemp; // 停止温度


    @Override
    public int compareTo(MotorFanTimerTask other) {
        LocalTime  thisTime =   LocalTime.parse(this.startTime, TIME_FORMATTER);
        LocalTime  otherTime =   LocalTime.parse(other.startTime, TIME_FORMATTER);

        return (int)otherTime.until(thisTime, ChronoUnit.SECONDS);
    }
}
