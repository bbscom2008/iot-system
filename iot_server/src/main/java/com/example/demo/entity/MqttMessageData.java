package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqttMessageData {
    private Long id;
    private String deviceNum;
    private Double ts1;
    private Double ts2;
    private Double ts3;
    private Double ts4;
    private Integer mt1;
    private Integer mt2;
    private Integer mt3;
    private Integer mt4;
    private Integer mt5;
    private Integer mt6;
    private Integer mt7;
    private Integer mt8;
    private Integer mt9;
    private Integer mt10;
    private Integer imt1;
    private Integer imt2;
    private LocalDateTime createdTime;
}

