package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MotorFanListDTO;
import com.example.demo.entity.MotorFan;
import com.example.demo.service.MotorFanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/motor-fan")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MotorFanController {

    private final MotorFanService motorFanService;

    /**
     * 获取所有风机列表（关联设备和用户信息）
     * GET /motor-fan/list
     */
    @GetMapping("/list")
    public ApiResponse<List<MotorFanListDTO>> getAllMotorFans(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userPhone,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String deviceNum,
            @RequestParam(required = false) String fanName) {
        
        // 构造查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("userName", userName);
        params.put("userPhone", userPhone);
        params.put("deviceName", deviceName);
        params.put("deviceNum", deviceNum);
        params.put("fanName", fanName);
        
        List<MotorFanListDTO> motorFans = motorFanService.findAll(params);
        return ApiResponse.success(motorFans);
    }

    /**
     * 获取父设备下的所有风机
     * GET /motor-fan/list/{parentId}
     */
    @GetMapping("/list/{parentId}")
    public ApiResponse<List<MotorFan>> getMotorFanList(@PathVariable Long parentId) {
        List<MotorFan> motorFans = motorFanService.findByParentId(parentId);
        return ApiResponse.success(motorFans);
    }

    /**
     * 根据ID获取风机详情
     * GET /motor-fan/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<MotorFan> getMotorFanById(@PathVariable Long id) {
        MotorFan motorFan = motorFanService.findById(id);
        return ApiResponse.success(motorFan);
    }

    /**
     * 新增风机
     * POST /motor-fan
     */
    @PostMapping
    public ApiResponse<String> addMotorFan(@RequestBody MotorFan motorFan) {
        // 验证必填字段
        if (motorFan.getFanName() == null || motorFan.getFanName().trim().isEmpty()) {
            throw new RuntimeException("风机名称不能为空");
        }
        if (motorFan.getDeviceId() == null) {
            throw new RuntimeException("父设备ID不能为空");
        }
        if (motorFan.getDeviceNum() == null || motorFan.getDeviceNum().trim().isEmpty()) {
            throw new RuntimeException("风机编码不能为空");
        }
        
        // 设置默认值
        if (motorFan.getIsRunning() == null) {
            motorFan.setIsRunning(0); // 默认停止
        }
        if (motorFan.getControlMode() == null) {
            motorFan.setControlMode(1); // 默认温控
        }
        if (motorFan.getAutoMode() == null) {
            motorFan.setAutoMode(1); // 默认自动
        }
        
        motorFanService.insert(motorFan);
        return ApiResponse.success("风机添加成功");
    }

    /**
     * 更新风机配置
     * PUT /motor-fan/update
     */
    @PutMapping("/update")
    public ApiResponse<String> updateMotorFan(@RequestBody MotorFan motorFan) {
        // 检查ID是否存在
        if (motorFan.getId() == null) {
            throw new RuntimeException("风机ID不能为空");
        }
        
        MotorFan existFan = motorFanService.findById(motorFan.getId());
        if (existFan == null) {
            throw new RuntimeException("风机不存在");
        }
        
        motorFanService.update(motorFan);
        return ApiResponse.success("更新成功");
    }

    /**
     * 删除风机
     * DELETE /motor-fan/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteMotorFan(@PathVariable Long id) {
        MotorFan existFan = motorFanService.findById(id);
        if (existFan == null) {
            throw new RuntimeException("风机不存在");
        }
        
        motorFanService.deleteById(id);
        return ApiResponse.success("风机删除成功");
    }
}