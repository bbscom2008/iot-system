package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceWarning {

    private Long id;
    private Long deviceId;
    private String deviceNum;
    private Long userId;

    // 温度报警与温度值
    private Integer ta1;
    private Double ts1;
    private Integer ta2;
    private Double ts2;
    private Integer ta3;
    private Double ts3;
    private Integer ta4;
    private Double ts4;

    // 湿度报警与湿度值
    private Integer ha;
    private Double hv;

    // 氨气报警与氨气值
    private Integer na;
    private Double nv;

    private LocalDateTime createdAt;
}

