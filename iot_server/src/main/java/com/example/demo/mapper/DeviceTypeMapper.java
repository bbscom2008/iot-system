package com.example.demo.mapper;

import com.example.demo.entity.DeviceType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeviceTypeMapper {

    /**
     * 查询所有设备类型
     */
    List<DeviceType> findAll();

    /**
     * 根据ID查询
     */
    DeviceType findById(Long id);
}

