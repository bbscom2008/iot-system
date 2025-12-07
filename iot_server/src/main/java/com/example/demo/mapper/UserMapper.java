package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据手机号查询用户
     */
    User findByPhone(@Param("phone") String phone);

    /**
     * 根据ID查询用户
     */
    User findById(@Param("id") Long id);

    /**
     * 注册用户
     */
    int insert(User user);

    /**
     * 更新用户信息
     */
    int updateUser(User user);

    /**
     * 查询用户列表
     */
    List<User> findList(Map<String, Object> params);

    /**
     * 统计用户总数
     */
    Long countUser(Map<String, Object> params);

    /**
     * 删除用户
     */
    int deleteById(@Param("id") Long id);
}
