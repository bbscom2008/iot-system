package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.SensorData;
import com.example.demo.service.SensorDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/sensor-data")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SensorDataController {

    private final SensorDataService sensorDataService;

    // 自定义日期时间格式：yyyy-MM-dd HH:mm:ss
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取设备传感器的历史数据
     * GET /sensor-data/history
     */
    @GetMapping("/history")
    public ApiResponse<List<SensorData>> getSensorHistory(
            @RequestParam Long deviceId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        
        // 如果没有指定时间范围，使用默认值（今天00:00到23:59）
        if (start == null) {
            start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        if (end == null) {
            end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        }
        
        List<SensorData> data = sensorDataService.getDeviceSensorHistory(deviceId, start, end);
        return ApiResponse.success(data);
    }

    /**
     * 获取指定传感器的历史数据
     * GET /sensor-data/history/{sensorId}
     */
    @GetMapping("/history/{sensorId}")
    public ApiResponse<List<SensorData>> getSensorHistoryBySensorId(
            @PathVariable Long sensorId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        
        // 如果没有指定时间范围，使用默认值（今天00:00到23:59）
        if (start == null) {
            start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        if (end == null) {
            end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        }
        
        List<SensorData> data = sensorDataService.getSensorHistory(sensorId, start, end);
        return ApiResponse.success(data);
    }
    
    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            // 如果解析失败，返回null
            return null;
        }
    }
}

