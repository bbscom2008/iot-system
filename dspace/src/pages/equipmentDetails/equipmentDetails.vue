<template>
  <view class="page">
    <!-- 基础信息 -->
    <view class="section">
      <view
        class="section-title"
        style="padding-top: 20rpx; justify-content: flex-start"
      >
        <view class="title-bar"></view>
        <text>基础信息</text>
      </view>

      <view class="info-card">
        <view class="info-item">
          <view class="info-label">更新时间:</view>
          <view class="info-value">{{ deviceInfo.updatedTime || "--" }}</view>
          <view class="status-dot success"></view>
        </view>
        <view class="info-item">
          <view class="info-label">最后在线时间:</view>
          <view class="info-value">{{
            deviceInfo.lastOnlineTime || "--"
          }}</view>
        </view>
      </view>

      <view class="device-card">
        <!-- 设备编号和操作按钮行 -->
        <view class="device-header">
          <view class="device-id">{{ deviceInfo.deviceName }}</view>
          <view class="device-actions">
            <button class="action-btn edit" @tap="goToDeviceSettings">
              编辑
            </button>
            <button class="action-btn delete" @tap="handleDelete">删除</button>
          </view>
        </view>

        <view class="device-number">设备编号: {{ deviceInfo.deviceNum }}</view>

        <view class="device-status-row">
          <view class="status-indicator">
            <view class="signal-bars">
              <view
                class="bar"
                v-for="i in 4"
                :key="i"
                :class="{ active: deviceInfo.signal > i }"
              ></view>
            </view>
            <text
              class="status-text"
              :class="{
                'network-normal': deviceInfo.signal > 1,
                'network-error': deviceInfo.signal <= 1,
              }"
            >
              {{ deviceInfo.signal > 1 ? "网络正常" : "网络异常" }}
            </text>
          </view>
          <view class="status-indicator">
            <view class="battery-icon">🔋</view>
            <text class="status-text"
              >{{ deviceInfo.electricQuantity || "--" }}%</text
            >
          </view>
          <view class="status-indicator">
            <view
              class="online-badge"
              :class="{
                online: deviceInfo.deviceLineState === 1,
                offline: deviceInfo.deviceLineState === 0,
              }"
            >
              {{ deviceInfo.deviceLineState === 1 ? "在线" : "离线" }}
            </view>
          </view>
        </view>
      </view>
      <!-- <view class="sort-input-row">
				<view class="input-label">设备排序:</view>
				<input class="sort-input" placeholder="请输入排序序号" />
				<button class="save-btn">保存</button>
			</view>  -->
    </view>

    <!-- 数据监控 -->
    <view class="section">
      <view class="section-title">
        <view class="title-left">
          <view class="title-bar"></view>
          <text>数据监控</text>
        </view>
        <button class="view-curve-btn" @tap="viewCurve">查看曲线</button>
      </view>

      <view class="monitoring-container">
        <!-- 第一行：温度传感器（sensor_type_id = 5） -->
        <view
          class="monitoring-row"
          v-if="getTemperatureSensors(deviceInfo.sensors).length > 0"
        >
          <view
            class="monitor-item"
            v-for="sensor in getTemperatureSensors(deviceInfo.sensors)"
            :key="sensor.id"
            @tap="goToSensorDetail(sensor)"
          >
            <view class="monitor-circle">
              <text class="circle-value"
                >{{ sensor.sensorValue || "--" }}</text
              >
              <text class="circle-unit">°C</text>
              
            </view>
            <text class="circle-label">{{ sensor.sensorName }}</text>
          </view>
        </view>
        <!-- 第二行：其他传感器（湿度、气体等） -->
        <view
          class="monitoring-row"
          v-if="getOtherSensors(deviceInfo.sensors).length > 0"
        >
          <view
            class="monitor-item"
            v-for="sensor in getOtherSensors(deviceInfo.sensors)"
            @tap="goToSensorDetail(sensor)"
            :key="sensor.id"
          >
            <view class="monitor-circle">
              <text class="circle-value">{{ sensor.sensorValue || "--" }}</text>
              <text class="circle-unit">{{ sensor.unit }}</text>
            </view>
            <text class="circle-label">{{ sensor.sensorName }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 设备控制 -->
    <view class="section">
      <view class="section-title">
        <view class="title-left">
          <view class="title-bar"></view>
          <text>设备控制</text>
        </view>
        <!-- <button class="modify-device-btn">修改设备</button> -->
      </view>

      <view class="control-grid">
        <!-- 风机设备 - 根据 motorFans 动态渲染 -->
        <view
          class="grid-item"
          v-for="fan in deviceInfo.motorFans || []"
          :key="fan.id"
          :data-id="fan.id"
          @tap="handleFanToggle"
        >
          <FanControl
            :device-id="fan.id"
            :label="fan.fanName"
            :is-active="fan.isRunning === 1"
          />
        </view>

        <!-- 变频电机 - 根据 frequencyMotors 动态渲染 -->
        <view
          v-for="motor in deviceInfo.frequencyMotors || []"
          :key="motor.id"
          class="grid-item control-item"
          :data-id="motor.id"
          @tap="handleFrequencyMotorClick"
        >
          <view class="frequency-circle">
            <text class="circle-value">{{ motor.value || "--" }}</text>
          </view>
          <text class="fan-label">{{ motor.deviceName }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import request from "@/utils/request.js";
import SvgIcon from "@/components/SvgIcon.vue";
import FanControl from "@/components/FanControl.vue";
import { mapState } from 'vuex'

export default {
  name: "EquipmentDetails",
  components: { SvgIcon, FanControl },
  data() {
    return {
      // deviceInfo 从 Vuex 仓库读取，移除本地初始化
      fanUpdateTimer: null, // 风扇状态更新定时器
    };
  },

  computed: {
    ...mapState('deviceDetail', {
      deviceInfo: state => state.deviceInfo || {},
      deviceId: state => state.currDevice.id
    })
  },
  // watch: {
  //   // 监听 currUpdateDeviceNum 的变化
  //   '$store.state.device.currUpdateDeviceNum':{
  //     handler(newVal, oldVal) {
  //       console.log('-----watch----currUpdateDeviceNum--', newVal);
  //       if(newVal == this.$store.state.deviceDetail.currDevice.deviceNum){
  //         console.log('设备详情页检测到设备更新，重新获取设备信息')
  //         this.getDeviceInfo();
  //       }
  //     },
  //   },
  // },
  onLoad(options) {
    this.getDeviceInfo();
  },
  onShow() {
    if (this.$store.state.deviceDetail.currDevice?.id) {
      this.getDeviceInfo();
    }
  },
  onUnload() {
    // 页面卸载时清除定时器
    // this.stopFanStatusUpdate();
    // 清空
    this.$store.state.deviceDetail.currDevice = null;
  },
  methods: {
    // 获取设备信息（通过 Vuex action）
    async getDeviceInfo() {
      try {
        const res = await this.$store.dispatch('deviceDetail/fetchDeviceInfo')
        console.log('设备详情API响应（已保存到仓库）:', res)
      } catch (err) {
        console.log('获取设备信息失败', err)
        uni.showToast({ title: '获取设备信息失败', icon: 'none' })
      }
    },

    // 处理风机点击事件 - 跳转到风机详情页面
    handleFanToggle(e) {
      // 通过 data-id 取值（小程序跨线程安全方式），再查找展开为纯对象
      const fanId = e.currentTarget.dataset.id;
      const fans = this.deviceInfo.motorFans || [];
      const found = fans.find(f => f.id === fanId);
      if (!found) return;
      const plainFan = { ...found };
      this.$store.commit("deviceDetail/SET_CURRENT_MOTOR_FAN", plainFan);

      // 跳转到风机详情页面
      uni.navigateTo({
        url: "/pages/motorFanDetail/motorFanDetail",
      });
    },
    // 查看曲线
    viewCurve() {
      uni.navigateTo({
        url: "/pages/curveChart/curveChart?deviceId=" + this.deviceId,
      });
    },
    // 获取温度传感器（sensor_type_id = 5）
    getTemperatureSensors(sensors) {
      if (!sensors || !Array.isArray(sensors)) return [];
      return sensors.filter((s) => s.sensorTypeId === 5);
    },
    // 获取其他传感器（sensor_type_id != 5）
    getOtherSensors(sensors) {
      if (!sensors || !Array.isArray(sensors)) return [];
      return sensors.filter((s) => s.sensorTypeId !== 5);
    },
    // 获取控制类型名称
    getControlTypeName(controlType) {
      const typeMap = {
        1: "温控",
        2: "湿控",
        3: "气体控制",
      };
      return typeMap[controlType] || "未知";
    },
    // 启动风扇状态随机更新（模拟 WebSocket）
    // startFanStatusUpdate() {
    //   // 每5秒随机更新一次风扇状态
    //   this.fanUpdateTimer = setInterval(() => {
    //     if (this.deviceInfo.motorFans && this.deviceInfo.motorFans.length > 0) {
    //       // 随机选择一个风扇
    //       const randomIndex = Math.floor(
    //         Math.random() * this.deviceInfo.motorFans.length
    //       );
    //       const fan = this.deviceInfo.motorFans[randomIndex];

    //       // 随机决定是否切换状态（50% 概率）
    //       if (Math.random() > 0.5) {
    //         const newStatus = fan.isRunning === 1 ? 0 : 1;
    //         this.$set(fan, "isRunning", newStatus);

    //         console.log(`${fan.fanName} ${newStatus === 1 ? "启动" : "停止"}`);
    //       }
    //     }
    //   }, 5000); // 每5秒更新一次
    // },
    // 停止风扇状态更新
    // stopFanStatusUpdate() {
    //   if (this.fanUpdateTimer) {
    //     clearInterval(this.fanUpdateTimer);
    //     this.fanUpdateTimer = null;
    //   }
    // },
    // 处理变频器点击事件
    handleFrequencyMotorClick(e) {
      // 通过 data-id 取值（小程序跨线程安全方式），再查找展开为纯对象
      const motorId = e.currentTarget.dataset.id;
      const motors = this.deviceInfo.frequencyMotors || [];
      const found = motors.find(m => m.id === motorId);
      if (!found) return;
      const plainMotor = { ...found };
      this.$store.commit("deviceDetail/SET_CURRENT_FREQUENCY_MOTOR", plainMotor);

      // 跳转到变频器详情页面
      uni.navigateTo({
        url: "/pages/frequencyConverter/frequencyConverter",
      });
    },
    // 跳转到设备设置页面
    goToDeviceSettings() {
      uni.navigateTo({
        url: "/pages/deviceSettings/deviceSettings?deviceId=" + this.deviceId,
      });
    },
    // 跳转到传感器详情页面
    goToSensorDetail(sensor) {
      this.$store.commit("deviceDetail/SET_CURRENT_SENSOR_ID", sensor.id);
      uni.navigateTo({
        url: `/pages/sensorDetail/sensorDetail`,
      });
    },
    // 删除设备
    handleDelete() {
      uni.showModal({
        title: "提示",
        content: "确定要删除该设备吗？",
        success: (res) => {
          if (res.confirm) {
            request.del(`/device/delete/${this.deviceId}`).then((res) => {
              uni.showToast({
                title: "删除成功",
                icon: "success",
              });
              // 获取设备列表更新 Vuex
              this.$store.dispatch('device/fetchDeviceList')

              uni.navigateBack();
            });
          }
        },
      });
    },
  },
};
</script>

<style scoped>
page {
  background-color: var(--primary-bg);
  padding-bottom: 100rpx;
}

.page {
  min-height: 100vh;
  background-color: var(--primary-bg);
  padding: 0 30rpx;
}

/* 章节 */
.section {
  margin-bottom: 40rpx;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
}

.title-bar {
  width: 6rpx;
  height: 40rpx;
  background-color: var(--accent-color);
  border-radius: 3rpx;
  margin-right: 20rpx;
}

.title-left {
  display: flex;
  align-items: center;
}

.section-title text {
  color: var(--text-primary);
  font-size: 30rpx;
  font-weight: bold;
}

.modify-device-btn {
  background-color: var(--accent-color);
  color: var(--text-primary);
  border: none;
  border-radius: 20rpx;
  padding: 10rpx 20rpx;
  font-size: 22rpx;
  margin-left: 0;
  margin-right: 20rpx;
}

.view-curve-btn {
  background-color: var(--accent-color);
  color: var(--text-primary);
  border: none;
  border-radius: 20rpx;
  padding: 10rpx 20rpx;
  font-size: 22rpx;
  margin-left: 0;
  margin-right: 20rpx;
}

/* 基础信息卡片 */
.info-card {
  /* background-color: var(--card-bg); */
  border-radius: 16rpx;
  padding: 20rpx;
  /* margin-bottom: 20rpx; */
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 15rpx;
  position: relative;
}

.info-label {
  color: var(--text-secondary);
  font-size: 26rpx;
  margin-right: 15rpx;
}

.info-value {
  color: var(--text-primary);
  font-size: 26rpx;
  flex: 1;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
}

.status-dot.success {
  background-color: var(--success-color);
}

.info-text {
  color: var(--text-secondary);
  font-size: 26rpx;
}

/* 设备卡片 */
.device-card {
  background-color: var(--card-bg);
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

/* 设备头部 - 设备编号和操作按钮 */
.device-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15rpx;
}

.device-id {
  font-size: 28rpx;
  font-weight: bold;
  color: var(--text-primary);
}

.device-number {
  color: var(--text-secondary);
  font-size: 26rpx;
  margin-bottom: 15rpx;
}

/* 设备状态行 */
.device-status-row {
  display: flex;
  align-items: center;
  gap: 25rpx;
  margin-bottom: 20rpx;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.signal-bars {
  display: flex;
  gap: 2rpx;
  align-items: end;
}

.bar {
  width: 4rpx;
  background-color: var(--text-muted);
  border-radius: 2rpx;
}

.bar:nth-child(1) {
  height: 8rpx;
}
.bar:nth-child(2) {
  height: 12rpx;
}
.bar:nth-child(3) {
  height: 16rpx;
}
.bar:nth-child(4) {
  height: 20rpx;
}

.bar.active {
  background-color: var(--success-color);
}

.status-text {
  color: var(--text-primary);
  font-size: 22rpx;
}

.status-text.network-normal {
  color: var(--success-color);
}

.status-text.network-error {
  color: var(--error-color);
}

.battery-icon {
  font-size: 22rpx;
}

.online-badge {
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
}

.online-badge.online {
  background-color: var(--success-color);
  color: #ffffff;
}

.online-badge.offline {
  background-color: var(--error-color);
  color: #ffffff;
}

/* 设备操作 */
.device-actions {
  display: flex;
  gap: 30rpx;
}

.action-btn {
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  border: none;
}

.action-btn.edit {
  background-color: var(--green-color);
  color: var(--text-primary);
}

.action-btn.delete {
  background-color: var(--error-color);
  color: var(--text-primary);
}

/* 排序输入行 */
.sort-input-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.input-label {
  color: var(--text-secondary);
  font-size: 26rpx;
}

.sort-input {
  flex: 1;
  height: 60rpx;
  background-color: var(--secondary-bg);
  border: 1rpx solid var(--border-color);
  border-radius: 30rpx;
  padding: 0 20rpx;
  color: var(--text-primary);
  font-size: 26rpx;
}

.save-btn {
  background-color: var(--accent-color);
  color: var(--text-primary);
  border: none;
  border-radius: 30rpx;
  padding: 12rpx 24rpx;
  font-size: 22rpx;
}

/* 数据监控 */
.monitoring-container {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.monitoring-row {
  display: flex;
  justify-content: space-evenly;
  gap: 15rpx;
}

.monitoring-row-center {
  justify-content: center;
  gap: 30rpx;
}

.monitor-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.monitor-circle {
  width: 120rpx;
  height: 120rpx;
  border: 2rpx solid var(--accent-color);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(106, 90, 205, 0.1);
  box-shadow: 0 0 20rpx rgba(106, 90, 205, 0.3);
  position: relative;
}

.monitor-circle::before {
  content: "";
  position: absolute;
  width: 128rpx;
  height: 128rpx;
  top: -8rpx;
  left: -8rpx;

  border-radius: 50%;
  border: 2rpx dotted var(--text-primary);
  background: linear-gradient(
    45deg,
    var(--accent-color),
    transparent,
    var(--accent-color)
  );
  /* z-index: -1; */
  opacity: 0.6;
}

.circle-value {
  font-size: 28rpx;
  font-weight: bold;
  color: var(--text-primary);
  text-align: center;
}
.circle-unit {
  font-size: 22rpx;
  color: var(--text-secondary);
  text-align: center;
  font-weight: bold;
}
.circle-label {
  font-size: 24rpx;
  color: var(--text-secondary);
  text-align: center;
  margin-top: 10rpx;
}

/* 设备控制 */
.control-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15rpx;
}
.grid-item {
  height: 80rpx;
}

/* 变频电机 */
.frequency-motor-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.frequency-motor-item {
  background-color: var(--card-bg);
  border-radius: 16rpx;
  padding: 24rpx;
  border: 2rpx solid var(--accent-color);
}

.motor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1px solid rgba(106, 90, 205, 0.2);
}

.motor-name {
  font-size: 28rpx;
  font-weight: bold;
  color: var(--text-primary);
}

.motor-mode {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
}

.motor-mode.auto {
  background-color: var(--success-color);
  color: #ffffff;
}

.motor-mode.manual {
  background-color: var(--warning-color);
  color: #ffffff;
}

.motor-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.info-row {
  display: flex;
  align-items: center;
}

.info-label {
  color: var(--text-secondary);
  font-size: 24rpx;
  margin-right: 8rpx;
}

.info-value {
  color: var(--text-primary);
  font-size: 24rpx;
  font-weight: bold;
}

.motor-threshold {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1px solid rgba(106, 90, 205, 0.2);
}

.threshold-title {
  color: var(--text-secondary);
  font-size: 22rpx;
  margin-bottom: 8rpx;
  display: block;
}

.threshold-list {
  color: var(--text-primary);
  font-size: 22rpx;
  padding-left: 20rpx;
}

/* 调速风机和变频2放在一行 */
.control-item-wide {
  grid-column: span 1; /* 改为占1列而不是2列 */
}

/* 变频电机样式 */
.fan-control {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: row;
  gap: 12rpx;
  padding: 15rpx;
  background-color: var(--card-bg);
  border-radius: 12rpx;
}

.fan-icon-container {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.fan-icon-container.active {
  animation: rotate 2s linear infinite;
}

.frequency-circle {
  width: 52rpx;
  height: 52rpx;
  border-radius: 30%;
  border: 4rpx solid var(--accent-color);
  border-color: #00ff00;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(106, 90, 205, 0.1);
  box-shadow: 0 0 20rpx rgba(106, 90, 205, 0.3);
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.frequency-circle.active {
  border-color: #00ff00;
  background-color: rgba(0, 255, 0, 0.1);
  box-shadow: 0 0 20rpx rgba(0, 255, 0, 0.3);
  animation: pulse 2s ease-in-out infinite;
}

.frequency-circle .circle-value {
  /* color: var(--accent-color); */
  color: #00ff00;
  font-size: 28rpx;
  font-weight: bold;
}

.fan-label {
  color: var(--accent-color);
  font-size: 28rpx;
  text-align: center;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.control-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 15rpx;
  background-color: var(--card-bg);
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.3s ease;
}

.control-circle {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid var(--accent-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  transition: all 0.3s ease;
}

.control-circle.active {
  background-color: var(--accent-color);
  border-color: #00ff00;
}

.circle-number {
  font-size: 20rpx;
  font-weight: bold;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.control-circle.active .circle-number {
  color: #000;
}

.control-text {
  color: var(--text-primary);
  font-size: 26rpx;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
