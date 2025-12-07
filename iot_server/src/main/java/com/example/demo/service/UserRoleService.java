package com.example.demo.service;

import com.example.demo.entity.UserRole;
import com.example.demo.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleMapper userRoleMapper;

    /**
     * 查询所有角色类型
     */
    public List<UserRole> findAll() {
        return userRoleMapper.findAll();
    }

    /**
     * 根据平台查询角色类型
     * @param platform mobile 或 web
     */
    public List<UserRole> findByPlatform(String platform) {
        return userRoleMapper.findByPlatform(platform);
    }

    /**
     * 根据ID查询
     */
    public UserRole findById(Long id) {
        return userRoleMapper.findById(id);
    }
}

