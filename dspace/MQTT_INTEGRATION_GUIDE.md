# MQTT 集成指南 - dspace 项目

## 📋 目录

- [项目概述](#项目概述)
- [已完成的实现](#已完成的实现)
- [配置说明](#配置说明)
- [使用方法](#使用方法)
- [API 文档](#api-文档)
- [常见问题](#常见问题)
- [下一步工作](#下一步工作)

---

## 项目概述

本文档详细说明了如何在 **dspace** (UniApp 项目)中集成 MQTT 实时消息系统，以实现与 **iot_server** 后端的实时通信。该实现支持 **H5** 和 **微信小程序** 两个平台。

### 系统架构

```
┌─────────────────────────────────────────┐
│         dspace (UniApp 前端)           │
├──────────────┬──────────────────────────┤
│              │      MQTT 模块           │
│              ├─────────────────────────┤
│  Components  │  ┌──────────────────┐  │
│  (Pages)     │  │ mqtt.js (核心)  │  │
│              │  └──────────────────┘  │
│              │  ┌──────────────────┐  │
│              │  │ mqtt-config.js  │  │
│              │  └──────────────────┘  │
│              │  ┌──────────────────┐  │
│              │  │message-parser.js│  │
│              │  └──────────────────┘  │
├──────────────┼──────────────────────────┤
│   Vuex Store │  modules/mqtt.js       │
│  (状态管理)  │  (状态 & 业务逻辑)   │
└──────────────┴──────────────────────────┘
         │
         │ (MQTT WebSocket / TCP)
         │
┌────────▼─────────────────────────────────┐
│    MQTT Broker (如 Mosquitto)            │
│  (地址: 192.168.56.128:8883)            │
└────────┬─────────────────────────────────┘
         │
         │
┌────────▼─────────────────────────────────┐
│      iot_server (SpringBoot 后端)       │
│  (已实现 MQTT 消息接收和数据库存储)     │
└──────────────────────────────────────────┘
```

---

## 已完成的实现

### ✅ 1. MQTT 客户端库安装

- **库**: `mqtt@4.3.7` (支持 Node.js、H5、小程序)
- **位置**: `package.json` dependencies
- **特性**: WebSocket 和 TCP 双协议支持

### ✅ 2. 配置文件 (`src/utils/mqtt-config.js`)

- 开发和生产环境配置分离
- 自动平台检测 (H5 vs 小程序)
- MQTT 主题定义
- QoS 等级配置

### ✅ 3. MQTT 核心服务 (`src/utils/mqtt.js`)

- **连接管理**

  - `connect()` - 建立连接
  - `disconnect()` - 断开连接
  - 自动重连机制

- **消息操作**

  - `subscribe()` - 订阅主题
  - `unsubscribe()` - 取消订阅
  - `publish()` - 发布消息

- **事件系统**
  - `on()` - 注册事件监听
  - `off()` - 移除事件监听
  - `emit()` - 触发事件

### ✅ 4. Vuex 状态管理 (`src/store/modules/mqtt.js`)

- **State**: 连接状态、消息数据、设备状态
- **Mutations**: 状态更新操作
- **Actions**: 异步业务逻辑

  - `initMqtt` - 初始化连接
  - `publishMessage` - 发布消息
  - `subscribeDevice` - 订阅设备
  - 离线消息队列处理

- **Getters**: 数据查询和计算属性

### ✅ 5. 消息解析器 (`src/utils/message-parser.js`)

- `DeviceDataParser` - 设备数据解析
- `DeviceStatusParser` - 设备状态解析
- `DeviceControlCommand` - 控制指令生成
- `SystemMessageHandler` - 系统消息处理

### ✅ 6. 应用初始化 (`src/main.js`)

- 自动 MQTT 连接
- 重试机制
- 前台/后台事件处理

### ✅ 7. 示例组件 (`src/components/MqttDemo.vue`)

- 连接状态显示
- 实时设备数据展示
- 控制指令发送
- 消息队列管理

---

## 配置说明

### 修改 MQTT Broker 地址

编辑 `src/utils/mqtt-config.js`:

```javascript
const MQTT_CONFIG = {
  dev: {
    broker: 'ws://192.168.56.128:8883', // WebSocket (H5)
    brokerTcp: 'mqtt://192.168.56.128:1883', // TCP (小程序)
    clientId: 'dspace-' + Math.random().toString(36).substr(2, 9),
    username: '', // 如需认证，填写用户名
    password: '', // 如需认证，填写密码
    // ... 其他配置
  },
  prod: {
    // 生产环境配置
  },
}
```

### 修改 MQTT 主题

编辑 `src/utils/mqtt-config.js` 中的 `MQTT_TOPICS`:

```javascript
export const MQTT_TOPICS = {
  DEVICE_DATA: 'device/+/data', // 设备数据
  DEVICE_STATUS: 'device/+/status', // 设备状态
  DEVICE_CONTROL: 'device/+/control', // 控制指令
  SYSTEM: 'system/#', // 系统消息
  // 根据需要添加更多主题
}
```

### 关闭自动连接

编辑 `src/main.js`:

```javascript
const shouldInitMqtt = false // 改为 false 禁用自动连接
if (shouldInitMqtt) {
  initializeMqtt()
}
```

---

## 使用方法

### 在组件中使用 MQTT

#### 1️⃣ 获取连接状态

```vue
<script>
import { mapGetters } from 'vuex'

export default {
  computed: {
    ...mapGetters('mqtt', ['isConnected']),
  },
}
</script>

<template>
  <view>
    <text v-if="isConnected">已连接</text>
    <text v-else>未连接</text>
  </view>
</template>
```

#### 2️⃣ 获取实时设备数据

```vue
<script>
import { mapGetters } from 'vuex'

export default {
  computed: {
    ...mapGetters('mqtt', ['getDeviceData']),
  },
  mounted() {
    // 获取特定设备的数据
    const deviceData = this.getDeviceData('device001')
    console.log('温度:', deviceData.temperature)
    console.log('湿度:', deviceData.humidity)
  },
}
</script>
```

#### 3️⃣ 发送控制指令

```vue
<script>
import { mapActions } from 'vuex'
import { DeviceControlCommand } from '@/utils/message-parser'

export default {
  methods: {
    ...mapActions('mqtt', ['publishMessage']),

    async setTemperature() {
      try {
        const command = DeviceControlCommand.createTemperatureControl(
          'device001', // 设备 ID
          25 // 目标温度
        )

        await this.publishMessage({
          deviceId: 'device001',
          action: command.action,
          payload: command.payload,
        })

        console.log('温度设置成功')
      } catch (error) {
        console.error('设置失败:', error)
      }
    },

    async toggleFan() {
      try {
        const command = DeviceControlCommand.createSwitchControl(
          'device001', // 设备 ID
          'fan', // 控制名称
          true // 打开
        )

        await this.publishMessage({
          deviceId: 'device001',
          action: command.action,
          payload: command.payload,
        })

        console.log('控制成功')
      } catch (error) {
        console.error('控制失败:', error)
      }
    },
  },
}
</script>
```

#### 4️⃣ 订阅特定设备

```vue
<script>
import { mapActions } from 'vuex'

export default {
  methods: {
    ...mapActions('mqtt', ['subscribeDevice']),

    async watchDevice() {
      try {
        await this.subscribeDevice('device001')
        console.log('已订阅 device001')
      } catch (error) {
        console.error('订阅失败:', error)
      }
    },
  },
}
</script>
```

#### 5️⃣ 监听 MQTT 连接事件

```vue
<script>
import mqttClient from '@/utils/mqtt'

export default {
  mounted() {
    // 连接成功
    mqttClient.on('connected', () => {
      console.log('MQTT 已连接')
      this.onMqttConnected()
    })

    // 连接断开
    mqttClient.on('disconnected', () => {
      console.log('MQTT 已断开')
      this.onMqttDisconnected()
    })

    // 接收消息
    mqttClient.on('message', ({ topic, message }) => {
      console.log(`收到消息 [${topic}]:`, message)
    })

    // 错误处理
    mqttClient.on('error', (error) => {
      console.error('MQTT 错误:', error)
    })
  },
}
</script>
```

---

## API 文档

### MQTT 核心服务 (`src/utils/mqtt.js`)

#### 连接管理

```javascript
// 连接到 MQTT Broker
await mqttClient.connect()

// 断开连接
await mqttClient.disconnect()

// 获取连接状态
const status = mqttClient.getStatus() // 返回 boolean
```

#### 消息操作

```javascript
// 订阅主题
await mqttClient.subscribe('device/+/data', { qos: 1 }, (message) => {
  console.log('收到消息:', message)
})

// 订阅多个主题
await mqttClient.subscribe(['device/+/data', 'device/+/status'])

// 取消订阅
await mqttClient.unsubscribe('device/+/data')

// 发布消息
await mqttClient.publish(
  'device/001/control',
  {
    action: 'set_temperature',
    value: 25,
  },
  { qos: 1 }
)
```

#### 事件系统

```javascript
// 注册事件监听
mqttClient.on('connected', () => {
  console.log('已连接')
})

mqttClient.on('message', ({ topic, message }) => {
  console.log('收到消息', topic, message)
})

// 移除事件监听
mqttClient.off('connected', handler)

// 可用事件:
// - 'connected' - 连接成功
// - 'disconnected' - 连接断开
// - 'error' - 错误发生
// - 'message' - 接收消息
// - 'subscribed' - 订阅成功
// - 'unsubscribed' - 取消订阅
// - 'published' - 消息已发布
// - 'reconnecting' - 重新连接中
// - 'offline' - 离线状态
```

### Vuex 状态管理 (`src/store/modules/mqtt.js`)

#### State

```javascript
// 连接状态
isConnected // boolean - 是否已连接
isConnecting // boolean - 是否正在连接
reconnectAttempts // number - 重连次数

// 消息数据
deviceDataMap // Object - {deviceId: {temperature, humidity, ...}}
deviceStatusMap // Object - {deviceId: {online, signal, ...}}
messageQueue // Array - 离线消息队列

// 其他
lastError // string - 最后错误信息
subscriptions // Array - 订阅的主题列表
```

#### Actions

```javascript
// 初始化 MQTT 连接
await store.dispatch('mqtt/initMqtt')

// 发布消息
await store.dispatch('mqtt/publishMessage', {
  deviceId: 'device001',
  action: 'set_temperature',
  payload: { targetTemperature: 25 },
})

// 订阅设备
await store.dispatch('mqtt/subscribeDevice', 'device001')

// 取消订阅设备
await store.dispatch('mqtt/unsubscribeDevice', 'device001')

// 断开连接
await store.dispatch('mqtt/disconnectMqtt')

// 重新连接
await store.dispatch('mqtt/reconnect')
```

#### Getters

```javascript
// 连接状态
store.getters['mqtt/isConnected']
store.getters['mqtt/isConnecting']
store.getters['mqtt/reconnectAttempts']

// 设备数据查询
store.getters['mqtt/getDeviceData']('device001') // 获取特定设备数据
store.getters['mqtt/getAllDeviceData'] // 获取所有设备数据
store.getters['mqtt/getDeviceStatus']('device001') // 获取设备状态
store.getters['mqtt/getAllDeviceStatus'] // 获取所有设备状态

// 队列信息
store.getters['mqtt/getMessageQueue'] // 获取离线消息队列
store.getters['mqtt/getQueueSize'] // 获取队列大小

// 其他
store.getters['mqtt/getSubscriptions'] // 获取订阅列表
store.getters['mqtt/getLastUpdateTime'] // 获取最后更新时间
```

### 消息解析器 (`src/utils/message-parser.js`)

#### DeviceDataParser

```javascript
import { DeviceDataParser } from '@/utils/message-parser'

// 解析设备数据消息
const data = DeviceDataParser.parse(message)
// 返回: {sensorCode, temperature, humidity, pressure, ...}

// 验证数据有效性
const isValid = DeviceDataParser.validate(data) // 返回 boolean

// 转换为数据库格式
const dbData = DeviceDataParser.toDbFormat(data)
```

#### DeviceControlCommand

```javascript
import { DeviceControlCommand } from '@/utils/message-parser'

// 创建温度设置指令
const cmd = DeviceControlCommand.createTemperatureControl('device001', 25)
// 返回: {action: 'set_temperature', deviceId, payload}

// 创建开关控制指令
const cmd = DeviceControlCommand.createSwitchControl('device001', 'fan', true)

// 创建设备 Ping 指令
const cmd = DeviceControlCommand.createPingCommand('device001')

// 创建配置更新指令
const cmd = DeviceControlCommand.createConfigUpdate('device001', {
  workMode: 'eco',
  updateInterval: 60,
})
```

---

## 常见问题

### Q1: 如何连接到自己的 MQTT Broker?

**A:** 修改 `src/utils/mqtt-config.js`:

```javascript
broker: 'ws://your-broker-ip:8883',      // H5 使用 WebSocket
brokerTcp: 'mqtt://your-broker-ip:1883', // 小程序使用 TCP
```

### Q2: H5 和小程序环境有什么区别?

**A:** 主要区别在于协议:

- **H5**: 使用 WebSocket 协议 (`ws://`)
- **小程序**: 使用 TCP 协议 (`mqtt://`)

配置文件会自动检测平台并选择合适的协议。

### Q3: 如何处理离线消息?

**A:** 系统自动维护一个离线消息队列，当重新连接时会自动重新发送:

```javascript
// 查看离线消息队列
const queue = store.getters['mqtt/getMessageQueue']
console.log('离线消息数:', queue.length)
```

### Q4: 如何调试 MQTT 连接?

**A:** 查看浏览器控制台日志 (F12):

```javascript
// 在 main.js 中添加调试输出
mqttClient.on('connected', () => {
  console.log('[MQTT Debug] Connected')
})

mqttClient.on('message', ({ topic, message }) => {
  console.log('[MQTT Debug] Message:', topic, message)
})
```

### Q5: 小程序中 MQTT 无法连接怎么办?

**A:** 检查以下几点:

1. **MQTT Broker 地址**: 确保 IP 和端口正确
2. **域名白名单**: 在小程序后台添加域名白名单
3. **协议版本**: 确保 Broker 支持 MQTT 4.0+
4. **防火墙**: 检查防火墙是否允许连接

```javascript
// 在 mqtt-config.js 中查看使用的地址
console.log('Using broker:', config.brokerUrl)
```

### Q6: 消息数据结构是什么?

**A:** 设备数据消息结构:

```javascript
{
  sensorCode: 'device001',        // 设备唯一标识
  temperature: 23.5,              // 温度 (°C)
  humidity: 45,                   // 湿度 (%)
  pressure: 1013,                 // 气压
  targetTemperature: 25,          // 目标温度
  status: 1,                      // 设备状态
  controls: {                     // 控制状态
    fanEnabled: true,             // 风扇开启
    heaterEnabled: false,         // 加热器关闭
    // ...
  },
  signal: -45,                    // 信号强度 (dBm)
  battery: 85,                    // 电池电量 (%)
  timestamp: 1671600000000        // 时间戳 (ms)
}
```

### Q7: 如何在生产环境中禁用调试日志?

**A:** 在 `src/utils/mqtt.js` 中注释掉或条件化 `console.log` 调用:

```javascript
if (process.env.NODE_ENV === 'development') {
  console.log('[MQTT]...') // 仅在开发环境输出
}
```

---

## 下一步工作

### 🔄 后续可以实现的功能

- [ ] **消息加密**: 实现 TLS/SSL 连接
- [ ] **本地存储**: 使用 SQLite 存储历史数据
- [ ] **数据同步**: 定期与后端同步离线数据
- [ ] **权限管理**: 实现基于用户的主题访问控制
- [ ] **实时图表**: 集成图表库展示实时数据变化
- [ ] **告警系统**: 实现数据异常告警
- [ ] **设备管理**: 完整的设备列表、绑定、配置功能
- [ ] **性能优化**: 消息压缩、批量发送
- [ ] **单元测试**: 为 MQTT 模块编写单元测试
- [ ] **错误恢复**: 增强错误处理和自动恢复机制

### 📝 参考链接

- [MQTT 官方文档](https://mqtt.org/)
- [MQTT.js 库文档](https://github.com/mqttjs/MQTT.js)
- [UniApp 官方文档](https://uniapp.dcloud.io/)
- [Vuex 文档](https://vuex.vuejs.org/)
- [iot_server 项目](../iot_server/)

---

## 使用示例

### 完整的设备监控页面

```vue
<template>
  <view class="monitor-page">
    <view v-if="isConnected" class="status-bar connected">
      <text>✓ MQTT 已连接</text>
    </view>
    <view v-else class="status-bar disconnected">
      <text>✗ MQTT 未连接</text>
    </view>

    <!-- 设备列表 -->
    <scroll-view class="device-list">
      <view
        v-for="(data, deviceId) in allDeviceData"
        :key="deviceId"
        class="device-item"
        @click="selectDevice(deviceId)"
      >
        <view class="device-name">{{ data.sensorCode }}</view>
        <view class="device-stats">
          <text>🌡️ {{ data.temperature }}°C</text>
          <text>💧 {{ data.humidity }}%</text>
        </view>
      </view>
    </scroll-view>

    <!-- 设备详情 -->
    <view v-if="selectedDevice" class="device-detail">
      <view class="detail-card">
        <text class="title">{{ selectedDevice.sensorCode }} 详情</text>
        <view class="detail-row">
          <text>温度：{{ selectedDevice.temperature }}°C</text>
          <button @click="increaseTemp">+</button>
          <button @click="decreaseTemp">-</button>
        </view>
        <view class="detail-row">
          <text>湿度：{{ selectedDevice.humidity }}%</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { DeviceControlCommand } from '@/utils/message-parser'

export default {
  data() {
    return {
      selectedDeviceId: null,
    }
  },
  computed: {
    ...mapGetters('mqtt', ['isConnected', 'getAllDeviceData', 'getDeviceData']),
    allDeviceData() {
      return this.getAllDeviceData
    },
    selectedDevice() {
      if (!this.selectedDeviceId) return null
      return this.getDeviceData(this.selectedDeviceId)
    },
  },
  methods: {
    ...mapActions('mqtt', ['publishMessage']),
    selectDevice(deviceId) {
      this.selectedDeviceId = deviceId
    },
    async increaseTemp() {
      const data = this.selectedDevice
      const newTemp = (data.temperature || 20) + 1
      const cmd = DeviceControlCommand.createTemperatureControl(
        this.selectedDeviceId,
        newTemp
      )
      await this.publishMessage({
        deviceId: this.selectedDeviceId,
        action: cmd.action,
        payload: cmd.payload,
      })
    },
    async decreaseTemp() {
      const data = this.selectedDevice
      const newTemp = Math.max(0, (data.temperature || 20) - 1)
      const cmd = DeviceControlCommand.createTemperatureControl(
        this.selectedDeviceId,
        newTemp
      )
      await this.publishMessage({
        deviceId: this.selectedDeviceId,
        action: cmd.action,
        payload: cmd.payload,
      })
    },
  },
  mounted() {
    console.log('监控页面已加载')
  },
}
</script>

<style scoped>
.monitor-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.status-bar {
  padding: 10px;
  text-align: center;
  color: white;
  font-weight: bold;
}

.status-bar.connected {
  background: #4caf50;
}

.status-bar.disconnected {
  background: #f44336;
}

.device-list {
  flex: 1;
  padding: 15px;
}

.device-item {
  background: white;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.device-name {
  font-weight: bold;
  margin-bottom: 8px;
}

.device-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.device-detail {
  padding: 15px;
  border-top: 1px solid #eee;
}

.detail-card {
  background: white;
  padding: 15px;
  border-radius: 8px;
}

.title {
  display: block;
  font-weight: bold;
  margin-bottom: 15px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

button {
  padding: 5px 10px;
  border: 1px solid #2196f3;
  background: #2196f3;
  color: white;
  border-radius: 4px;
}
</style>
```

---

**最后更新**: 2025-12-22
**版本**: 1.0
**作者**: AI Assistant
