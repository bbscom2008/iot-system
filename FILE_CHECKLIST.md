# 文件修改清单

## 📋 项目结构概览

```
iot-system/
├── QUICK_START_GUIDE.md                                          [新增] 快速开始指南
├── MOTOR_CONTROL_IMPLEMENTATION_SUMMARY.md                      [新增] 实现总结文档
├── RABBITMQ_DELAYED_PLUGIN_SETUP.md                            [新增] RabbitMQ插件安装指南
├── pom.xml                                                       [修改] 添加RabbitMQ依赖
├── iot_server/
│   ├── pom.xml                                                   [修改] 添加RabbitMQ依赖
│   ├── src/main/resources/
│   │   └── application.properties                                [修改] 添加RabbitMQ配置
│   └── src/main/java/com/example/demo/
│       ├── config/
│       │   └── RabbitMqConfig.java                              [新增] RabbitMQ配置类
│       ├── dto/
│       │   └── MotorControlMessage.java                         [新增] 电机控制消息DTO
│       └── service/
│           ├── MqttService.java                                  [修改] 添加电机控制规则处理
│           ├── MotorControlProducerService.java                 [新增] 消息生产者
│           ├── MotorControlConsumerService.java                 [新增] 消息消费者
│           └── MotorControlRuleEngineService.java               [新增] 控制规则引擎
└── dspace/                                                        [不变] 前端代码
```

## 📁 新增文件详表

### 1. 配置文件

#### RabbitMqConfig.java
- **路径**：`iot_server/src/main/java/com/example/demo/config/RabbitMqConfig.java`
- **行数**：约80行
- **功能**：
  - 配置延时消息交换机（x-delayed-message）
  - 配置实际处理交换机（direct）
  - 定义队列和绑定关系
- **关键方法**：
  - `motorControlDelayExchange()` - 创建延时交换机
  - `motorControlDelayQueue()` - 创建延时队列
  - `motorControlExchange()` - 创建处理交换机
  - `motorControlQueue()` - 创建处理队列
- **依赖**：Spring AMQP

### 2. DTO（数据传输对象）

#### MotorControlMessage.java
- **路径**：`iot_server/src/main/java/com/example/demo/dto/MotorControlMessage.java`
- **行数**：约50行
- **功能**：定义电机控制消息结构
- **核心字段**：
  - `motorId` - 电机ID
  - `state` - 电机状态（0=停止, 1=运行）
  - `controlMode` - 控制模式（1-5）
  - `autoMode` - 自动模式（1=自动, 2=开, 3=关）
  - `delayTime` - 延迟时间（毫秒）
- **实现**：Serializable，用于RabbitMQ序列化

### 3. 业务服务

#### MotorControlProducerService.java
- **路径**：`iot_server/src/main/java/com/example/demo/service/MotorControlProducerService.java`
- **行数**：约60行
- **功能**：发送电机控制消息到RabbitMQ
- **核心方法**：
  - `sendMotorControlDelayMessage()` - 发送延时消息
  - `sendMotorControlMessage()` - 发送无延时消息
- **依赖**：
  - RabbitTemplate
  - RabbitMqConfig

#### MotorControlConsumerService.java
- **路径**：`iot_server/src/main/java/com/example/demo/service/MotorControlConsumerService.java`
- **行数**：约45行
- **功能**：消费电机控制消息并更新数据库
- **核心方法**：
  - `processMotorControlMessage()` - 处理消息并更新电机状态
- **注解**：@RabbitListener
- **依赖**：
  - MotorFanService
  - RabbitMqConfig

#### MotorControlRuleEngineService.java
- **路径**：`iot_server/src/main/java/com/example/demo/service/MotorControlRuleEngineService.java`
- **行数**：约400行
- **功能**：实现电机控制规则逻辑
- **核心方法**：
  - `processMotorControl()` - 主要处理方法
  - `processTemperatureControl()` - 温度控制
  - `processHumidityControl()` - 湿度控制
  - `processGasControl()` - 气体控制
  - `processCycleControl()` - 循环控制
  - `processTimerControl()` - 定时控制
  - `isTimeInRange()` - 时间范围检查
  - `sendMotorControlMessage()` - 发送控制消息
- **控制模式支持**：
  1. 温度控制
  2. 循环控制
  3. 湿度控制
  4. 气体控制
  5. 定时控制
- **依赖**：
  - MotorFanService
  - SensorService
  - MotorControlProducerService

## 📝 修改文件详表

### 1. Maven配置

#### pom.xml
- **路径**：`iot_server/pom.xml`
- **变更**：添加RabbitMQ依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```
- **位置**：在`spring-boot-starter-test`后

### 2. 应用配置

#### application.properties
- **路径**：`iot_server/src/main/resources/application.properties`
- **新增配置**：
```properties
# RabbitMQ配置
spring.rabbitmq.host=192.168.56.128
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/

# 电机控制延迟时间配置（毫秒）
rabbitmq.motor.delay-time=5000
```

### 3. 核心服务类

#### MqttService.java
- **路径**：`iot_server/src/main/java/com/example/demo/service/MqttService.java`
- **修改内容**：
  1. **新增依赖注入**：`MotorControlRuleEngineService motorControlRuleEngineService`
  2. **新增方法调用**：在`messageArrived()`中添加
     ```java
     // 应用电机控制规则
     processMotorControlRules(device.getId(), node, sensorValues);
     ```
  3. **新增私有方法**：
     - `processMotorControlRules()` - 处理所有电机的控制规则
     - `extractSensorValue()` - 从列表中提取传感器值
     - `getSensorValueById()` - 根据ID获取传感器值
- **修改行数**：约50行新增代码

## 🎯 核心工作流程修改

### MQTT消息处理流程
```
原流程：
MQTT消息 → 解析 → 更新传感器 → 更新电机状态 → 更新变频电机 → 发布前端消息

