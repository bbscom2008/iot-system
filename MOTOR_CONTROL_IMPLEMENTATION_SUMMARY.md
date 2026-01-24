# IoT 养殖场智能硬件控制系统 - 电机控制功能实现总结

## 功能概述

本次实现为IoT养殖场智能硬件控制系统添加了**电机延时控制管理功能**，包括：

1. ✅ **RabbitMQ延时消息队列** - 用于发送延时电机控制命令
2. ✅ **电机控制规则引擎** - 根据传感器数据和配置自动管理电机状态
3. ✅ **灵活的延迟时间控制** - 每个电机可设置不同的延迟时间
4. ✅ **多种控制模式支持** - 温度控制、循环控制、湿度控制、气体控制、定时控制
5. ✅ **MQTT消息集成** - 接收MQTT消息后自动触发电机控制规则

## 文件清单

### 新建文件

#### 1. **RabbitMQ配置类**
- 文件：`RabbitMqConfig.java`
- 位置：`src/main/java/com/example/demo/config/`
- 功能：
  - 配置延时消息交换机（使用x-delayed-message类型）
  - 配置实际处理交换机
  - 定义队列和绑定关系
  - 支持RabbitMQ延迟消息插件

#### 2. **电机控制消息DTO**
- 文件：`MotorControlMessage.java`
- 位置：`src/main/java/com/example/demo/dto/`
- 功能：
  - 定义电机控制消息结构
  - 包含电机ID、状态、控制模式、延迟时间等字段
  - 用于RabbitMQ消息序列化

#### 3. **电机控制消息生产者**
- 文件：`MotorControlProducerService.java`
- 位置：`src/main/java/com/example/demo/service/`
- 功能：
  - 发送延时电机控制消息到RabbitMQ延时队列
  - 发送无延时的电机控制消息到处理队列
  - 设置消息延迟时间（x-delay头部）

#### 4. **电机控制消息消费者**
- 文件：`MotorControlConsumerService.java`
- 位置：`src/main/java/com/example/demo/service/`
- 功能：
  - 监听RabbitMQ电机控制队列
  - 处理延时后到达的电机控制消息
  - 更新电机运行状态

#### 5. **电机控制规则引擎**
- 文件：`MotorControlRuleEngineService.java`
- 位置：`src/main/java/com/example/demo/service/`
- 功能：
  - 根据motor_fan表的配置和传感器数据进行电机控制
  - 支持5种控制模式：
    - **温度控制**：根据当前温度决定开/关
    - **循环控制**：定时运行/暂停
    - **湿度控制**：根据当前湿度决定开/关
    - **气体控制**：根据气体浓度决定开/关
    - **定时控制**：按时间表决定开/关
  - 检查自动模式（auto_mode）确定是否应用控制规则

#### 6. **RabbitMQ延迟插件安装指南**
- 文件：`RABBITMQ_DELAYED_PLUGIN_SETUP.md`
- 位置：项目根目录
- 功能：
  - 详细的RabbitMQ延迟插件安装步骤
  - 支持Linux、Docker、Windows系统
  - 常见问题解决方案
  - 性能考虑建议

### 修改的文件

#### 1. **pom.xml**
- 添加RabbitMQ依赖：`spring-boot-starter-amqp`

#### 2. **application.properties**
- 添加RabbitMQ配置：
  ```properties
  spring.rabbitmq.host=192.168.56.128
  spring.rabbitmq.port=5672
  spring.rabbitmq.username=guest
  spring.rabbitmq.password=guest
  spring.rabbitmq.virtual-host=/
  rabbitmq.motor.delay-time=5000
  ```

#### 3. **MqttService.java**
- 添加`MotorControlRuleEngineService`依赖
- 在`messageArrived()`方法中调用`processMotorControlRules()`
- 实现`processMotorControlRules()`方法处理所有电机的控制规则
- 实现`extractSensorValue()`方法提取传感器值
- 实现`getSensorValueById()`方法获取指定传感器值

## 工作流程

### 1. MQTT消息到达
```
温控仪 → MQTT消息 → IoT服务器
```

### 2. 消息解析与数据更新
```
MQTT消息解析 → 更新传感器值 → 更新电机状态 → 更新变频电机值
```

