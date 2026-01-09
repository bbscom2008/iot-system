<template>
  <view class="page">
    <!-- TabBar 手动/自动 -->
    <view class="tabbar">
      <view
        class="tab-item"
        :class="{ active: currMotor.isAuto === 0 }"
        @tap="switchMode(0)"
      >
        手动
      </view>
      <view
        class="tab-item"
        :class="{ active: currMotor.isAuto === 1 }"
        @tap="switchMode(1)"
      >
        自动
      </view>
    </view>

    <!-- 手动模式 -->
    <view v-if="currMotor.isAuto === 0" class="manual-content">
      <!-- 设备信息 -->
      <view class="device-header">
        <view class="device-circle">
          <text class="device-number">{{ currMotor.value }}</text>
        </view>
        <input
          class="form-input-full device-name"
          v-model="currMotor.deviceName"
          placeholder="请输入设备名称"
        />
      </view>

      <!-- 手动转速设置 -->
      <view class="form-item">
        <text class="form-label">手动转速:</text>
        <input
          class="form-input"
          v-model="currMotor.manualSpeed"
          placeholder="10"
        />
        <text class="form-unit">%</text>
      </view>

      <!-- 运行时间设置 -->
      <view class="form-item">
        <text class="form-label">运行时间:</text>
        <input class="form-input-small" v-model="runMinutes" placeholder="0" />
        <text class="form-unit-small">分</text>
        <input class="form-input-small" v-model="runSeconds" placeholder="0" />
        <text class="form-unit-small">秒</text>
      </view>

      <!-- 暂停时间设置 -->
      <view class="form-item">
        <text class="form-label">暂停时间:</text>
        <input
          class="form-input-small"
          v-model="pauseMinutes"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="pauseSeconds"
          placeholder="0"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <!-- 说明文字 -->
      <view class="instructions">
        <text class="instruction-item"
          >1、循环设置方法：设置所需运行时间和暂停时间,变频设备按照手动转速循环启停。</text
        >
        <text class="instruction-item"
          >2、长转设置方法：设置运行时间为0分1秒,暂停时间为0分0秒。</text
        >
      </view>
    </view>

    <!-- 自动模式 -->
    <view v-if="currMotor.isAuto === 1" class="auto-content">
      <!-- 控制选项：火控、温控、气体 -->
      <view class="control-options">
        <view
          class="control-option"
          :class="{ active: currMotor.controlType === 1 }"
          @tap="switchControl(1)"
        >
          <view class="control-icon temp-icon">
            <SvgIcon
              name="temperature"
              :color="currMotor.controlType === 1 ? 'red' : '#CCCCCC'"
              :fill="currMotor.controlType === 1 ? 'red' : '#CCCCCC'"
            />
          </view>
          <text class="control-label">温控</text>
        </view>

        <view
          class="control-option"
          :class="{ active: currMotor.controlType === 2 }"
          @tap="switchControl(2)"
        >
          <view class="control-icon fire-icon">
            <SvgIcon
              name="humidity"
              :color="currMotor.controlType === 2 ? 'red' : '#CCCCCC'"
              :fill="currMotor.controlType === 2 ? 'red' : '#CCCCCC'"
            />
          </view>
          <text class="control-label">湿控</text>
        </view>

        <view
          class="control-option"
          :class="{ active: currMotor.controlType === 3 }"
          @tap="switchControl(3)"
        >
          <view class="control-icon gas-icon">
            <SvgIcon
              name="gas"
              :color="currMotor.controlType === 3 ? 'red' : '#CCCCCC'"
              :fill="currMotor.controlType === 3 ? 'red' : '#CCCCCC'"
            />
          </view>
          <text class="control-label">气体</text>
        </view>
      </view>

      

      <!-- 温控内容 -->
      <view v-if="currMotor.controlType === 1" class="control-content">
        <view class="device-header">
          <view class="device-circle">
            <text class="device-number">{{ currMotor.value }}</text>
          </view>
          <input
            class="form-input-full device-name"
            v-model="currMotor.deviceName"
            placeholder="请输入设备名称"
          />
          <text class="realtime-value">实时温度: {{ realtimeTemp }}°C</text>
        </view>

        <view class="form-item">
          <text class="form-label">探头选择:</text>
          <picker
            class="picker-wrapper"
            @change="onProbeChange"
            :value="probeIndex"
            :range="temperatureSensors"
            range-key="sensorName"
          >
            <view class="picker-input">
              <text :class="{ 'placeholder': probeIndex < 0 }">
                {{ probeIndex >= 0 ? temperatureSensors[probeIndex].sensorName : '请选择探头' }}
              </text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">保护转速:</text>
          <input
            class="form-input"
            v-model="currMotor.protectSpeed"
            placeholder="10"
          />
          <text class="form-unit">%</text>
        </view>

        <view class="form-item">
          <text class="form-label">温度上限:</text>
          <input
            class="form-input"
            v-model="currMotor.tempUpper"
            placeholder="35"
          />
          <text class="form-unit">°C</text>
        </view>

        <view class="form-item">
          <text class="form-label">温度下限:</text>
          <input
            class="form-input"
            v-model="currMotor.tempLower"
            placeholder="33"
          />
          <text class="form-unit">°C</text>
        </view>

        <view class="form-item">
          <text class="form-label">运行时间:</text>
          <input
            class="form-input-small"
            v-model="runMinutes"
            placeholder="0"
          />
          <text class="form-unit-small">分</text>
          <input
            class="form-input-small"
            v-model="runSeconds"
            placeholder="0"
          />
          <text class="form-unit-small">秒</text>
        </view>

        <view class="form-item">
          <text class="form-label">暂停时间:</text>
          <input
            class="form-input-small"
            v-model="pauseMinutes"
            placeholder="0"
          />
          <text class="form-unit-small">分</text>
          <input
            class="form-input-small"
            v-model="pauseSeconds"
            placeholder="0"
          />
          <text class="form-unit-small">秒</text>
        </view>

        <view class="instructions">
          <text class="instruction-item"
            >1、实时温度大于等于温度上限值全速运行,在温度上下限之间根据实时温度变速运行。</text
          >
          <text class="instruction-item"
            >2、下限以下循环设置方法：低于温度下限时根据运行时间和暂停时间按照保护转速循环运行。</text
          >
          <text class="instruction-item"
            >3、低温关闭方法：设置运行时间为0分0秒,暂停时间为0分1秒。</text
          >
          <text class="instruction-item"
            >4、低温常开方法：设置运行时间为0分1秒,暂停时间为0分0秒。</text
          >
        </view>
      </view>

      <!-- 湿控内容 -->
      <view v-if="currMotor.controlType === 2" class="control-content">
        <view class="device-header">
          <view class="device-circle">
            <text class="device-number">{{ currMotor.value }}</text>
          </view>
          <input
            class="form-input-full device-name"
            v-model="currMotor.deviceName"
            placeholder="请输入设备名称"
          />
          <text class="realtime-value">实时湿度: {{ humiditySensor.sensorValue }} %</text>
        </view>

        <view class="form-item">
          <text class="form-label">保护转速:</text>
          <input
            class="form-input"
            v-model="currMotor.protectSpeed"
            placeholder="10"
          />
          <text class="form-unit">%</text>
        </view>

        <view class="form-item">
          <text class="form-label">湿度上限:</text>
          <input
            class="form-input"
            v-model="currMotor.humidityUpper"
            placeholder="70"
          />
          <text class="form-unit">%</text>
        </view>

        <view class="form-item">
          <text class="form-label">湿度下限:</text>
          <input
            class="form-input"
            v-model="currMotor.humidityLower"
            placeholder="50"
          />
          <text class="form-unit">%</text>
        </view>

        <view class="form-item">
          <text class="form-label">运行时间:</text>
          <input
            class="form-input-small"
            v-model="runMinutes"
            placeholder="0"
          />
          <text class="form-unit-small">分</text>
          <input
            class="form-input-small"
            v-model="runSeconds"
            placeholder="0"
          />
          <text class="form-unit-small">秒</text>
        </view>

        <view class="form-item">
          <text class="form-label">暂停时间:</text>
          <input
            class="form-input-small"
            v-model="pauseMinutes"
            placeholder="0"
          />
          <text class="form-unit-small">分</text>
          <input
            class="form-input-small"
            v-model="pauseSeconds"
            placeholder="0"
          />
          <text class="form-unit-small">秒</text>
        </view>

        <view class="instructions">
          <text class="instruction-item"
            >1、实时湿度大于等于湿度上限值全速运行,在湿度上下限之间根据实时湿度变速运行。</text
          >
          <text class="instruction-item"
            >2、下限以下循环设置方法：低于湿度下限时根据运行时间和暂停时间按照保护转速循环运行。</text
          >
          <text class="instruction-item"
            >3、循环关闭方法：设置运行时间为0分0秒,暂停时间为0分1秒。</text
          >
          <text class="instruction-item"
            >4、循环常开方法：设置运行时间为0分1秒,暂停时间为0分0秒。</text
          >
        </view>
      </view>

      <!-- 气体内容 -->
      <view v-if="currMotor.controlType === 3" class="control-content">
        <view class="device-header">
          <view class="device-circle">
            <text class="device-number">{{ currMotor.value }}</text>
          </view>
          <input
            class="form-input-full device-name"
            v-model="currMotor.deviceName"
            placeholder="请输入设备名称"
          />
          <text class="realtime-value">实时气体: {{ gasSensor.sensorValue }} ppm</text>
        </view>

        <view class="form-item">
          <text class="form-label">保护转速:</text>
          <input
            class="form-input"
            v-model="currMotor.protectSpeed"
            placeholder="10"
          />
          <text class="form-unit">%</text>
        </view>

        <view class="form-item">
          <text class="form-label">气体上限:</text>
          <input
            class="form-input"
            v-model="currMotor.gasUpper"
            placeholder="70"
          />
          <text class="form-unit">°C</text>
        </view>

        <view class="form-item">
          <text class="form-label">气体下限:</text>
          <input
            class="form-input"
            v-model="currMotor.gasLower"
            placeholder="50"
          />
          <text class="form-unit">°C</text>
        </view>

        <view class="form-item">
          <text class="form-label">运行时间:</text>
          <input
            class="form-input-small"
            v-model="runMinutes"
            placeholder="0"
          />
          <text class="form-unit-small">分</text>
          <input
            class="form-input-small"
            v-model="runSeconds"
            placeholder="0"
          />
          <text class="form-unit-small">秒</text>
        </view>

        <view class="form-item">
          <text class="form-label">暂停时间:</text>
          <input
            class="form-input-small"
            v-model="pauseMinutes"
            placeholder="0"
          />
          <text class="form-unit-small">分</text>
          <input
            class="form-input-small"
            v-model="pauseSeconds"
            placeholder="0"
          />
          <text class="form-unit-small">秒</text>
        </view>

        <view class="instructions">
          <text class="instruction-item"
            >1、实时气体大于等于气体上限值全速运行,在气体上下限之间根据实时气体浓度变速运行。</text
          >
          <text class="instruction-item"
            >2、下限以下循环设置方法：低于气体下限时根据运行时间和暂停时间按照保护转速循环运行。</text
          >
          <text class="instruction-item"
            >3、循环关闭方法：设置运行时间为0分0秒,暂停时间为0分1秒。</text
          >
          <text class="instruction-item"
            >4、循环常开方法：设置运行时间为0分1秒,暂停时间为0分0秒。</text
          >
        </view>
      </view>
    </view>

    <!-- 保存按钮 -->
    <button class="save-btn" @tap="handleSave">保存</button>
  </view>
