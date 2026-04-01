package com.example.demo.service;

import com.example.demo.entity.FrequencyMotor;
import com.example.demo.mapper.FrequencyMotorMapper;
import com.example.demo.util.JsonUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @param list 设备编号与对应值的映射
     * @return 更新的行数
     */
    public int batchUpdateValueByParentId(Long parentId, List<JsonUtils.KV<Integer>> list) {
        
        if (list == null || list.isEmpty()) {
            return 0;
        }

        List<FrequencyMotor> existing = frequencyMotorMapper.findByParentId(parentId);
        // 现有的所有风机的编号
        Set<String> existingCodes = new HashSet<>();
        if (existing != null) {
            for (FrequencyMotor mf : existing) {
                existingCodes.add(mf.getDeviceNum());
            }
        }
        for (JsonUtils.KV<Integer> e : list) {
            String deviceNum = e.getKey();
            // 如果某个编号不存在，就创建一个新的风机
            if (!existingCodes.contains(deviceNum)) {
                FrequencyMotor fan = getFrequencyMotor(parentId, e);
                frequencyMotorMapper.insert(fan);
            }
        }

        return frequencyMotorMapper.batchUpdateValueByParentId(parentId, list);
    }

    public static FrequencyMotor getFrequencyMotor(Long parentId, JsonUtils.KV<Integer> e) {
        FrequencyMotor fan = new FrequencyMotor();
        fan.setDeviceId(parentId);
        fan.setDeviceNum(e.getKey());
        fan.setValue(e.getValue());
        // 设置默认名称为设备编号
        String name = "变频";
        try {
            String idxStr = e.getKey().replaceAll("[^0-9]", "");
            if (!idxStr.isEmpty()) {
                name = name + idxStr;
            }
        } catch (Exception ignored) {
        }
        fan.setDeviceName(name);

        // 其他字段可以根据需要设置默认值
        fan.setFcm(0); // 默认手动模式
        fan.setMs(0.0);
        fan.setMrtm(0);
        fan.setMrts(0);
        fan.setMptm(0);
        fan.setMpts(0);

        fan.setAtps(0);
        fan.setAtls(0.0);
        fan.setAtrtm(0);
        fan.setAtrts(0);
        fan.setAtptm(0);
        fan.setAtpts(0);

        fan.setAhls(0.0);
        fan.setAhrtm(0);
        fan.setAhrts(0);
        fan.setAhptm(0);
        fan.setAhpts(0);

        fan.setAnls(0.0);
        fan.setAnrtm(0);
        fan.setAnrts(0);
        fan.setAnptm(0);
        fan.setAnpts(0);
        return fan;
    }
}