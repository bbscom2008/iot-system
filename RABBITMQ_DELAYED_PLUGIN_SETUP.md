# RabbitMQ 延迟消息插件安装指南

## 概述
本项目使用 RabbitMQ 延迟消息插件 (`rabbitmq-delayed-message-exchange`) 来实现灵活的电机控制延迟。每个电机可以有不同的延迟开关时间。

## 插件特性
- 支持动态延迟时间（不受全局TTL限制）
- 每条消息可以独立设置延迟时间（x-delay 头部）
- 延迟时间范围：0 ~ 2147483647 毫秒（约24.8天）
- 适合需要灵活延迟控制的场景

## 安装步骤

### 1. 下载延迟插件

访问 RabbitMQ 官方 GitHub 下载对应版本的插件：
```
https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases
```

根据你的 RabbitMQ 版本下载相应的 `.ez` 文件。例如：
- RabbitMQ 3.8.x: `rabbitmq_delayed_message_exchange-3.8.x.ez`
- RabbitMQ 3.9.x: `rabbitmq_delayed_message_exchange-3.9.x.ez`
- RabbitMQ 3.10.x: `rabbitmq_delayed_message_exchange-3.10.x.ez`

### 2. Linux 系统安装

#### 2.1 复制插件文件
```bash
# 将下载的 .ez 文件复制到 RabbitMQ 插件目录
cp rabbitmq_delayed_message_exchange-3.x.x.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.x.x/plugins/

# 或者如果是 Rocky Linux 或 CentOS
sudo cp rabbitmq_delayed_message_exchange-3.x.x.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.x.x/plugins/
```

#### 2.2 启用插件
```bash
# 启用插件
rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 验证插件是否启用
rabbitmq-plugins list

# 输出应该包含：
# [E*] rabbitmq_delayed_message_exchange
```

#### 2.3 重启 RabbitMQ
```bash
# 重启 RabbitMQ 服务
sudo systemctl restart rabbitmq-server

# 或者
systemctl restart rabbitmq-server
```

### 3. Docker 安装

如果你使用 Docker 运行 RabbitMQ：

```bash
# 进入 RabbitMQ 容器
docker exec -it <container_id> bash

# 下载并启用插件
rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 或者在 Dockerfile 中安装
FROM rabbitmq:3.10-management

RUN rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

### 4. Windows 安装

#### 4.1 复制插件文件
```
将下载的 .ez 文件复制到：
C:\Program Files\RabbitMQ Server\rabbitmq_server-3.x.x\plugins\
```

#### 4.2 启用插件
```powershell
# 以管理员身份打开 PowerShell，进入 RabbitMQ sbin 目录
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.x.x\sbin"

# 启用插件
.\rabbitmq-plugins.bat enable rabbitmq_delayed_message_exchange

# 验证
.\rabbitmq-plugins.bat list
```

#### 4.3 重启 RabbitMQ
```powershell
# 重启服务
Restart-Service RabbitMQ
```

## 验证安装

### 1. 检查插件状态
```bash
rabbitmq-plugins list | grep delayed
```

输出应该显示：
```
[E*] rabbitmq_delayed_message_exchange
```

其中 `[E*]` 表示已启用（E = enabled，* = running）。

### 2. 通过 RabbitMQ 管理界面验证

访问 RabbitMQ 管理界面（默认 http://localhost:15672）：
1. 用户名/密码：guest/guest
2. 进入 "Exchanges" 标签
3. 创建新交换机时，应该能看到 `x-delayed-message` 类型选项

### 3. 查看 RabbitMQ 日志

```bash
# 查看 RabbitMQ 启动日志
tail -f /var/log/rabbitmq/rabbit@hostname.log

# 日志中应该包含：
# Plugin rabbitmq_delayed_message_exchange loaded
```

## 应用中的使用

### 配置说明

项目已在 `RabbitMqConfig.java` 中配置了使用延迟插件的交换机：

```java
@Bean
public CustomExchange motorControlDelayExchange() {
    return new CustomExchange(
        MOTOR_CONTROL_DELAY_EXCHANGE, 
        "x-delayed-message",  // 使用延迟消息交换机类型
        true, 
        false,
        java.util.Collections.singletonMap("x-delayed-type", "direct")
    );
}
```

### 发送延迟消息

在 `MotorControlProducerService.java` 中，可以灵活设置每条消息的延迟时间：

```java
// 创建消息
MotorControlMessage message = MotorControlMessage.builder()
    .motorId(motorId)
    .state(1)           // 1 = 开, 0 = 关
    .delayTime(5000L)   // 延迟 5000 毫秒（5秒）
    .timestamp(System.currentTimeMillis())
    .build();

