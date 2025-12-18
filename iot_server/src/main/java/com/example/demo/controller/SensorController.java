package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.SensorDTO;
import com.example.demo.dto.SensorListDTO;
import com.example.demo.dto.SensorListDTO;
import com.example.demo.entity.Sensor;
import com.example.demo.service.SensorService;
import com.example.demo.util.DtoConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sensor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SensorController {

    private final SensorService sensorService;
    private final DtoConverter dtoConverter;

    /**
     * 根据设备ID获取传感器列表
     * GET /sensor/device/{deviceId}
     */
    @GetMapping("/device/{deviceId}")
    public ApiResponse<Map<String, Object>> getSensorsByDeviceId(@PathVariable Long deviceId) {
        List<Sensor> list = sensorService.findByDeviceId(deviceId);
        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        return ApiResponse.success(result);
    }

    /**
     * 根据设备编号获取传感器列表
     * GET /sensor/device-num/{deviceNum}
     */
    @GetMapping("/device-num/{deviceNum}")
    public ApiResponse<Map<String, Object>> getSensorsByDeviceNum(@PathVariable String deviceNum) {
        List<Sensor> list = sensorService.findByDeviceNum(deviceNum);
        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID获取传感器详情
     * GET /sensor/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<SensorDTO> getSensorById(@PathVariable Long id) {
        Sensor sensor = sensorService.findById(id);
        if (sensor == null) {
            throw new RuntimeException("传感器不存在");
        }
        SensorDTO sensorDTO = dtoConverter.toSensorDTO(sensor);
        return ApiResponse.success(sensorDTO);
    }

    /**
     * 新增传感器
     * POST /sensor
     */
    @PostMapping
    public ApiResponse<String> addSensor(Long parentId, @RequestBody Sensor sensor) {
        sensorService.addSensor( sensor);
        return ApiResponse.success("传感器添加成功");
    }

    /**
     * 更新传感器值
     * PUT /sensor/{id}/value
     */
    @PutMapping("/{id}/value")
    public ApiResponse<String> updateSensorValue(
            @PathVariable Long id,
            @RequestBody Map<String, Double> params) {
        Double sensorValue = params.get("sensorValue");
        sensorService.updateSensorValue(id, sensorValue);
        return ApiResponse.success("传感器值更新成功");
    }

    /**
     * 更新传感器信息
     * PUT /sensor/update
     */
    @PutMapping("/update")
    public ApiResponse<String> updateSensor(@RequestBody Long deviceId, @RequestBody Sensor sensor) {
        sensorService.updateSensor(deviceId, sensor);
        return ApiResponse.success("传感器更新成功");
    }

    /**
     * 删除传感器
     * DELETE /sensor/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteSensor(@PathVariable Long id) {
        sensorService.deleteSensor(id);
        return ApiResponse.success("传感器删除成功");
    }

    /**
     * 分页查询传感器列表（关联设备和用户信息）
     * GET /sensor/list
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getSensorList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String deviceNum,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userPhone,
            @RequestParam(required = false) String sensorName,
            @RequestParam(required = false) String sensorCode) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        params.put("deviceName", deviceName);
        params.put("deviceNum", deviceNum);
        params.put("userName", userName);
        params.put("userPhone", userPhone);
        params.put("sensorName", sensorName);
        params.put("sensorCode", sensorCode);

        // 分页参数
        if (pageNum != null && pageSize != null) {
            int offset = (pageNum - 1) * pageSize;
            params.put("offset", offset);
            params.put("pageSize", pageSize);
        }

        List<SensorListDTO> list = sensorService.findListWithDeviceAndUser(params);
        Long total = sensorService.countListWithDeviceAndUser(params);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        return ApiResponse.success(result);
    }
}

