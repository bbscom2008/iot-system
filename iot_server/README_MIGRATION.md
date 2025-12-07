# 数据库迁移说明

## 已完成的修改

### 1. 数据库迁移脚本
已创建：`update_role_type_platform.sql`

该脚本执行以下操作：
- 将 `role_type` 表重命名为 `user_role`
- 添加 `platform` 字段用于区分手机端(mobile)和PC端(web)
- 更新现有数据标记为 mobile 平台
- 插入PC端角色：管理员(10)、员工(11)
- 添加复合唯一索引 `uk_dict_value_platform`

### 2. Java 后端代码修改

#### 实体类
- ✅ `RoleType.java` → `UserRole.java` (已重命名)
- ✅ 添加 `platform` 字段

#### Mapper 接口
- ✅ `RoleTypeMapper.java` → `UserRoleMapper.java` (已重命名)
- ✅ 添加 `findByPlatform(String platform)` 方法

#### MyBatis XML
- ✅ `RoleTypeMapper.xml` → `UserRoleMapper.xml` (已重命名)
- ✅ 更新 namespace 和 resultMap
- ✅ 更新表名为 `user_role`
- ✅ 添加 platform 字段映射
- ✅ 添加 `findByPlatform` 查询语句

#### Service 层
- ✅ `RoleTypeService.java` → `UserRoleService.java` (已重命名)
- ✅ 添加 `findByPlatform(String platform)` 方法

#### Controller 层
- ✅ 更新 `DictDataController.java`
- ✅ 使用 `UserRoleService` 替代 `RoleTypeService`
- ✅ 添加新接口：`GET /api/dict-data/role-type/list/{platform}`

## 需要执行的操作

### 方法 1：使用数据库管理工具（推荐）

使用 Navicat、DBeaver、MySQL Workbench 等工具：

1. 连接到 `iot_system` 数据库
2. 打开并执行 `update_role_type_platform.sql` 文件
3. 验证表结构和数据

### 方法 2：使用 MySQL 命令行

```bash
mysql -u root -p iot_system < update_role_type_platform.sql
```

### 方法 3：使用 Spring Boot 自动迁移

如果系统已安装 Java 17 JDK：

1. 启用迁移工具：
   ```java
   // 在 DatabaseMigration.java 中取消注释 @Component
   ```

2. 运行应用：
   ```bash
   ./mvnw spring-boot:run
   ```

3. 迁移完成后，注释掉 @Component：
   ```java
   // @Component
   ```

## 验证迁移

执行以下 SQL 验证迁移结果：

```sql
-- 查看表结构
DESC user_role;

-- 查看所有角色数据
SELECT * FROM user_role ORDER BY platform, dict_sort;

-- 应该看到：
-- mobile 平台：老板(0)、饲养员(1)、其他(2)
-- web 平台：管理员(10)、员工(11)
```

## API 使用示例

### 获取所有角色
```
GET /api/dict-data/role-type/list
```

### 获取手机端角色
```
GET /api/dict-data/role-type/list/mobile
```

### 获取PC端角色
```
GET /api/dict-data/role-type/list/web
```

## 注意事项

1. ✅ 手机端显示逻辑不受影响 - 通过 `platform=mobile` 过滤
2. ✅ PC端可以使用自己的角色 - 通过 `platform=web` 过滤
3. ✅ 代码向后兼容 - 保留了 `findAll()` 方法
4. ✅ 数据完整性 - 添加了复合唯一索引防止重复

## 后续开发

在前端调用 API 时，使用带平台参数的接口：

```javascript
// PC 端 - iot-admin-ui
getRoleTypes() {
  return request({
    url: '/api/dict-data/role-type/list/web',
    method: 'get'
  })
}

// 手机端 - dspace (如需修改)
getRoleTypes() {
  return request({
    url: '/api/dict-data/role-type/list/mobile',
    method: 'get'
  })
}
```
