package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.SendVerifyCodeRequest;
import com.example.demo.dto.LoginByCodeRequest;
import com.example.demo.dto.ChangePhoneRequest;
import com.example.demo.dto.ChangePasswordByOldRequest;
import com.example.demo.dto.ChangePasswordByPhoneRequest;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.enums.PlatformType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     * POST /user/register
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求，手机号: {}", request.getPhone());
        userService.register(request);
        log.info("注册成功，准备返回响应");
        ApiResponse<String> response = ApiResponse.success("注册成功");
        log.info("返回响应: {}", response);
        return response;
    }

    /**
     * 发送注册验证码（注册时使用）
     * POST /user/sendRegisterCode
     */
    @PostMapping("/sendRegisterCode")
    public ApiResponse<String> sendRegisterCode(@Valid @RequestBody SendVerifyCodeRequest request) {
        userService.sendRegisterCode(request.getPhone());
        return ApiResponse.success("验证码已发送");
    }

    /**
     * 发送登录验证码（登录时使用）
     * POST /user/sendLoginCode
     */
    @PostMapping("/sendLoginCode")
    public ApiResponse<String> sendLoginCode(@Valid @RequestBody SendVerifyCodeRequest request) {
        userService.sendLoginCode(request.getPhone());
        return ApiResponse.success("验证码已发送");
    }

    /**
     * 发送验证码（兼容旧接口）
     * POST /user/sendVerifyCode
     * @deprecated 请使用 sendRegisterCode 或 sendLoginCode
     */
    @Deprecated
    @PostMapping("/sendVerifyCode")
    public ApiResponse<String> sendVerifyCode(@Valid @RequestBody SendVerifyCodeRequest request) {
        userService.sendRegisterCode(request.getPhone());
        return ApiResponse.success("验证码已发送");
    }

    /**
     * 用户登录（密码登录）
     * POST /user/login
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 验证码登录
     * POST /user/loginByCode
     */
    @PostMapping("/loginByCode")
    public ApiResponse<LoginResponse> loginByCode(@Valid @RequestBody LoginByCodeRequest request) {
        LoginResponse response = userService.loginByCode(request.getPhone(), request.getVerifyCode());
        return ApiResponse.success(response);
    }

    /**
     * 用户登出
     * POST /user/logout
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        // JWT是无状态的，登出操作主要在前端清除token
        // 后端可以在这里记录登出日志或执行其他业务逻辑
        log.info("用户登出");
        return ApiResponse.success("登出成功");
    }

    /**
     * 获取Token中的角色信息（用于测试）
     * GET /user/token-info
     */
    @GetMapping("/token-info")
    public ApiResponse<Map<String, Object>> getTokenInfo(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String phone = jwtUtil.getPhoneFromToken(token);
        String platform = jwtUtil.getPlatformFromToken(token);
        String roleId = jwtUtil.getRoleIdFromToken(token);
        
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", userId);
        tokenInfo.put("phone", phone);
        tokenInfo.put("platform", platform);
        tokenInfo.put("roleId", roleId);
        tokenInfo.put("isAdmin", "10".equals(roleId)); // 角色ID为10的是管理员
        
        return ApiResponse.success(tokenInfo);
    }

    /**
     * 获取用户信息
     * GET /user/info
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getUserInfo(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserInfo(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", user);
        return ApiResponse.success(result);
    }

    /**
     * 更新用户信息
     * POST /user/updateInfo
     */
    @PostMapping("/updateInfo")
    public ApiResponse<String> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody User updateData) {
        log.info("=== Controller 接收到更新请求 ===");
        log.info("更新数据: {}", updateData);
        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("从Token解析的用户ID: {}", userId);
        userService.updateUserInfo(userId, updateData);
        return ApiResponse.success("更新成功");
    }

    /**
     * 发送修改手机号验证码
     * POST /user/sendChangePhoneCode
     */
    @PostMapping("/sendChangePhoneCode")
    public ApiResponse<String> sendChangePhoneCode(@Valid @RequestBody SendVerifyCodeRequest request) {
        userService.sendChangePhoneCode(request.getPhone());
        return ApiResponse.success("验证码已发送");
    }

    /**
     * 修改手机号
     * POST /user/changePhone
     */
    @PostMapping("/changePhone")
    public ApiResponse<String> changePhone(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ChangePhoneRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        userService.changePhone(userId, request.getNewPhone(), request.getVerifyCode());
        return ApiResponse.success("手机号修改成功");
    }

    /**
     * 发送重置密码验证码
     * POST /user/sendResetPasswordCode
     */
    @PostMapping("/sendResetPasswordCode")
    public ApiResponse<String> sendResetPasswordCode(@Valid @RequestBody SendVerifyCodeRequest request) {
        userService.sendResetPasswordCode(request.getPhone());
        return ApiResponse.success("验证码已发送");
    }

    /**
     * 获取用户列表（web端使用）
     * GET /user/list
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<User>> getUserList(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false, name = "nickName") String nickName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer breedingType) {
        // 验证是web端用户
        String platformUserStr = jwtUtil.getPlatformFromToken(token);
        PlatformType platformUser = PlatformType.from(platformUserStr);
        if (platformUser != PlatformType.WEB) {
            return ApiResponse.error("无权访问");
        }
        
        PageResult<User> result = userService.getUserList(pageNum, pageSize, search, platform, nickName, phone, breedingType);
        return ApiResponse.success(result);
    }

    /**
     * 通过原密码修改密码
     * POST /user/changePasswordByOld
     */
    @PostMapping("/changePasswordByOld")
    public ApiResponse<String> changePasswordByOld(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ChangePasswordByOldRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        userService.changePasswordByOld(userId, request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success("密码修改成功");
    }

    @PostMapping("/changePasswordByPhone")
    public ApiResponse<String> changePasswordByPhone(@Valid @RequestBody ChangePasswordByPhoneRequest request) {
        userService.changePasswordByPhone(request.getPhone(), request.getVerifyCode(), request.getNewPassword());
        return ApiResponse.success("密码修改成功");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteUser(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id) {
        String platformUserStr = jwtUtil.getPlatformFromToken(token);
        PlatformType platformUser = PlatformType.from(platformUserStr);
        if (platformUser != PlatformType.WEB) {
            return ApiResponse.error("无权访问");
        }
        userService.deleteUserById(id);
        return ApiResponse.success("删除成功");
    }

    /**
     * 管理员更新指定用户
     */
    @PostMapping("/admin/update/{id}")
    public ApiResponse<String> updateUserByAdmin(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id,
            @RequestBody User updateData) {
        String platformUserStr = jwtUtil.getPlatformFromToken(token);
        PlatformType platformUser = PlatformType.from(platformUserStr);
        if (platformUser != PlatformType.WEB) {
            return ApiResponse.error("无权访问");
        }
        userService.updateUserByAdmin(id, updateData);
        return ApiResponse.success("更新成功");
    }
}