### 3. 电机控制规则处理
```
获取设备的所有电机配置
    ↓
对每个电机：
    ├─ 检查 auto_mode（自动/手动）
    ├─ 如果自动模式，按 control_mode 处理：
    │  ├─ 1: 温度控制 → 与温度阈值比较
    │  ├─ 2: 循环控制 → 按时间周期运行
    │  ├─ 3: 湿度控制 → 与湿度阈值比较
    │  ├─ 4: 气体控制 → 与气体阈值比较
    │  └─ 5: 定时控制 → 检查时间表
    └─ 发送控制消息到RabbitMQ
```

### 4. RabbitMQ处理
```
发送电机控制消息
    ├─ 如果有延迟时间：
    │  ├─ 发送到延时队列
    │  ├─ 设置 x-delay 头部
    │  ├─ 等待延迟时间
    │  └─ 路由到处理队列
    └─ 如果无延迟：
       └─ 直接发送到处理队列
```

### 5. 消息消费与执行
```
消费者接收消息
    ↓
更新电机运行状态到数据库
    ↓
电机执行对应的开/关动作
```

## 数据库表关系

### motor_fan 表的关键字段

| 字段 | 说明 | 用途 |
|------|------|------|
| `id` | 电机ID | 唯一标识 |
| `device_id` | 设备ID | 关联温控仪 |
| `device_num` | 设备编号 | MQTT主题识别 |
| `auto_mode` | 自动模式 | 1=自动, 2=开, 3=关 |
| `control_mode` | 控制模式 | 1-5对应不同控制方式 |
| `probe_sensor_id` | 探头传感器ID | 选择使用哪个传感器 |
| `temp_upper/lower` | 温度上下限 | 温度控制模式使用 |
| `humidity_upper/lower` | 湿度上下限 | 湿度控制模式使用 |
| `gas_upper/lower` | 气体上下限 | 气体控制模式使用 |
| `timer1_enabled/start_time/end_time` | 定时1配置 | 定时控制模式使用 |
| `timer2_enabled/start_time/end_time` | 定时2配置 | 定时控制模式使用 |
| `timer3_enabled/start_time/end_time` | 定时3配置 | 定时控制模式使用 |

## 配置示例

### 示例1：温度控制
```sql
UPDATE motor_fan 
SET 
    auto_mode = 1,          -- 自动模式
    control_mode = 1,       -- 温度控制
    probe_sensor_id = 1,    -- 使用第一个温度传感器
    temp_upper = 38.00,     -- 温度上限（启动温度）
    temp_lower = 30.00      -- 温度下限（停止温度）
WHERE id = 95;              -- 设备d002的风机1
```

### 示例2：定时控制
```sql
UPDATE motor_fan 
SET 
    auto_mode = 1,          -- 自动模式
    control_mode = 5,       -- 定时控制
    timer1_enabled = 1,     -- 启用定时1
    timer1_start_time = '08:00',
    timer1_end_time = '17:00',
    timer2_enabled = 1,     -- 启用定时2
    timer2_start_time = '20:00',
    timer2_end_time = '06:00'  -- 跨越午夜
WHERE id = 96;              -- 设备d002的风机2
```

### 示例3：手动控制
```sql
UPDATE motor_fan 
SET 
    auto_mode = 2           -- 手动模式（始终开）
WHERE id = 97;              -- 设备d002的风机3

-- 或
UPDATE motor_fan 
SET 
    auto_mode = 3           -- 手动模式（始终关）
WHERE id = 97;
```

## 部署步骤

### 1. 安装RabbitMQ延迟插件
参考 `RABBITMQ_DELAYED_PLUGIN_SETUP.md` 文件

### 2. 更新依赖
```bash
mvn clean install
```

### 3. 配置application.properties
确保包含以下配置：
```properties
spring.rabbitmq.host=192.168.56.128
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/
```

### 4. 启动应用
```bash
java -jar demo-0.0.1-SNAPSHOT.jar
```

### 5. 验证功能
- 发送MQTT消息
- 观察RabbitMQ队列中是否有消息
- 检查数据库中电机状态是否更新
- 查看应用日志

