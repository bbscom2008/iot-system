package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceType {

    private Long id;
    private String name;
    private String typeId;
    private String remark;
    private LocalDateTime createdAt;
}

