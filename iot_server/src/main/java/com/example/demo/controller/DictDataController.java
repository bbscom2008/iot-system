package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.*;
import com.example.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/dict")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DictDataController {

    // 4个字典服务
    private final BreedTypeService breedTypeService;
    private final UserRoleService userRoleService;
    private final DeviceTypeService deviceTypeService;
    private final WarningTypeService warningTypeService;

    /**
     * 获取养殖类型
     * GET /user/dict/breed-type/list
     */
    @GetMapping("/breed-type/list")
    public ApiResponse<Map<String, Object>> getBreedTypeList() {
        List<BreedType> list = breedTypeService.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        return ApiResponse.success(result);
    }

    /**
     * 获取角色类型（所有平台）
     * GET /user/dict/role-type/list
     */
    @GetMapping("/role-type/list")
    public ApiResponse<Map<String, Object>> getRoleTypeList() {
        List<UserRole> list = userRoleService.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        return ApiResponse.success(result);
    }

    /**
     * 根据平台获取角色类型
     * GET /user/dict/role-type/list/{platform}
     * @param platform mobile 或 web
     */
    @GetMapping("/role-type/list/{platform}")
    public ApiResponse<Map<String, Object>> getRoleTypeByPlatform(@PathVariable String platform) {
        List<UserRole> list = userRoleService.findByPlatform(platform);
        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        return ApiResponse.success(result);
    }

    /**
     * 获取设备类型
     * GET /user/dict/device-type/list
     */
    @GetMapping("/device-type/list")
    public ApiResponse<Map<String, Object>> getDeviceTypeList() {
        List<DeviceType> list = deviceTypeService.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        return ApiResponse.success(result);
    }

    /**
     * 获取报警类型
     * GET /user/dict/warning-type/list
     */
    @GetMapping("/warning-type/list")
    public ApiResponse<Map<String, Object>> getWarningTypeList() {
        List<WarningType> list = warningTypeService.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        return ApiResponse.success(result);
    }
}

