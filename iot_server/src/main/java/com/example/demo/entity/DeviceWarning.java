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
    private String warningType;
    private String warningMsg;
    private Integer isRead;         // 0-未读 1-已读
    private LocalDateTime createdAt;
}

