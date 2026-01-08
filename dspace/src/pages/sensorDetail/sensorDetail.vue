<template>
  <view class="page">
    

    <!-- 传感器信息 -->
    <view class="section">
      <view class="section-title">
        <view class="title-bar"></view>
        <text>传感器信息</text>
      </view>
      
      <view class="setting-card">
        <view class="setting-item">
          <view class="setting-label">传感器名称:</view>
          <input class="setting-input" v-model="sensorInfo.sensorName" placeholder="请输入传感器名称" />
        </view>
        
        <view class="setting-item">
          <view class="setting-label">传感器编码:</view>
          <input class="setting-input" v-model="sensorInfo.sensorCode" placeholder="请输入传感器编码" />
        </view>
        
        <view class="setting-item">
          <view class="setting-label">所属设备:</view>
          <view class="readonly-value">{{ $store.state.app.deviceInfo.deviceName || '--' }}</view>
        </view>
      </view>
    </view>

    <!-- 当前值 -->
    <view class="section">
      <view class="section-title">
        <view class="title-bar"></view>
        <text>当前值</text>
      </view>
      <view class="value-card">
        <text class="value-display">{{ sensorInfo.sensorValue || "--" }}{{ sensorInfo.unit }}</text>
      </view>
    </view>

    <!-- 探头校正 -->
    <view class="section">
      <view class="section-title">
        <view class="title-bar"></view>
        <text>探头校正</text>
      </view>
      
      <view class="calibration-card">
        <view class="calibration-item">
          <view class="calibration-label">校正值:</view>
          <view class="calibration-controls">
            <button class="control-btn" @tap="adjustCalibration(-1)">-</button>
            <input class="calibration-input" type="number" v-model="sensorInfo.adjustValue" placeholder="0.00" />
            <button class="control-btn" @tap="adjustCalibration(1)">+</button>
          </view>
          <!-- <view class="calibration-hint">范围：-1024.00 到 1024.00</view> -->
        </view>
      </view>
    </view>
 


    <!-- 时间信息 -->
    <view class="section">
      <view class="section-title">
        <view class="title-bar"></view>
        <text>时间信息</text>
      </view>
      
      <view class="time-card">
        <view class="time-item">
          <view class="time-label">创建时间:</view>
          <view class="time-value">{{ sensorInfo.createdAt || "--" }}</view>
        </view>
        <view class="time-item">
          <view class="time-label">更新时间:</view>
          <view class="time-value">{{ sensorInfo.updatedAt || "--" }}</view>
        </view>
      </view>
    </view>

       <!-- 保存按钮 -->
    <view class="footer">
      <button class="save-btn" @tap="handleSave">保存</button>
    </view>
    
  </view>
</template>

<script>
import request from "@/utils/request.js";

export default {
  name: "SensorDetail",
  computed: {
    sensorInfo() {
      return this.$store.state.app.currentSensor;
    },
  },
  methods: {
    // 调整校正值
    adjustCalibration(step) {
      const currentValue = parseFloat(this.sensorInfo.adjustValue) || 0;
      let newValue = currentValue + step;
      
      // 保留两位小数
      this.sensorInfo.adjustValue = newValue.toFixed(2);
      this.$store.commit("deviceDetail/SET_CURRENT_SENSOR", this.sensorInfo);
    },
    
    // 保存传感器信息
    async handleSave() {
      try {
        // 验证必填字段
        if (!this.sensorInfo.sensorName || !this.sensorInfo.sensorName.trim()) {
          uni.showToast({
            title: "请输入传感器名称",
            icon: "none",
          });
          return;
        }
        
        if (!this.sensorInfo.sensorCode || !this.sensorInfo.sensorCode.trim()) {
          uni.showToast({
            title: "请输入传感器编码",
            icon: "none",
          });
          return;
        }
        
        // 验证校正值范围
        const adjustValue = parseFloat(this.sensorInfo.adjustValue);
        if (isNaN(adjustValue) || adjustValue < -1024 || adjustValue > 1024) {
          uni.showToast({
            title: "校正值必须在 -1024 到 1024 之间",
            icon: "none",
          });
          return;
        }
        
        // 发送更新请求
        await request.put(`/sensor/update`, {
          id: this.sensorInfo.id,
          sensorName: this.sensorInfo.sensorName.trim(),
          sensorCode: this.sensorInfo.sensorCode.trim(),
          adjustValue: adjustValue,
        });
        
        uni.showToast({
          title: "保存成功",
          icon: "success",
        });
        
        // 延迟返回上一页
        setTimeout(() => {
          uni.navigateBack();
        }, 500);
      } catch (err) {
        console.log("保存失败", err);
        uni.showToast({
          title: err.msg || "保存失败",
          icon: "none",
        });
      }
    },
  },
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--primary-bg);
  padding: 0 30rpx 150rpx;
}

