package com.example.demo.service;

import com.example.demo.entity.DeviceType;
import com.example.demo.mapper.DeviceTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceTypeService {

    private final DeviceTypeMapper deviceTypeMapper;

    /**
     * 查询所有设备类型
     */
    public List<DeviceType> findAll() {
        return deviceTypeMapper.findAll();
    }

    /**
     * 根据ID查询
     */
    public DeviceType findById(Long id) {
        return deviceTypeMapper.findById(id);
    }
}

