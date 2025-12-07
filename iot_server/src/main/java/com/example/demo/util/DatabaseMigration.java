package com.example.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;

/**
 * 数据库迁移工具 - 仅执行一次后需注释掉 @Component
 */
//@Component  // 执行完成后注释掉此行
public class DatabaseMigration implements CommandLineRunner {

//    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始执行数据库迁移...");
        
        String sqlFile = "update_role_type_platform.sql";
        
        try {
            // 1. 重命名表
            jdbcTemplate.execute("RENAME TABLE `role_type` TO `user_role`");
            System.out.println("✓ 表重命名成功: role_type -> user_role");
            
            // 2. 添加 platform 字段
            jdbcTemplate.execute(
                "ALTER TABLE `user_role` " +
                "ADD COLUMN `platform` VARCHAR(20) NOT NULL DEFAULT 'mobile' " +
                "COMMENT '平台类型：mobile-手机端，web-PC端' AFTER `role_id`"
            );
            System.out.println("✓ 添加 platform 字段成功");
            
            // 3. 更新现有数据
            jdbcTemplate.execute("UPDATE `user_role` SET `platform` = 'mobile'");
            System.out.println("✓ 更新现有数据为 mobile 平台");
            
            // 4. 插入 PC 端角色
            jdbcTemplate.execute(
                "INSERT INTO `user_role` (`label`, `role_id`, `platform`, `sort`, `remark`, `created_at`) VALUES " +
                "('管理员', '10', 'web', 1, 'PC端管理员', NOW()), " +
                "('员工', '11', 'web', 2, 'PC端员工', NOW())"
            );
            System.out.println("✓ 插入 PC 端角色成功");
            
            // 5. 添加复合唯一索引
            jdbcTemplate.execute(
                "ALTER TABLE `user_role` " +
                "ADD UNIQUE INDEX `uk_role_id_platform` (`role_id`, `platform`)"
            );
            System.out.println("✓ 添加复合唯一索引成功");
            
            System.out.println("====================================");
            System.out.println("数据库迁移完成！");
            System.out.println("请注释掉 DatabaseMigration 类的 @Component 注解");
            System.out.println("====================================");
            
        } catch (Exception e) {
            System.err.println("数据库迁移失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
