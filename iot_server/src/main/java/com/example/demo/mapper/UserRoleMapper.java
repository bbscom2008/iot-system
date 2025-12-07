package com.example.demo.mapper;

import com.example.demo.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    /**
     * 查询所有角色类型
     */
    List<UserRole> findAll();

    /**
     * 根据平台查询角色类型
     * @param platform mobile 或 web
     */
    List<UserRole> findByPlatform(@Param("platform") String platform);

    /**
     * 根据ID查询
     */
    UserRole findById(Long id);

    /**
     * 根据 roleId 和 platform 查询角色
     */
    UserRole findByRoleIdAndPlatform(@Param("roleId") String roleId, @Param("platform") String platform);
}

