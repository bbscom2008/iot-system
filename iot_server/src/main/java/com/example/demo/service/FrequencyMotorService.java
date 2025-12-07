package com.example.demo.service;

import com.example.demo.entity.FrequencyMotor;
import com.example.demo.mapper.FrequencyMotorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FrequencyMotorService {

    private final FrequencyMotorMapper frequencyMotorMapper;

    /**
     * 根据父设备ID查询所有变频电机
     */
    public List<FrequencyMotor> findByParentId(Long parentId) {
        return frequencyMotorMapper.findByParentId(parentId);
    }

    /**
     * 根据设备编号查询所有变频电机
     */
    public List<FrequencyMotor> findByDeviceNum(String deviceNum) {
        return frequencyMotorMapper.findByDeviceNum(deviceNum);
    }

    /**
     * 查询所有变频电机（关联设备和用户信息）
     */
    public List<FrequencyMotor> findAll(Map<String, Object> params) {
        return frequencyMotorMapper.findAll(params);
    }

    /**
     * 查询所有变频电机
     */
    public List<FrequencyMotor> findAll() {
        return frequencyMotorMapper.findAll(null);
    }

    /**
     * 根据ID查询变频电机
     */
    public FrequencyMotor findById(Long id) {
        return frequencyMotorMapper.findById(id);
    }

    /**
     * 新增变频电机
     */
    public void insert(FrequencyMotor frequencyMotor) {
        frequencyMotorMapper.insert(frequencyMotor);
    }

    /**
     * 更新变频电机配置
     */
    public int update(FrequencyMotor frequencyMotor) {
        return frequencyMotorMapper.update(frequencyMotor);
    }

    /**
     * 删除变频电机
     */
    public void deleteById(Long id) {
        frequencyMotorMapper.deleteById(id);
    }

    /**
     * 删除父设备的所有变频电机
     */
    public void deleteByParentId(Long parentId) {
        frequencyMotorMapper.deleteByParentId(parentId);
    }
}