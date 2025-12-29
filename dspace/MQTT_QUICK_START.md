# MQTT 集成 - 快速开始

## 🚀 5分钟快速上手

### 步骤 1: 安装依赖

```bash
cd dspace
npm install
# 或使用 pnpm
pnpm install
```

### 步骤 2: 配置 MQTT Broker 地址

编辑 `src/utils/mqtt-config.js`:

```javascript
const MQTT_CONFIG = {
  dev: {
    broker: 'ws://192.168.56.128:8883',      // 改为你的 Broker 地址 (H5)
    brokerTcp: 'mqtt://192.168.56.128:1883', // 改为你的 Broker 地址 (小程序)
    clientId: 'dspace-' + Math.random().toString(36).substr(2, 9),
    username: '', // 如需认证，填写用户名
    password: '', // 如需认证，填写密码
  }
};
```

### 步骤 3: 在页面中使用

#### 方式 A: 在模板中显示 MQTT 状态

```vue
<template>
  <view>
    <!-- 导入示例组件 -->
    <mqtt-demo />
  </view>
</template>

<script>
import MqttDemo from '@/components/MqttDemo.vue'

export default {
  components: {
    MqttDemo
  }
}
</script>
```

#### 方式 B: 在组件中获取设备数据

```vue
<template>
  <view>
    <text v-if="isConnected">✓ 已连接</text>
    <text v-else>✗ 未连接</text>
    
    <view v-for="(data, id) in allDeviceData" :key="id">
      <text>{{ id }}: {{ data.temperature }}°C, {{ data.humidity }}%</text>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  computed: {
    ...mapGetters('mqtt', ['isConnected', 'getAllDeviceData']),
    allDeviceData() {
      return this.getAllDeviceData
    }
  }
}
</script>
```

#### 方式 C: 发送控制指令

```vue
<template>
  <view>
    <button @click="setTemperature">设置温度为 25°C</button>
  </view>
</template>

<script>
import { mapActions } from 'vuex'
import { DeviceControlCommand } from '@/utils/message-parser'

export default {
  methods: {
    ...mapActions('mqtt', ['publishMessage']),
    
    async setTemperature() {
      const cmd = DeviceControlCommand.createTemperatureControl('device001', 25);
      await this.publishMessage({
        deviceId: 'device001',
        action: cmd.action,
        payload: cmd.payload
      });
    }
  }
}
</script>
```

### 步骤 4: 运行项目

```bash
# 开发 H5
npm run dev:h5

# 开发微信小程序
npm run dev:mp-weixin

# 构建 H5
npm run build:h5

# 构建微信小程序
npm run build:mp-weixin
```

---

## 📁 文件结构

```
src/
├── utils/
│   ├── mqtt.js              ← MQTT 核心服务
│   ├── mqtt-config.js       ← MQTT 配置文件
│   └── message-parser.js    ← 消息解析和控制指令
├── store/
│   └── modules/
│       └── mqtt.js          ← Vuex 状态管理
├── components/
│   └── MqttDemo.vue         ← 示例组件
├── main.js                  ← 应用入口（MQTT 初始化）
└── App.vue
```

---

## 🔧 常见配置

### 改变 MQTT 主题

编辑 `src/utils/mqtt-config.js`:

```javascript
export const MQTT_TOPICS = {
  DEVICE_DATA: 'your/custom/topic/data',
  DEVICE_STATUS: 'your/custom/topic/status',
  // ...
};
```

### 禁用自动连接

编辑 `src/main.js`:

```javascript
const shouldInitMqtt = false; // 改为 false
```

### 增加日志输出

编辑 `src/utils/mqtt.js`，找到所有 `console.log` 调用，它们会输出详细的调试信息。

---

## 🧪 测试连接

### 使用 MQTT.js 浏览器工具

