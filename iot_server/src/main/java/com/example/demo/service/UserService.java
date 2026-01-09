package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UserRoleMapper;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.demo.enums.PlatformType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final VerifyCodeService verifyCodeService;
    private final UserRoleMapper userRoleMapper;

    /**
     * 用户登录（密码登录）
     */
    public LoginResponse login(LoginRequest request) {
        log.info("开始密码登录，手机号: {}", request.getPhone());
        
        // 查找用户
        User user = userMapper.findByPhone(request.getPhone());
        if (user == null) {
            log.warn("登录失败：用户不存在 - {}", request.getPhone());
            throw new RuntimeException("用户不存在，请先注册");
        }
        
        log.info("找到用户，ID: {}, 手机号: {}", user.getId(), user.getPhone());

        // 验证密码（注意：生产环境应该使用加密密码）
        if (user.getPassword() == null || !user.getPassword().equals(request.getPassword())) {
            log.warn("登录失败：密码错误 - {}", request.getPhone());
            throw new RuntimeException("密码错误");
        }
        
        log.info("密码验证通过，准备生成Token");

        // 根据用户的 role 字段判断平台类型
        PlatformType platform = determinePlatform(user.getRole());
        String roleId = String.valueOf(user.getRole());
        
        log.info("用户角色: {}, 平台类型: {}", roleId, platform.getValue());

        // 生成Token（包含角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), platform.getValue(), roleId);
        log.info("登录成功，用户ID: {}, Token已生成", user.getId());

        // 返回登录响应
        return new LoginResponse(token, LoginResponse.UserInfo.from(user));
    }

    /**
     * 验证码登录
     */
    public LoginResponse loginByCode(String phone, String verifyCode) {
        // 查找用户
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证验证码
        boolean isCodeValid = verifyCodeService.verifyCode(phone, verifyCode);
        if (!isCodeValid) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 根据用户的 role 字段判断平台类型
        PlatformType platform = determinePlatform(user.getRole());
        String roleId = String.valueOf(user.getRole());

        // 生成Token（包含角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), platform.getValue(), roleId);

        // 返回登录响应
        return new LoginResponse(token, LoginResponse.UserInfo.from(user));
    }

    /**
     * 获取用户信息
     */
    public User getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 不返回密码
        user.setPassword(null);
        return user;
    }

    /**
     * 用户注册
     */
    @Transactional
    public void register(RegisterRequest request) {
        log.info("开始注册用户，手机号: {}", request.getPhone());
        
        // 1. 验证手机号是否已注册
        User existUser = userMapper.findByPhone(request.getPhone());
        if (existUser != null) {
            log.warn("注册失败：手机号已注册 - {}", request.getPhone());
            throw new RuntimeException("该手机号已注册");
        }

        // 2. 管理员后台新增时不校验验证码，普通用户注册按旧逻辑校验
        // 开发期间，全部暂不校验验证码
//        if (request.getVerifyCode() != null && !request.getVerifyCode().isEmpty()) {
//            boolean isCodeValid = verifyCodeService.verifyCode(request.getPhone(), request.getVerifyCode());
//            if (!isCodeValid) {
//                throw new RuntimeException("验证码错误或已过期");
//            }
//        }

        // 3. 创建新用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword()); // 注意：生产环境应该加密
        // 使用提供的昵称，若未提供则默认为手机号
        user.setNikeName(request.getNikeName() != null ? request.getNikeName() : request.getPhone());
        user.setAddress(request.getAddress()); // 地址
        user.setBreedingType(request.getBreedingType()); // Integer 类型
        user.setRole(request.getRole()); // Integer 类型

        // 4. 保存用户
        int result = userMapper.insert(user);
        log.info("注册成功，手机号: {}, 用户ID: {}, 昵称: {}, 地址: {}, 养殖类型: {}, 角色: {}, 影响行数: {}", 
                request.getPhone(), user.getId(), user.getNikeName(), user.getAddress(), request.getBreedingType(), request.getRole(), result);
    }

    /**
     * 发送注册验证码（注册时使用）
     */
    public void sendRegisterCode(String phone) {
        // 检查手机号是否已注册
        User existUser = userMapper.findByPhone(phone);
        if (existUser != null) {
            throw new RuntimeException("该手机号已注册");
        }

        // 发送验证码
        verifyCodeService.sendVerifyCode(phone);
    }

    /**
     * 发送登录验证码（登录时使用）
     */
    public void sendLoginCode(String phone) {
        // 检查手机号是否已注册
        User existUser = userMapper.findByPhone(phone);
        if (existUser == null) {
            throw new RuntimeException("该手机号未注册");
        }

        // 发送验证码
        verifyCodeService.sendVerifyCode(phone);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUserInfo(Long userId, User updateData) {
        log.info("=== 开始更新用户信息 ===");
        log.info("用户ID: {}", userId);
        log.info("接收到的更新数据: nikeName={}, breedingType={}, role={}, icon={}", 
                updateData.getNikeName(), updateData.getBreedingType(), 
                updateData.getRole(), updateData.getIcon());
        
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        log.info("更新前的用户数据: nikeName={}, breedingType={}, role={}", 
                user.getNikeName(), user.getBreedingType(), user.getRole());

        // 更新字段
        if (updateData.getNikeName() != null) {
            user.setNikeName(updateData.getNikeName());
        }
        if (updateData.getAddress() != null) {
            user.setAddress(updateData.getAddress());
        }
        if (updateData.getIcon() != null) {
            user.setIcon(updateData.getIcon());
        }
        if (updateData.getBreedingType() != null) {
            user.setBreedingType(updateData.getBreedingType());
        }
        if (updateData.getRole() != null) {
            user.setRole(updateData.getRole());
        }
        
        log.info("更新后的用户数据: nikeName={}, breedingType={}, role={}", 
                user.getNikeName(), user.getBreedingType(), user.getRole());

        int result = userMapper.updateUser(user);
        log.info("用户信息更新成功，用户ID: {}, 影响行数: {}", userId, result);
    }

    /**
     * 发送修改手机号验证码
     */
    public void sendChangePhoneCode(String newPhone) {
        // 检查新手机号是否已被注册
        User existUser = userMapper.findByPhone(newPhone);
        if (existUser != null) {
            throw new RuntimeException("该手机号已被其他用户使用");
        }

        // 发送验证码
        verifyCodeService.sendVerifyCode(newPhone);
        log.info("发送修改手机号验证码到: {}", newPhone);
    }

    /**
     * 修改手机号
     */
    @Transactional
    public void changePhone(Long userId, String newPhone, String verifyCode) {
        // 查找用户
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查新手机号是否与当前手机号相同
        if (newPhone.equals(user.getPhone())) {
            throw new RuntimeException("新手机号不能与当前手机号相同");
        }

        // 检查新手机号是否已被注册
        User existUser = userMapper.findByPhone(newPhone);
        if (existUser != null) {
            throw new RuntimeException("该手机号已被其他用户使用");
        }

        // 验证验证码
        boolean isCodeValid = verifyCodeService.verifyCode(newPhone, verifyCode);
        if (!isCodeValid) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 更新手机号
        user.setPhone(newPhone);
        userMapper.updateUser(user);
        log.info("手机号修改成功，用户ID: {}, 新手机号: {}", userId, newPhone);
    }

    /**
     * 发送重置密码验证码
     */
    public void sendResetPasswordCode(String phone) {
        // 检查手机号是否已注册
        User existUser = userMapper.findByPhone(phone);
        if (existUser == null) {
            throw new RuntimeException("该手机号未注册");
        }

        // 发送验证码
        verifyCodeService.sendVerifyCode(phone);
        log.info("发送重置密码验证码到: {}", phone);
    }

    /**
     * 根据用户角色ID判断平台类型
     * 角色ID 0-2 为手机端（0-老板, 1-饲养员, 2-其他）
     * 角色ID 10+ 为 PC端（10-管理员, 11-员工）
     */
    private PlatformType determinePlatform(Integer role) {
        if (role == null) {
            return PlatformType.MOBILE; // 默认为手机端
        }
        // role >= 10 为 PC端，< 10 为手机端
        return role >= 10 ? PlatformType.WEB : PlatformType.MOBILE;
    }

    /**
     * 通过原密码修改密码
     */
    @Transactional
    public void changePasswordByOld(Long userId, String oldPassword, String newPassword) {
        // 查找用户
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证原密码
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("原密码错误");
        }

        // 新旧密码不能相同
        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        // 更新密码
        user.setPassword(newPassword);
        userMapper.updateUser(user);
        log.info("密码修改成功（通过原密码），用户ID: {}", userId);
    }

    /**
     * 通过手机号验证码修改密码
     */
    @Transactional
    public void changePasswordByPhone(String phone, String verifyCode, String newPassword) {
        // 查找用户
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }

        // 验证验证码
        boolean isCodeValid = verifyCodeService.verifyCode(phone, verifyCode);
        if (!isCodeValid) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 更新密码
        user.setPassword(newPassword);
        userMapper.updateUser(user);
        log.info("密码修改成功（通过手机号），用户手机号: {}", phone);
    }

    /**
     * 获取用户列表（分页）
     */
    public PageResult<User> getUserList(Integer pageNum, Integer pageSize, String search, String platform, String nickName, String phone, Integer breedingType) {
        Map<String, Object> params = new HashMap<>();
        params.put("search", search);
        params.put("platform", platform);
        params.put("nickName", nickName);
        params.put("phone", phone);
        params.put("breedingType", breedingType);

        // 分页参数
        if (pageNum != null && pageSize != null) {
            int offset = (pageNum - 1) * pageSize;
            params.put("offset", offset);
            params.put("pageSize", pageSize);
        }

        List<User> list = userMapper.findList(params);
        Long total = userMapper.countUser(params);

        // 不返回密码
        list.forEach(user -> user.setPassword(null));

        return PageResult.of(list, total, pageNum, pageSize);
    }

    /**
     * 管理员更新指定用户信息
     */
    @Transactional
    public void updateUserByAdmin(Long userId, User updateData) {
        log.info("管理员更新用户信息，用户ID: {}", userId);
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (updateData.getNikeName() != null) user.setNikeName(updateData.getNikeName());
        if (updateData.getAddress() != null) user.setAddress(updateData.getAddress());
        if (updateData.getIcon() != null) user.setIcon(updateData.getIcon());
        if (updateData.getBreedingType() != null) user.setBreedingType(updateData.getBreedingType());
        if (updateData.getRole() != null) user.setRole(updateData.getRole());
        if (updateData.getPassword() != null) user.setPassword(updateData.getPassword());
        int result = userMapper.updateUser(user);
        log.info("管理员更新成功，影响行数: {}", result);
    }

    /**
     * 管理员删除用户
     */
    @Transactional
    public void deleteUserById(Long userId) {
        int result = userMapper.deleteById(userId);
        log.info("管理员删除用户，用户ID: {}, 影响行数: {}", userId, result);
    }
}