/* 章节 */
.section {
  margin-bottom: 40rpx;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.title-bar {
  width: 6rpx;
  height: 40rpx;
  background-color: var(--accent-color);
  border-radius: 3rpx;
  margin-right: 20rpx;
}

.section-title text {
  color: var(--text-primary);
  font-size: 30rpx;
  font-weight: bold;
}

/* 当前值卡片 */
.value-card {
  background-color: var(--card-bg);
  border-radius: 16rpx;
  padding: 40rpx;
  text-align: center;
  border: 2rpx solid var(--accent-color);
}

.value-display {
  font-size: 64rpx;
  font-weight: bold;
  color: var(--success-color);
}

/* 设置卡片 */
.setting-card {
  background-color: var(--card-bg);
  border-radius: 16rpx;
  padding: 20rpx;
}

.setting-item {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-label {
  color: var(--text-secondary);
  font-size: 26rpx;
  width: 200rpx;
  flex-shrink: 0;
}

.setting-input {
  flex: 1;
  background-color: #272734;
  border-radius: 8rpx;
  padding: 10rpx 20rpx;
  color: var(--text-primary);
  font-size: 26rpx;
  border: 2rpx solid transparent;
}

.setting-input:focus {
  border-color: var(--accent-color);
}

.readonly-value {
  flex: 1;
  color: var(--text-primary);
  font-size: 26rpx;
}

/* 探头校正 */
.calibration-card {
  background-color: var(--card-bg);
  border-radius: 16rpx;
  padding: 30rpx;
}

.calibration-item {
  margin-bottom: 20rpx;
}

.calibration-label {
  color: var(--text-secondary);
  font-size: 26rpx;
  margin-bottom: 20rpx;
}

.calibration-controls {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.control-btn {
  flex: 1;
  height: 60rpx;
  background-color: var(--accent-color);
  color: var(--text-primary);
  border: none;
  border-radius: 8rpx;
  font-size: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.calibration-input {
  flex: 2;
  background-color: #272734;
  border-radius: 8rpx;
  padding: 20rpx;
  color: var(--text-primary);
  font-size: 26rpx;
  text-align: center;
  border: 2rpx solid transparent;
}

.calibration-input:focus {
  border-color: var(--accent-color);
}

.calibration-hint {
  color: var(--text-muted);
  font-size: 22rpx;
  margin-top: 10rpx;
}

/* 时间卡片 */
.time-card {
  background-color: var(--card-bg);
  border-radius: 16rpx;
  padding: 20rpx;
}

.time-item {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.time-item:last-child {
  margin-bottom: 0;
}

.time-label {
  color: var(--text-secondary);
  font-size: 26rpx;
  width: 200rpx;
  flex-shrink: 0;
}

.time-value {
  flex: 1;
  color: var(--text-primary);
  font-size: 26rpx;
}

/* 底部保存按钮 */
.footer {
  /* position: fixed; */
  /* bottom: 0; */
  /* left: 0; */
  /* right: 0; */
  padding: 30rpx;
  background-color: var(--primary-bg);
  /* border-top: 1rpx solid var(--border-color); */
}

.save-btn {
  width: 50%;
  height: 70rpx;
  line-height: 70rpx;
  background-color: var(--accent-color);
  color: var(--text-primary);
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
}
</style>

