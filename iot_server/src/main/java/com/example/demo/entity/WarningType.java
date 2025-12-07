package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarningType {

    private Long id;
    private String dictLabel;
    private String dictValue;
    private Integer dictSort;
    private String remark;
    private LocalDateTime createdAt;
}