</template>

<script>
import SvgIcon from "@/components/SvgIcon.vue";
import request from "@/utils/request";

export default {
  name: "FrequencyConverter",
  components: {
    SvgIcon,
  },
  computed: {
    currMotor() {
      return this.$store.state.deviceDetail.currentFrequencyMotor;
    },
    // 温度传感器列表
    temperatureSensors() {
      return this.$store.state.deviceDetail.deviceInfo.sensors.filter(sensor => sensor.sensorTypeId === 5);
    },
    // 湿度传感器
    humiditySensor() {
      return this.$store.state.deviceDetail.deviceInfo.sensors.find(sensor => sensor.sensorTypeId === 6);
    },
    // 气体传感器
    gasSensor() {
      return this.$store.state.deviceDetail.deviceInfo.sensors.find(sensor => sensor.sensorTypeId === 7);
    },
    
  },
  data() {
    return {
      runMinutes: 0,
      runSeconds: 0,
      pauseMinutes: 0,
      pauseSeconds: 0,
	  realtimeTemp: 15,
	  probeIndex: -1,
      
    };
  },
  onLoad() {
    this.runMinutes = Math.floor(this.currMotor.runTime / 60);
    this.runSeconds = this.currMotor.runTime % 60;
    this.pauseMinutes = Math.floor(this.currMotor.pauseTime / 60);
    this.pauseSeconds = this.currMotor.pauseTime % 60;
    
    // 设置已选中的探头索引
    if (this.currMotor.tempSensorId && this.temperatureSensors) {
      this.probeIndex = this.temperatureSensors.findIndex(sensor => sensor.id === this.currMotor.tempSensorId);
    }
  },
  onUnload() {},
  methods: {
    switchMode(isAuto) {
      this.currMotor.isAuto = isAuto;
    },
    switchControl(controlType) {
      this.currMotor.controlType = controlType;
    },
    // 探头选择变化
    onProbeChange(e) {
      this.probeIndex = parseInt(e.detail.value);
      if (this.probeIndex >= 0 && this.temperatureSensors && this.temperatureSensors[this.probeIndex]) {
        this.currMotor.tempSensorId = this.temperatureSensors[this.probeIndex].id;
      }
    },
    async handleSave() {
      try {
        // 计算运行时间和暂停时间（秒）
        const runTime = (this.runMinutes * 60) + this.runSeconds;
        const pauseTime = (this.pauseMinutes * 60) + this.pauseSeconds;
        
        // 更新变频器数据
        const updateData = {
          id: this.currMotor.id,
          isAuto: this.currMotor.isAuto,
          manualSpeed: this.currMotor.manualSpeed,
          protectSpeed: this.currMotor.protectSpeed,
          runTime: runTime,
          pauseTime: pauseTime,
          controlType: this.currMotor.controlType,
          value: this.currMotor.value,
          tempSensorId: this.currMotor.tempSensorId,
          tempUpper: this.currMotor.tempUpper,
          tempLower: this.currMotor.tempLower,
          humidityUpper: this.currMotor.humidityUpper,
          humidityLower: this.currMotor.humidityLower,
          gasUpper: this.currMotor.gasUpper,
          gasLower: this.currMotor.gasLower,
          deviceName: this.currMotor.deviceName,
        };
        
        // 调用更新接口
        await request.put('/frequencyMotor/update', updateData);
        
        uni.showToast({
          title: "保存成功",
          icon: "success",
        });
        
        // 延迟返回上一页
        // setTimeout(() => {
        //   uni.navigateBack();
        // }, 1500);
      } catch (error) {
        console.error('保存失败:', error);
        uni.showToast({
          title: error.message || "保存失败",
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
  background: linear-gradient(135deg, #04041a 0%, #0a0a2e 100%);
  padding: 20rpx 40rpx 100rpx;
}

/* 导航栏 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  margin-bottom: 20rpx;
}

.navbar-back {
  padding: 10rpx;
}

.navbar-title {
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
}

.navbar-actions {
  display: flex;
  gap: 20rpx;
}

.nav-icon {
  color: #fff;
  font-size: 32rpx;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(106, 90, 205, 0.3);
}

/* TabBar */
.tabbar {
  display: flex;
  justify-content: center;
  margin-bottom: 40rpx;
  gap: 0;
}

.tab-item {
  width: 200rpx;
  height: 70rpx;
  line-height: 70rpx;
  text-align: center;
  color: #fff;
  font-size: 28rpx;
  background: #1a1a3a;
  border-radius: 0;
  border: 2rpx solid var(--accent-color);
}

.tab-item:first-child {
  border-radius: 40rpx 0 0 40rpx;
}

.tab-item:last-child {
  border-radius: 0 40rpx 40rpx 0;
}

.tab-item.active {
  background: var(--accent-color);
  /* color: #04041A; */
  font-weight: bold;
  border: 2rpx solid var(--accent-color);
}

/* 手动模式内容 */
.manual-content {
  padding-bottom: 40rpx;
}

/* 设备头部 */
.device-header {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  gap: 20rpx;
}

.device-circle {
  width: 60rpx;
  height: 60rpx;
  border: 3rpx dashed #00ffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(106, 90, 205, 0.1);
}

.device-number {
  color: #00ffff;
  font-size: 32rpx;
  font-weight: bold;
}

.device-name {
  color: #fff;
  font-size: 28rpx;
  font-weight: bold;
}

.realtime-value {
  color: #fff;
  font-size: 26rpx;
  margin-left: auto;
}

/* 表单项 */
.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  gap: 15rpx;
}

.form-label {
  color: #fff;
  font-size: 26rpx;
  width: 200rpx;
  text-align: right;
  margin-right: 20rpx;
}

.form-input {
  /* flex: 1; */
  width: 300rpx;
  height: 60rpx;
  background: #1c1c26;
  border: 2rpx solid rgba(106, 90, 205, 0.3);
  border-radius: 12rpx;
  padding: 0 25rpx;
  color: #fff;
  font-size: 28rpx;
}

.form-input-full {
  width: 300rpx;
  height: 60rpx;
  /* background: #1c1c26; */
  border-bottom: 2rpx solid rgba(106, 90, 205, 0.3);
  border-radius: 12rpx;
  padding: 0 25rpx;
  color: #fff;
  font-size: 28rpx;
}

.form-input-small {
  width: 100rpx;
  height: 60rpx;
  background: #1c1c26;
  border: 2rpx solid rgba(106, 90, 205, 0.3);
  border-radius: 12rpx;
  padding: 0 20rpx;
  color: #fff;
  font-size: 28rpx;
}

.form-unit {
  color: #fff;
  font-size: 26rpx;
}

.form-unit-small {
  color: #fff;
  font-size: 26rpx;
}

/* 说明文字 */
.instructions {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-top: 40rpx;
  padding: 30rpx;
  background: var(--card-bg);
  border-radius: 12rpx;
}

.instruction-item {
  color: var(--accent-cyan);
  font-size: 24rpx;
  line-height: 1.8;
}

/* 自动模式控制选项 */
.control-options {
  display: flex;
  justify-content: center;
  gap: 40rpx;
  margin-bottom: 40rpx;
}

.control-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 120rpx;
  gap: 15rpx;
  padding: 20rpx;
  border-radius: 40rpx;
  background: #1a1a3a;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.control-option.active {
  background: rgba(0, 255, 255, 0.1);
  border-color: #00ffff;
}

.control-icon {
  width: 90rpx;
  height: 90rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 50rpx;
  border-radius: 50%;
  background: rgba(26, 26, 58, 0.5);
}

.fire-icon {
  background: linear-gradient(
    135deg,
    rgba(255, 69, 0, 0.3),
    rgba(255, 165, 0, 0.3)
  );
}

.temp-icon {
  background: linear-gradient(
    135deg,
    rgba(220, 20, 60, 0.3),
    rgba(255, 105, 180, 0.3)
  );
}

.gas-icon {
  background: linear-gradient(
    135deg,
    rgba(70, 130, 180, 0.3),
    rgba(135, 206, 250, 0.3)
  );
}

/* 火焰图标 */
.flame-icon {
  width: 40rpx;
  height: 60rpx;
  background: linear-gradient(180deg, #ff4500 0%, #ffa500 50%, #ffd700 100%);
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  position: relative;
  box-shadow: 0 0 20rpx rgba(255, 69, 0, 0.5);
}

.flame-icon::before {
  content: "";
  position: absolute;
  width: 25rpx;
  height: 40rpx;
  background: linear-gradient(180deg, #ff6347 0%, #ffd700 100%);
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  left: 50%;
  top: 10rpx;
  transform: translateX(-50%);
}

/* 温度计图标 */
.thermometer-icon {
  width: 20rpx;
  height: 60rpx;
  background: linear-gradient(180deg, #ff6347 0%, #ffd700 70%);
  border-radius: 20rpx;
  position: relative;
  box-shadow: 0 0 20rpx rgba(255, 105, 180, 0.5);
}

.thermometer-icon::before {
  content: "";
  position: absolute;
  width: 30rpx;
  height: 30rpx;
  background: #ff6347;
  border-radius: 50%;
  left: -5rpx;
  bottom: -5rpx;
}

.gas-text {
  color: #87ceeb;
  font-size: 24rpx;
  font-weight: bold;
}

.control-label {
  color: #fff;
  font-size: 24rpx;
}

.control-option.active .control-label {
  color: #00ffff;
  font-weight: bold;
}

/* 保存按钮 */
.save-btn {
  width: 60%;
  height: 80rpx;
  line-height: 80rpx;
  background: #6a5acd;
  color: #ffffff;
  border-radius: 8rpx;
  text-align: center;
  font-size: 32rpx;
  margin-top: 40rpx;
  border: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.save-btn:active {
   background-color: #5a4acd;
 }

/* 选择器样式 */
.picker-wrapper {
  width: 300rpx;
  height: 60rpx;
}

.picker-input {
  width: 300rpx;
  height: 60rpx;
  box-sizing: content-box;
  background-color: #1c1c26;
  border-radius: 12rpx;
  padding: 0 25rpx;
  border: 2rpx solid rgba(106, 90, 205, 0.3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: border-color 0.3s;
}

.picker-input text {
  color: #fff;
  font-size: 28rpx;
}

.picker-input .placeholder {
  color: #999;
}
</style>
