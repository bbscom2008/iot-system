package com.example.demo.service;

import com.example.demo.dto.MotorFanListDTO;
import com.example.demo.entity.MotorFan;
import com.example.demo.mapper.MotorFanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

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

    public int batchUpdateRunningStatusByParentId(Long parentId, Map<String, Integer> valuesMap) {
        if (valuesMap == null || valuesMap.isEmpty()) {
            return 0;
        }
        List<MotorFan> existing = motorFanMapper.findByParentId(parentId);
        Set<String> existingCodes = new HashSet<>();
        if (existing != null) {
            for (MotorFan mf : existing) {
                if (mf.getDeviceNum() != null) {
                    existingCodes.add(mf.getDeviceNum());
                }
            }
        }
        for (Map.Entry<String, Integer> e : valuesMap.entrySet()) {
            String code = e.getKey();
            if (!existingCodes.contains(code)) {
                MotorFan fan = new MotorFan();
                fan.setDeviceId(parentId);
                fan.setDeviceNum(code);
                Integer run = e.getValue();
                fan.setIsRunning(run == null ? 0 : run);
                fan.setControlMode(1);
                fan.setAutoMode(1);
                String name = "风机";
                try {
                    String idxStr = code.replaceAll("[^0-9]", "");
                    if (!idxStr.isEmpty()) {
                        name = name + idxStr;
                    }
                } catch (Exception ignored) {
                }
                fan.setFanName(name);
//                motorFanMapper.insert(fan);
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("parentId", parentId);
        params.put("valuesMap", valuesMap);
        return motorFanMapper.batchUpdateRunningStatusByParentId(params);
    }
}
