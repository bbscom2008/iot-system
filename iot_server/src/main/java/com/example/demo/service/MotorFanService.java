package com.example.demo.service;

import com.example.demo.dto.MotorFanListDTO;
import com.example.demo.entity.MotorFan;
import com.example.demo.mapper.MotorFanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MotorFanService {

    private final MotorFanMapper motorFanMapper;

    /**
     * 根据父设备ID查询所有风扇
     */
    public List<MotorFan> findByParentId(Long parentId) {
        return motorFanMapper.findByParentId(parentId);
    }

    /**
     * 根据风机编码查询所有风扇
     */
    public List<MotorFan> findByFanCode(String fanCode) {
        return motorFanMapper.findByFanCode(fanCode);
    }

    /**
     * 查询所有风扇（关联设备和用户信息）
     */
    public List<MotorFanListDTO> findAll(Map<String, Object> params) {
        return motorFanMapper.findAll(params);
    }

    /**
     * 查询所有风扇
     */
    public List<MotorFan> findAll() {
        // 注意：这个方法返回的是MotorFan实体，不包含关联信息
        return List.of();
    }

    /**
     * 根据ID查询风扇
     */
    public MotorFan findById(Long id) {
        return motorFanMapper.findById(id);
    }

    /**
     * 新增风扇
     */
    public void insert(MotorFan motorFan) {
        motorFanMapper.insert(motorFan);
    }

    /**
     * 更新风扇运行状态
     */
    public void updateRunningStatus(Long id, Integer isRunning) {
        motorFanMapper.updateRunningStatus(id, isRunning);
    }

    /**
     * 更新风机配置
     */
    public void update(MotorFan motorFan) {
        motorFanMapper.update(motorFan);
    }

    /**
     * 删除风扇
     */
    public void deleteById(Long id) {
        motorFanMapper.deleteById(id);
    }

    /**
     * 删除父设备的所有风扇
     */
    public void deleteByParentId(Long parentId) {
        motorFanMapper.deleteByParentId(parentId);
    }
}