<template>
  <view class="mqtt-demo">
    <!-- 连接状态卡片 -->
    <view class="card">
      <view class="card-title">
        <text>MQTT 连接状态</text>
      </view>
      <view class="card-content">
        <view class="status-item">
          <text class="label">连接状态：</text>
          <text :class="['value', isConnected ? 'online' : 'offline']">
            {{ isConnected ? '已连接' : '未连接' }}
          </text>
        </view>
        <view class="status-item">
          <text class="label">正在连接：</text>
          <text :class="['value', isConnecting ? 'loading' : '']">
            {{ isConnecting ? '连接中...' : '否' }}
          </text>
        </view>
        <view class="status-item">
          <text class="label">重连次数：</text>
          <text class="value">{{ reconnectAttempts }}</text>
        </view>
        <view class="status-item" v-if="lastError">
          <text class="label">最后错误：</text>
          <text class="value error">{{ lastError }}</text>
        </view>
        <view class="button-group">
          <button 
            @click="handleConnect" 
            :disabled="isConnecting || isConnected"
            class="btn btn-primary"
          >
            连接
          </button>
          <button 
            @click="handleDisconnect" 
            :disabled="!isConnected"
            class="btn btn-danger"
          >
            断开
          </button>
          <button 
            @click="handleReconnect" 
            class="btn btn-warning"
          >
            重连
          </button>
        </view>
      </view>
    </view>

    <!-- 设备数据显示 -->
    <view class="card" v-if="Object.keys(allDeviceData).length > 0">
      <view class="card-title">
        <text>设备数据（实时）</text>
      </view>
      <view class="card-content">
        <view class="device-list">
          <view class="device-card" v-for="(data, deviceId) in allDeviceData" :key="deviceId">
            <view class="device-header">
              <text class="device-name">{{ data.sensorCode || deviceId }}</text>
              <text class="device-time">{{ formatTime(data.timestamp) }}</text>
            </view>
            <view class="device-data">
              <view class="data-row" v-if="data.temperature !== undefined">
                <text class="data-label">温度：</text>
                <text class="data-value">{{ data.temperature }}°C</text>
              </view>
              <view class="data-row" v-if="data.humidity !== undefined">
                <text class="data-label">湿度：</text>
                <text class="data-value">{{ data.humidity }}%</text>
              </view>
              <view class="data-row" v-if="data.pressure !== undefined">
                <text class="data-label">气压：</text>
                <text class="data-value">{{ data.pressure }}</text>
              </view>
              <view class="data-row" v-if="data.signal !== undefined">
                <text class="data-label">信号强度：</text>
                <text class="data-value">{{ data.signal }}dBm</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 设备控制 -->
    <view class="card">
      <view class="card-title">
        <text>设备控制示例</text>
      </view>
      <view class="card-content">
        <view class="control-section">
          <text class="section-title">温度设置</text>
          <view class="input-group">
            <input 
              v-model="testDeviceId" 
              placeholder="设备 ID"
              class="input"
            />
            <input 
              v-model.number="targetTemp" 
              placeholder="目标温度 (°C)"
              type="number"
              class="input"
            />
            <button 
              @click="handleSetTemperature"
              :disabled="!isConnected"
              class="btn btn-info"
            >
              设置温度
            </button>
          </view>
        </view>

        <view class="control-section">
          <text class="section-title">控制开关</text>
          <view class="input-group">
            <input 
              v-model="controlDeviceId" 
              placeholder="设备 ID"
              class="input"
            />
            <input 
              v-model="controlName" 
              placeholder="控制名称 (fan/heater)"
              class="input"
            />
            <button 
              @click="handleToggleControl(true)"
              :disabled="!isConnected"
              class="btn btn-info"
            >
              打开
            </button>
            <button 
              @click="handleToggleControl(false)"
              :disabled="!isConnected"
              class="btn btn-secondary"
            >
              关闭
            </button>
          </view>
        </view>

        <view class="control-section">
          <text class="section-title">设备心跳检测</text>
          <view class="input-group">
            <input 
              v-model="pingDeviceId" 
              placeholder="设备 ID"
              class="input"
            />
            <button 
              @click="handlePing"
              :disabled="!isConnected"
              class="btn btn-info"
            >
              Ping 设备
            </button>
          </view>
        </view>
      </view>
    </view>

    <!-- 消息队列 -->
    <view class="card" v-if="queueSize > 0">
      <view class="card-title">
        <text>离线消息队列 ({{ queueSize }})</text>
      </view>
      <view class="card-content">
        <view class="message-list">
          <view class="message-item" v-for="(msg, index) in messageQueue" :key="index">
            <text class="msg-topic">{{ msg.topic }}</text>
            <text class="msg-content">{{ JSON.stringify(msg.message) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 订阅列表 -->
    <view class="card" v-if="subscriptions.length > 0">
      <view class="card-title">
        <text>已订阅主题</text>
      </view>
      <view class="card-content">
        <view class="tag-list">
          <text class="tag" v-for="(topic, index) in subscriptions" :key="index">
            {{ topic }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapState, mapGetters, mapActions } from 'vuex'
import { DeviceControlCommand } from '../../utils/message-parser'

export default {
  name: 'MqttDemo',
  data() {
    return {
      testDeviceId: 'device001',
      targetTemp: 25,
      controlDeviceId: 'device001',
      controlName: 'fan',
      pingDeviceId: 'device001',
    }
  },
  computed: {
    ...mapState('mqtt', [
      'isConnected',
      'isConnecting',
      'reconnectAttempts',
      'lastError',
      'messageQueue',
    ]),
    ...mapGetters('mqtt', [
      'getAllDeviceData',
      'getAllDeviceStatus',
      'getQueueSize',
      'getSubscriptions',
    ]),
    allDeviceData() {
      return this.getAllDeviceData
    },
    queueSize() {
      return this.getQueueSize
    },
    subscriptions() {
      return this.getSubscriptions
    },
  },
  methods: {
    ...mapActions('mqtt', [
      'initMqtt',
      'disconnectMqtt',
      'reconnect',
      'publishMessage',
    ]),
    
    async handleConnect() {
      try {
        await this.initMqtt()
        uni.showToast({
          title: '连接成功',
          icon: 'success',
        })
      } catch (error) {
        uni.showToast({
          title: '连接失败',
          icon: 'error',
        })
      }
    },

    async handleDisconnect() {
      try {
        await this.disconnectMqtt()
        uni.showToast({
          title: '已断开连接',
          icon: 'success',
        })
      } catch (error) {
        uni.showToast({
          title: '断开失败',
          icon: 'error',
        })
      }
    },

    async handleReconnect() {
      try {
        await this.reconnect()
        uni.showToast({
          title: '重新连接成功',
          icon: 'success',
        })
      } catch (error) {
        uni.showToast({
          title: '重新连接失败',
          icon: 'error',
        })
      }
    },

    async handleSetTemperature() {
      try {
        const command = DeviceControlCommand.createTemperatureControl(
          this.testDeviceId,
          this.targetTemp
        )
        await this.publishMessage({
          deviceId: this.testDeviceId,
          action: command.action,
          payload: command.payload,
        })
        uni.showToast({
          title: '指令已发送',
          icon: 'success',
        })
      } catch (error) {
        uni.showToast({
          title: '发送失败',
          icon: 'error',
        })
      }
    },

    async handleToggleControl(enabled) {
      try {
        const command = DeviceControlCommand.createSwitchControl(
          this.controlDeviceId,
          this.controlName,
          enabled
        )
        await this.publishMessage({
          deviceId: this.controlDeviceId,
          action: command.action,
          payload: command.payload,
        })
        uni.showToast({
          title: enabled ? '打开成功' : '关闭成功',
          icon: 'success',
        })
      } catch (error) {
        uni.showToast({
          title: '操作失败',
          icon: 'error',
        })
      }
    },

    async handlePing() {
      try {
        const command = DeviceControlCommand.createPingCommand(this.pingDeviceId)
        await this.publishMessage({
          deviceId: this.pingDeviceId,
          action: command.action,
          payload: command.payload,
        })
        uni.showToast({
          title: 'Ping 已发送',
          icon: 'success',
        })
      } catch (error) {
        uni.showToast({
          title: 'Ping 发送失败',
          icon: 'error',
        })
      }
    },

    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(Number(timestamp))
      return date.toLocaleTimeString()
    },
  },
  mounted() {
    // 可以在这里初始化数据
  },
  beforeUnmount() {
    // 清理工作
  },
}
</script>

<style scoped lang="scss">
.mqtt-demo {
  padding: 15px;
  background: #f5f5f5;

  .card {
    background: white;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 15px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .card-title {
      font-size: 16px;
      font-weight: bold;
      color: #333;
      padding-bottom: 10px;
      border-bottom: 1px solid #eee;
      margin-bottom: 15px;
    }

    .card-content {
      .status-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;
        border-bottom: 1px solid #f0f0f0;

        .label {
          color: #666;
          font-size: 14px;
          min-width: 100px;
        }

        .value {
          font-size: 14px;
          font-weight: bold;

          &.online {
            color: #4caf50;
          }

          &.offline {
            color: #f44336;
          }

          &.loading {
            color: #ff9800;
          }

          &.error {
            color: #f44336;
          }
        }
      }

      .button-group {
        display: flex;
        gap: 10px;
        margin-top: 15px;
        flex-wrap: wrap;

        .btn {
          flex: 1;
          min-width: 80px;
          padding: 8px 12px;
          border: none;
          border-radius: 4px;
          font-size: 14px;
          cursor: pointer;
          transition: all 0.3s ease;

          &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
          }

          &.btn-primary {
            background: #2196f3;
            color: white;

            &:not(:disabled):active {
              background: #1976d2;
            }
          }

          &.btn-danger {
            background: #f44336;
            color: white;

            &:not(:disabled):active {
              background: #da190b;
            }
          }

          &.btn-warning {
            background: #ff9800;
            color: white;

            &:not(:disabled):active {
              background: #e68900;
            }
          }

          &.btn-info {
            background: #4caf50;
            color: white;

            &:not(:disabled):active {
              background: #388e3c;
            }
          }

          &.btn-secondary {
            background: #9e9e9e;
            color: white;

            &:not(:disabled):active {
              background: #757575;
            }
          }
        }
      }

      .device-list {
        display: flex;
        flex-direction: column;
        gap: 10px;

        .device-card {
          background: #f9f9f9;
          border: 1px solid #e0e0e0;
          border-radius: 4px;
          padding: 10px;

          .device-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
            padding-bottom: 10px;
            border-bottom: 1px solid #e0e0e0;

            .device-name {
              font-weight: bold;
              color: #333;
              font-size: 14px;
            }

            .device-time {
              color: #999;
              font-size: 12px;
            }
          }

          .device-data {
            .data-row {
              display: flex;
              justify-content: space-between;
              padding: 4px 0;
              font-size: 13px;

              .data-label {
                color: #666;
                min-width: 60px;
              }

              .data-value {
                color: #2196f3;
                font-weight: bold;
              }
            }
          }
        }
      }

      .input-group {
        display: flex;
        gap: 10px;
        margin-bottom: 15px;
        flex-wrap: wrap;

        .input {
          flex: 1;
          min-width: 100px;
          padding: 8px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 14px;

          &:focus {
            border-color: #2196f3;
            outline: none;
          }
        }
      }

      .control-section {
        margin-bottom: 20px;

        .section-title {
          display: block;
          font-weight: bold;
          color: #333;
          margin-bottom: 10px;
          font-size: 14px;
        }
      }

      .message-list {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .message-item {
          background: #f0f0f0;
          padding: 10px;
          border-radius: 4px;
          font-size: 12px;
          word-break: break-all;

          .msg-topic {
            display: block;
            font-weight: bold;
            color: #2196f3;
            margin-bottom: 4px;
          }

          .msg-content {
            color: #666;
          }
        }
      }

      .tag-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .tag {
          background: #2196f3;
          color: white;
          padding: 4px 8px;
          border-radius: 4px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
