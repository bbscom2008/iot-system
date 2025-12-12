package com.example.demo.service;

import com.example.demo.entity.FrequencyMotor;
import com.example.demo.mapper.FrequencyMotorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    
    /**
     * 根据父设备ID和设备编号更新变频电机的当前值
     */
    public int updateValueByParentAndCode(Long parentId, String code, Integer value) {
        return frequencyMotorMapper.updateValueByParentAndCode(parentId, code, value);
    }
    
    /**
     * 根据父设备ID批量更新变频电机的当前值
     * @param parentId 父设备ID
     * @param valuesMap 设备编号与对应值的映射
     * @return 更新的行数
     */
    public int batchUpdateValueByParentId(Long parentId, Map<String, Integer> valuesMap) {
        if (valuesMap == null || valuesMap.isEmpty()) {
            return 0;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("parentId", parentId);
        params.put("valuesMap", valuesMap);
        return frequencyMotorMapper.batchUpdateValueByParentId(params);
    }
}