新流程：
MQTT消息 → 解析 → 更新传感器 → 更新电机状态 → 更新变频电机 
         ↓
    应用控制规则
         ↓
    获取设备所有电机配置
         ↓
    检查auto_mode（自动/手动）
         ↓
    按control_mode处理（温度/循环/湿度/气体/定时）
         ↓
    发送消息到RabbitMQ
         ↓
    (可选)延迟后执行
         ↓
    更新电机状态 → 发布前端消息
```

## 📊 代码统计

| 项目 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 新增配置 | RabbitMqConfig.java | ~80 | RabbitMQ配置 |
| 新增DTO | MotorControlMessage.java | ~50 | 消息定义 |
| 新增生产者 | MotorControlProducerService.java | ~60 | 消息发送 |
| 新增消费者 | MotorControlConsumerService.java | ~45 | 消息处理 |
| 新增引擎 | MotorControlRuleEngineService.java | ~400 | 控制逻辑 |
| 修改MQTT | MqttService.java | +50 | 集成规则引擎 |
| 修改配置 | application.properties | +6 | RabbitMQ配置 |
| 修改依赖 | pom.xml | +4 | RabbitMQ依赖 |
| **总计** | **8个文件** | **~695** | - |

## 📚 文档文件

| 文件名 | 说明 | 用途 |
|--------|------|------|
| QUICK_START_GUIDE.md | 5分钟快速开始 | 快速上手 |
| MOTOR_CONTROL_IMPLEMENTATION_SUMMARY.md | 详细实现总结 | 深入理解 |
| RABBITMQ_DELAYED_PLUGIN_SETUP.md | RabbitMQ插件安装 | 部署配置 |
| FILE_CHECKLIST.md | 本文件 | 项目管理 |

## 🔧 依赖关系图

```
MqttService
    ├── MotorControlRuleEngineService
    │   ├── MotorFanService
    │   ├── SensorService
    │   └── MotorControlProducerService
    │       └── RabbitTemplate
    │           └── RabbitMqConfig
    │
    ├── MotorControlConsumerService
    │   └── MotorFanService
    │       └── RabbitMqConfig
    │
    └── 其他已有服务
        ├── DeviceService
        ├── SensorService
        ├── MotorFanService
        ├── FrequencyMotorService
        └── MqttMessageDataService
```

## ✅ 变更验证清单

部署前请确认以下项目：

- [ ] pom.xml已添加spring-boot-starter-amqp依赖
- [ ] application.properties已配置RabbitMQ连接信息
- [ ] RabbitMqConfig.java已创建
- [ ] MotorControlMessage.java已创建
- [ ] MotorControlProducerService.java已创建
- [ ] MotorControlConsumerService.java已创建
- [ ] MotorControlRuleEngineService.java已创建
- [ ] MqttService.java已更新（添加规则处理）
- [ ] RabbitMQ服务已启动
- [ ] RabbitMQ延迟插件已安装
- [ ] 应用已成功编译
- [ ] 应用已成功启动
- [ ] MQTT消息可以正常处理
- [ ] RabbitMQ队列中可以看到消息

## 🚀 部署步骤

1. **备份现有代码**
   ```bash
   git commit -m "backup before motor control update"
   ```

2. **更新依赖**
   ```bash
   cd iot_server
   mvn clean install
   ```

3. **验证编译**
   ```bash
   mvn compile
   ```

4. **启动应用**
   ```bash
   java -jar target/demo-0.0.1-SNAPSHOT.jar
   ```

5. **验证功能**
   - 发送MQTT消息
   - 检查RabbitMQ队列
   - 验证数据库更新

## 📖 相关文档导航

```
快速开始 → QUICK_START_GUIDE.md
深入理解 → MOTOR_CONTROL_IMPLEMENTATION_SUMMARY.md
插件安装 → RABBITMQ_DELAYED_PLUGIN_SETUP.md
文件清单 → FILE_CHECKLIST.md（本文件）
```

## 🐛 常见问题

### Q: 新增文件放在哪里？
A: 严格按照Java规范：
- Config类：`src/main/java/com/example/demo/config/`
- DTO类：`src/main/java/com/example/demo/dto/`
- Service类：`src/main/java/com/example/demo/service/`

### Q: 是否需要修改数据库？
A: 不需要。系统使用现有的motor_fan表，但需要正确配置：
- auto_mode（自动模式）
- control_mode（控制模式）
- 相应的阈值和定时配置

### Q: RabbitMQ延迟插件可选吗？
A: 不可选。如果不安装延迟插件：
- x-delayed-message交换机将无法创建
- 应用启动时会报错
- 参考RABBITMQ_DELAYED_PLUGIN_SETUP.md安装

### Q: 如何回滚这些变更？
A: 
1. 删除新增的5个Java类文件
2. 恢复MqttService.java（移除processMotorControlRules相关代码）
3. 恢复application.properties（移除RabbitMQ配置）
4. 恢复pom.xml（移除amqp依赖）
5. `mvn clean install`重新编译

## 📞 支持

遇到问题？请参考：
1. RABBITMQ_DELAYED_PLUGIN_SETUP.md - RabbitMQ相关
2. MOTOR_CONTROL_IMPLEMENTATION_SUMMARY.md - 功能相关
3. QUICK_START_GUIDE.md - 使用相关
4. 应用日志 - 错误信息
5. RabbitMQ管理界面 - 队列状态

---

**最后更新**：2026年1月24日  
**版本**：1.0  
**状态**：✅ 完整实现
