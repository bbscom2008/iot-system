package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.FrequencyMotor;
import com.example.demo.service.FrequencyMotorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/frequency-motor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FrequencyMotorController {

    private final FrequencyMotorService frequencyMotorService;

    /**
     * 获取所有变频电机列表（关联设备和用户信息）
     * GET /frequency-motor/list
     */
    @GetMapping("/list")
    public ApiResponse<List<FrequencyMotor>> getAllFrequencyMotors(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userPhone,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String deviceNum,
            @RequestParam(required = false) String motorName,
            @RequestParam(required = false) String motorCode) {
        
        // 构造查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("userName", userName);
        params.put("userPhone", userPhone);
        params.put("deviceName", deviceName);
        params.put("deviceNum", deviceNum);
        params.put("motorName", motorName);
        params.put("motorCode", motorCode);
        
        List<FrequencyMotor> frequencyMotors = frequencyMotorService.findAll(params);
        return ApiResponse.success(frequencyMotors);
    }

    /**
     * 获取父设备下的所有变频电机
     * GET /frequency-motor/list/{parentId}
     */
    @GetMapping("/list/{parentId}")
    public ApiResponse<List<FrequencyMotor>> getFrequencyMotorList(@PathVariable Long parentId) {
        List<FrequencyMotor> frequencyMotors = frequencyMotorService.findByParentId(parentId);
        return ApiResponse.success(frequencyMotors);
    }

    /**
     * 根据ID获取变频电机详情
     * GET /frequency-motor/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<FrequencyMotor> getFrequencyMotorById(@PathVariable Long id) {
        FrequencyMotor frequencyMotor = frequencyMotorService.findById(id);
        return ApiResponse.success(frequencyMotor);
    }

    /**
     * 新增变频电机
     * POST /frequency-motor
     */
    @PostMapping
    public ApiResponse<String> addFrequencyMotor(@RequestBody FrequencyMotor frequencyMotor) {
        // 验证必填字段
        if (frequencyMotor.getDeviceName() == null || frequencyMotor.getDeviceName().trim().isEmpty()) {
            throw new RuntimeException("变频电机名称不能为空");
        }
        if (frequencyMotor.getDeviceId() == null) {
            throw new RuntimeException("父设备ID不能为空");
        }
        if (frequencyMotor.getDeviceNum() == null || frequencyMotor.getDeviceNum().trim().isEmpty()) {
            throw new RuntimeException("变频器设备编码不能为空");
        }
        
        // 设置默认值
        if (frequencyMotor.getIsAuto() == null) {
            frequencyMotor.setIsAuto(0); // 默认手动
        }
        if (frequencyMotor.getControlType() == null) {
            frequencyMotor.setControlType(1); // 默认温控
        }
        if (frequencyMotor.getValue() == null) {
            frequencyMotor.setValue(10); // 默认值
        }
        if (frequencyMotor.getManualSpeed() == null) {
            frequencyMotor.setManualSpeed(10.0); // 默认手动转速
        }
        if (frequencyMotor.getRunTime() == null) {
            frequencyMotor.setRunTime(60); // 默认运行时间
        }
        if (frequencyMotor.getPauseTime() == null) {
            frequencyMotor.setPauseTime(30); // 默认暂停时间
        }
        
        frequencyMotorService.insert(frequencyMotor);
        return ApiResponse.success("变频电机添加成功");
    }

    /**
     * 更新变频电机配置
     * PUT /frequency-motor/update
     */
    @PutMapping("/update")
    public ApiResponse<String> updateFrequencyMotor(@RequestBody FrequencyMotor frequencyMotor) {
        // 检查ID是否存在
        if (frequencyMotor.getId() == null) {
            throw new RuntimeException("变频电机ID不能为空");
        }
        
        FrequencyMotor existMotor = frequencyMotorService.findById(frequencyMotor.getId());
        if (existMotor == null) {
            throw new RuntimeException("变频电机不存在");
        }

        int affected = frequencyMotorService.update(frequencyMotor);
        if (affected <= 0) {
            throw new RuntimeException("更新失败，未影响任何行");
        }
        return ApiResponse.success("更新成功");
    }

    /**
     * 删除变频电机
     * DELETE /frequency-motor/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteFrequencyMotor(@PathVariable Long id) {
        FrequencyMotor existMotor = frequencyMotorService.findById(id);
        if (existMotor == null) {
            throw new RuntimeException("变频电机不存在");
        }
        
        frequencyMotorService.deleteById(id);
        return ApiResponse.success("变频电机删除成功");
    }
}