## 监控和调试

### RabbitMQ管理界面
访问 `http://localhost:15672`
- 用户名：guest
- 密码：guest

### 队列状态检查
1. 进入 "Queues" 标签
2. 查看以下队列：
   - `motor.control.delay.queue` - 延时队列
   - `motor.control.queue` - 处理队列

### 日志查看
```bash
# 查看应用日志
tail -f logs/application.log

# 关键日志关键词：
# - "发送延时电机控制消息"
# - "处理电机控制消息"
# - "电机控制消息处理成功"
# - "处理电机控制规则错误"
```

## 常见场景

### 场景1：温度自动控制
```
当前温度 < 30°C → 电机关闭
当前温度 >= 38°C → 电机启动
30°C ≤ 当前温度 < 38°C → 保持当前状态
```

### 场景2：定时通风
```
08:00 - 17:00 → 电机启动
17:00 - 08:00 → 电机关闭
其他时间 → 电机关闭
```

### 场景3：循环运行
```
运行 5 分钟 → 暂停 2 分钟 → 运行 5 分钟 → ...
（定时控制模式的扩展实现）
```

## 扩展建议

### 1. 增加延迟时间配置
在motor_fan表中添加：
```sql
ALTER TABLE motor_fan 
ADD COLUMN delay_off_time INT DEFAULT 0 COMMENT '关闭延迟时间（秒）';

ALTER TABLE motor_fan 
ADD COLUMN delay_on_time INT DEFAULT 0 COMMENT '启动延迟时间（秒）';
```

### 2. 支持状态转换触发
根据电机从不同状态转换到新状态时应用不同的延迟：
- 从关到开：可能需要缓冲时间
- 从开到关：可能需要冷却时间

### 3. 历史记录存储
创建表记录所有电机控制事件：
```sql
CREATE TABLE motor_control_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    motor_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    old_state TINYINT,
    new_state TINYINT,
    control_reason VARCHAR(100),
    delay_time BIGINT,
    executed_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 4. 告警规则
添加异常电机状态告警：
- 电机应该启动但未启动
- 电机运行异常长时间
- 控制命令执行失败

## 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.6 | 应用框架 |
| RabbitMQ | 3.10+ | 消息队列 |
| MyBatis | 3.0.3 | 数据访问 |
| Lombok | 最新 | 代码简化 |
| Jackson | 最新 | JSON处理 |

## 注意事项

1. ⚠️ **RabbitMQ延迟插件必须安装** - 否则x-delayed-message交换机无法使用
2. ⚠️ **延迟时间设置** - 建议不超过1小时以获得最佳性能
3. ⚠️ **数据库一致性** - 确保motor_fan表配置正确
4. ⚠️ **网络稳定性** - MQTT和RabbitMQ连接需要稳定的网络
5. ⚠️ **时区设置** - 定时控制模式依赖系统时区

## 测试用例

### 测试1：温度触发控制
1. 在motor_fan表中配置电机为温度控制模式
2. 发送MQTT消息，温度值低于下限
3. 验证电机状态为关闭（is_running = 0）
4. 发送MQTT消息，温度值高于上限
5. 验证电机状态为启动（is_running = 1）

### 测试2：延迟消息
1. 创建延迟为10秒的电机控制消息
2. 立即检查处理队列 - 应该为空
3. 等待10秒后检查 - 消息应该已被消费
4. 验证电机状态已更新

### 测试3：多电机独立控制
1. 为同一设备的多个电机配置不同的控制模式
2. 发送MQTT消息包含所有传感器数据
3. 验证每个电机根据其配置进行独立控制

## 相关文档参考

- [RabbitMQ 官方文档](https://www.rabbitmq.com/)
- [Spring AMQP 文档](https://spring.io/projects/spring-amqp)
- [MQTT 协议规范](http://mqtt.org/)

## 支持和反馈

如有问题或需要进一步优化，请参考以下资源：
1. 查看RABBITMQ_DELAYED_PLUGIN_SETUP.md了解RabbitMQ配置
2. 检查应用日志获取错误信息
3. 验证数据库表配置是否正确
4. 确认RabbitMQ延迟插件已正确安装