1. 打开 [MQTT.js HiveMQ WebSocket Client](https://www.hivemq.com/demos/websocket-client/)
2. 输入你的 Broker 地址
3. 订阅 `device/+/data` 主题
4. 你应该看到从 dspace 发送的消息

### 使用 mosquitto_sub 命令行工具

```bash
# 订阅设备数据
mosquitto_sub -h 192.168.56.128 -p 1883 -t "device/+/data"

# 订阅设备状态
mosquitto_sub -h 192.168.56.128 -p 1883 -t "device/+/status"
```

### 使用 mosquitto_pub 发送测试消息

```bash
# 发送设备数据
mosquitto_pub -h 192.168.56.128 -p 1883 -t "device/test/data" -m '{"sensorCode":"test","temperature":23.5,"humidity":45}'

# 发送设备状态
mosquitto_pub -h 192.168.56.128 -p 1883 -t "device/test/status" -m '{"sensorCode":"test","online":true,"signal":-45}'
```

---

## 🐛 调试技巧

### 1. 查看连接日志

打开浏览器开发者工具 (F12)，查看 Console 标签。你会看到：

```
[MQTT] Connecting to: ws://192.168.56.128:8883
[MQTT] Connected successfully
[MQTT] Received message on device/device001/data : {...}
```

### 2. 监视 Vuex 状态

在浏览器中安装 [Vue DevTools](https://devtools.vuejs.org/)，查看 Vuex 状态变化：

- `mqtt.isConnected` - 连接状态
- `mqtt.deviceDataMap` - 设备数据
- `mqtt.messageQueue` - 离线消息

### 3. 监听事件

在 `mounted()` 中添加事件监听：

```javascript
import mqttClient from '@/utils/mqtt'

export default {
  mounted() {
    mqttClient.on('message', ({ topic, message }) => {
      console.log('DEBUG: Message received', topic, message);
    });
  }
}
```

---

## ⚠️ 常见问题排查

| 问题 | 原因 | 解决方案 |
|------|------|--------|
| WebSocket is closed | 连接断开 | 检查 Broker 是否运行，IP 端口是否正确 |
| ECONNREFUSED | 无法连接 | 检查防火墙，确认 Broker 监听的地址和端口 |
| 小程序连接失败 | 域名白名单 | 在小程序后台添加 Broker 的域名或 IP 到白名单 |
| 消息接收不到 | 主题订阅错误 | 检查订阅的主题是否正确匹配 |
| 断线无法重连 | 重连配置 | 检查 `mqtt-config.js` 中的 `reconnectPeriod` |

---

## 📚 完整 API 速查表

### 连接管理

```javascript
// 连接
store.dispatch('mqtt/initMqtt')

// 断开
store.dispatch('mqtt/disconnectMqtt')

// 重连
store.dispatch('mqtt/reconnect')

// 获取状态
store.getters['mqtt/isConnected']
```

### 消息操作

```javascript
// 发布
store.dispatch('mqtt/publishMessage', {
  deviceId: 'device001',
  action: 'set_temperature',
  payload: { targetTemperature: 25 }
})

// 订阅设备
store.dispatch('mqtt/subscribeDevice', 'device001')

// 取消订阅
store.dispatch('mqtt/unsubscribeDevice', 'device001')
```

### 数据查询

```javascript
// 获取设备数据
store.getters['mqtt/getDeviceData']('device001')

// 获取所有设备数据
store.getters['mqtt/getAllDeviceData']

// 获取设备状态
store.getters['mqtt/getDeviceStatus']('device001')
```

### 控制指令

```javascript
import { DeviceControlCommand } from '@/utils/message-parser'

// 温度控制
DeviceControlCommand.createTemperatureControl(deviceId, temp)

// 开关控制
DeviceControlCommand.createSwitchControl(deviceId, controlName, enabled)

// Ping 设备
DeviceControlCommand.createPingCommand(deviceId)

// 配置更新
DeviceControlCommand.createConfigUpdate(deviceId, config)

// 设备重启
DeviceControlCommand.createRebootCommand(deviceId)
```

---

## 🎯 下一步

- [查看完整文档](./MQTT_INTEGRATION_GUIDE.md)
- [查看示例组件](./src/components/MqttDemo.vue)
- [查看 iot_server 实现](../iot_server/)
- [MQTT 官方文档](https://mqtt.org/)

---

**需要帮助？**

1. 查看 [完整集成指南](./MQTT_INTEGRATION_GUIDE.md)
2. 检查 `src/components/MqttDemo.vue` 中的完整示例
3. 查看浏览器开发者工具的控制台日志
4. 检查 Broker 的日志

祝你使用愉快！ 🚀
