package com.example.demo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.demo.dto.MotorFanListDTO;
import com.example.demo.entity.MotorFan;
import com.example.demo.mapper.MotorFanMapper;
import com.example.demo.util.JsonUtils;

import lombok.RequiredArgsConstructor;

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

    /**
     * 根据父设备与风机编码更新运行状态
     */
    public void updateRunningStatusByParentAndCode(Long parentId, String fanCode, Integer isRunning) {
        motorFanMapper.updateRunningStatusByParentAndCode(parentId, fanCode, isRunning);
    }

    public int batchUpdateRunningStatusByParentId(Long parentId, List<JsonUtils.KV<Integer>> motorList) {
        if (motorList == null || motorList.isEmpty()) {
            return 0;
        }
        List<MotorFan> existing = motorFanMapper.findByParentId(parentId);
        // 现有的所有风机的编号
        Set<String> existingCodes = new HashSet<>();
        if (existing != null) {
            for (MotorFan mf : existing) {
                existingCodes.add(mf.getDeviceNum());
            }
        }
        for (JsonUtils.KV<Integer> e : motorList) {
            String deviceNum = e.getKey();
            // 如果某个编号不存在，就创建一个新的风机
            if (!existingCodes.contains(deviceNum)) {
                MotorFan fan = getMotorFan(parentId, e);
                motorFanMapper.insert(fan);
            }
        }
        // Map<String, Object> params = new HashMap<>();
        // params.put("parentId", parentId);
        // params.put("motorList", motorList);
        return motorFanMapper.batchUpdateRunningStatusByParentId(parentId, motorList);
    }

    private static MotorFan getMotorFan(Long parentId, JsonUtils.KV<Integer> e) {
        MotorFan fan = new MotorFan();
        fan.setDeviceId(parentId);
        fan.setDeviceNum(e.getKey());
        Integer run = e.getValue();
        fan.setIsRunning(run == null ? 0 : run);
        fan.setControlMode(1);
        fan.setAutoMode(1);
        String name = "风机";
        try {
            String idxStr = e.getKey().replaceAll("[^0-9]", "");
            if (!idxStr.isEmpty()) {
                name = name + idxStr;
            }
        } catch (Exception ignored) {
        }
        fan.setFanName(name);
        return fan;
    }
}
