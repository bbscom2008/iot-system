package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.DeviceDetailDTO;
import com.example.demo.dto.DeviceListDTO;
import com.example.demo.dto.DeviceSettingsRequest;
import com.example.demo.dto.DeviceStatistics;
import com.example.demo.dto.PageResult;
import com.example.demo.entity.Device;
import com.example.demo.service.DeviceService;
import com.example.demo.util.DtoConverter;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeviceController {

    private final DeviceService deviceService;
    private final JwtUtil jwtUtil;
    private final DtoConverter dtoConverter;

    /**
     * 获取设备列表
     * GET /device/list
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<DeviceListDTO>> getDeviceList(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String deviceNum,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userPhone,
            @RequestParam(required = false) Integer warningStatus) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String platform = jwtUtil.getPlatformFromToken(token);
        
        // web端可以查看所有数据，mobile端只能查看自己的数据
        Long queryUserId = "web".equals(platform) ? null : userId;
        
        PageResult<DeviceListDTO> result = deviceService.getDeviceListDTO(
                queryUserId, pageNum, pageSize, deviceName, deviceNum, userName, userPhone, warningStatus);
        return ApiResponse.success(result);
    }

    /**
     * 获取设备统计
     * GET /device/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<DeviceStatistics> getStatistics(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String platform = jwtUtil.getPlatformFromToken(token);
        
        // web端查看所有统计，mobile端只看自己的
        Long queryUserId = "web".equals(platform) ? null : userId;
        
        DeviceStatistics statistics = deviceService.getStatistics(queryUserId);
        return ApiResponse.success(statistics);
    }

    /**
     * 获取设备详情
     * GET /device/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public ApiResponse<DeviceDetailDTO> getDeviceDetail(@PathVariable Long id) {
        DeviceDetailDTO deviceDetailDTO = deviceService.getDeviceDetailDTO(id);
        return ApiResponse.success(deviceDetailDTO);
    }

    /**
     * 绑定设备
     * POST /device/bind
     */
    @PostMapping("/bind")
    public ApiResponse<String> bindDevice(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> params) {
        String platform = jwtUtil.getPlatformFromToken(token);
        Long userId;
        
        // 如果是 web 端(后台端)，或者移动端（h5或小程序）  可以指定 userId，否则使用当前登录用户
        // 后台绑定设备时，
        if ("web".equals(platform) && params.containsKey("userId")) {
            Object userIdObj = params.get("userId");
            if (userIdObj != null) {
                userId = Long.valueOf(userIdObj.toString());
            } else {
                userId = null;
                throw new RuntimeException("userId 是 null");
            }
        } else {
            userId = jwtUtil.getUserIdFromToken(token);
        }
        
        String deviceNum = (String) params.get("deviceNum");
        String deviceName = "环控仪";//(String) params.get("deviceName");
        // 1 环控仪
        Integer deviceType =  1; // (Integer) params.get("deviceType");
        deviceService.bindDevice(userId, deviceNum, deviceName, deviceType);
        return ApiResponse.success("绑定成功");
    }

    /**
     * 解绑设备
     * PUT /device/un/bind
     */
    @PutMapping("/un/bind")
    public ApiResponse<String> unbindDevice(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Long> params) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        Long deviceId = params.get("deviceId");
        
        deviceService.unbindDevice(deviceId, userId);
        return ApiResponse.success("解绑成功");
    }

    /**
     * 更新设备设置
     * PUT /device/settings/{id}
     */
    @PutMapping("/settings/{id}")
    public ApiResponse<String> updateDeviceSettings(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody DeviceSettingsRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String platform = jwtUtil.getPlatformFromToken(token);
        
        // 使用工具类将 DTO 转换为 Entity
        Device device = dtoConverter.toEntity(request, Device.class);
        
        deviceService.updateDeviceSettings(id, userId, platform, device);
        return ApiResponse.success("更新成功");
    }

    /**
     * 删除设备
     * DELETE /device/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteDevice(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String platform = jwtUtil.getPlatformFromToken(token);
        deviceService.deleteDevice(id, userId, platform);
        return ApiResponse.success("删除成功");
    }
}