// 发送延迟消息
motorControlProducerService.sendMotorControlDelayMessage(message);
```

### 配置文件

在 `application.properties` 中配置 RabbitMQ：

```properties
# RabbitMQ 配置
spring.rabbitmq.host=192.168.56.128
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/

# 电机控制延迟时间配置（毫秒）- 默认值，消息中的 delayTime 会覆盖此值
rabbitmq.motor.delay-time=5000
```

## 常见问题

### 问题 1：插件加载失败
**症状**：RabbitMQ 启动失败，日志中显示 "Plugin could not be loaded"

**解决方案**：
1. 确保下载的插件版本与 RabbitMQ 版本匹配
2. 检查插件文件权限
3. 清除 RabbitMQ Mnesia 数据库重新启动：
   ```bash
   sudo rm -rf /var/lib/rabbitmq/mnesia/*
   sudo systemctl restart rabbitmq-server
   ```

### 问题 2：x-delayed-message 交换机类型未找到
**症状**：创建交换机时看不到 `x-delayed-message` 类型

**解决方案**：
1. 确认插件已启用：`rabbitmq-plugins list`
2. 确认插件状态为 `[E*]`（enabled and running）
3. 重启 RabbitMQ 服务
4. 刷新浏览器管理界面

### 问题 3：消息延迟不生效
**症状**：设置了 x-delay 头部，但消息立即到达

**解决方案**：
1. 检查消息头设置：
   ```java
   message.getMessageProperties().setHeader("x-delay", delayTime);
   ```
2. 确认交换机类型为 `x-delayed-message`
3. 确认消息已发送到正确的交换机
4. 检查 RabbitMQ 日志获取更多信息

### 问题 4：插件升级 RabbitMQ 后失效
**症状**：升级 RabbitMQ 后，延迟插件不工作

**解决方案**：
1. 重新下载对应 RabbitMQ 版本的延迟插件
2. 卸载旧版本插件：
   ```bash
   rabbitmq-plugins disable rabbitmq_delayed_message_exchange
   ```
3. 替换插件文件
4. 启用新版本插件
5. 重启 RabbitMQ

## 性能考虑

- 延迟消息的处理开销随着延迟时间和消息数量增加而增加
- 建议延迟时间不超过 1 小时以获得最佳性能
- 对于大量延迟消息，考虑使用专门的消息队列服务（如 AWS SQS）

## 参考资源

- [RabbitMQ 延迟消息插件 GitHub](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange)
- [RabbitMQ 官方文档](https://www.rabbitmq.com/documentation.html)
- [RabbitMQ 插件管理](https://www.rabbitmq.com/plugins.html)

## 电机延迟控制工作流程

```
MQTT 消息到达
    ↓
解析电机数据和状态
    ↓
读取 motor_fan 表配置
    ↓
检查 auto_mode（自动/手动）
    ↓
如果自动模式，按 control_mode 进行控制
    ├─ 温度控制：根据当前温度决定开/关
    ├─ 循环控制：按时间周期循环
    ├─ 湿度控制：根据当前湿度决定开/关
    ├─ 气体控制：根据当前气体浓度决定开/关
    └─ 定时控制：按设定时间表决定开/关
    ↓
如果需要延迟执行，创建延迟消息（设置 delayTime）
    ↓
发送消息到 RabbitMQ 延迟交换机
    ↓
延迟插件根据 x-delay 头延迟消息
    ↓
延迟时间到期后，消息路由到处理队列
    ↓
消费者处理消息，更新电机状态
    ↓
电机执行对应的开/关动作
```

## 电机控制场景示例

### 场景1：温度控制延迟关闭
```
温度达到上限 → 电机启动冷却 → 温度降至下限 → 延迟 2 分钟后关闭电机
```

在代码中实现：
```java
// 温度超过上限时
if (currentTemp >= motorFan.getTempUpper()) {
    // 立即开启
    motorControlProducerService.sendMotorControlMessage(enableMessage);
}

// 温度回到下限时
else if (currentTemp <= motorFan.getTempLower()) {
    // 延迟 2 分钟后关闭，给温度一个缓冲期
    motorControlMessage.setDelayTime(120000L);  // 120秒
    motorControlProducerService.sendMotorControlDelayMessage(motorControlMessage);
}
```

### 场景2：多个电机不同的延迟时间
```
电机 1：温度控制，降温后延迟 1 分钟关闭
电机 2：湿度控制，除湿后延迟 3 分钟关闭
电机 3：气体控制，排气后立即关闭
```

每个电机可以有不同的延迟配置，完全由 `motor_fan` 表的配置决定。
