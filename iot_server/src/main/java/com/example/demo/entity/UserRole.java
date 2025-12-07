package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    private Long id;
    private String label;        // 角色名称
    private String roleId;       // 角色ID
    private String platform;     // 平台类型：mobile-手机端，web-PC端
    private Integer sort;        // 排序
    private String remark;
    private LocalDateTime createdAt;
}

