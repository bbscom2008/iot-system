package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String phone;
    private String password;
    private String nikeName;
    private String address;
    private String icon;
    private Integer breedingType;   // 养殖类型：0-猪 1-羊 2-牛 3-鸡 4-鸭 5-其他（对应字典）
    private Integer role;           // 角色：0-老板 1-饲养员 2-其他（对应字典）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
