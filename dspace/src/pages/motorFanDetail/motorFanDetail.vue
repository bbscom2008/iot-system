<template>
  <view class="page">
    <!-- 控制模式选项 -->
    <view class="control-options">
      <view
        class="control-option"
        :class="{ active: controlMode === 1 }"
        @tap="switchControlMode(1)"
      >
        <view class="control-icon temp-icon">
          <SvgIcon
            name="temperature"
            :color="controlMode === 1 ? 'red' : '#CCCCCC'"
            :fill="controlMode === 1 ? 'red' : '#CCCCCC'"
          />
        </view>
        <text class="control-label">温控</text>
      </view>

      <view
        class="control-option"
        :class="{ active: controlMode === 2 }"
        @tap="switchControlMode(2)"
      >
        <view class="control-icon fire-icon">
          <SvgIcon
            name="recycle"
            :color="controlMode === 2 ? '#00ff00' : '#CCCCCC'"
            :fill="controlMode === 2 ? '#00ff00' : '#CCCCCC'"
          />
        </view>
        <text class="control-label">循环</text>
      </view>

      <view
        class="control-option"
        :class="{ active: controlMode === 3 }"
        @tap="switchControlMode(3)"
      >
        <view class="control-icon humidity-icon">
          <SvgIcon
            name="humidity"
            :color="controlMode === 3 ? '#FFA500' : '#CCCCCC'"
            :fill="controlMode === 3 ? '#FFA500' : '#CCCCCC'"
          />
        </view>
        <text class="control-label">湿控</text>
      </view>

      <view
        class="control-option"
        :class="{ active: controlMode === 4 }"
        @tap="switchControlMode(4)"
      >
        <view class="control-icon gas-icon">
          <SvgIcon
            name="gas"
            :color="controlMode === 4 ? '#87CEEB' : '#CCCCCC'"
            :fill="controlMode === 4 ? '#87CEEB' : '#CCCCCC'"
          />
        </view>
        <text class="control-label">气体</text>
      </view>

      <view
        class="control-option"
        :class="{ active: controlMode === 5 }"
        @tap="switchControlMode(5)"
      >
        <view class="control-icon timer-icon">
          <SvgIcon
            name="timer"
            :color="controlMode === 5 ? '#9370DB' : '#CCCCCC'"
            :fill="controlMode === 5 ? '#9370DB' : '#CCCCCC'"
          />
        </view>
        <text class="control-label">定时</text>
      </view>
    </view>

    <!-- 状态切换栏 -->
    <view class="status-bar">
      <view
        class="status-item"
        :class="{ active: autoMode === 1 }"
        @tap="switchAutoMode(1)"
      >
        自动
      </view>
      <view
        class="status-item"
        :class="{ active: autoMode === 2 }"
        @tap="switchAutoMode(2)"
      >
        开
      </view>
      <view
        class="status-item"
        :class="{ active: autoMode === 3 }"
        @tap="switchAutoMode(3)"
      >
        关
      </view>
    </view>

    <!-- 风机信息 -->
    <view class="fan-info">
      <view class="fan-icon-wrapper">
        <SvgIcon name="fan" color="#6a5acd" :fill="'#6a5acd'" />
      </view>
      <input class="fan-name-input" v-model="fanName" placeholder="请输入风机名称" />
      <text class="realtime-value">实时温度: {{ realtimeTemp }}°C</text>
    </view>

    <!-- 温控模式内容 -->
    <view v-if="controlMode === 1" class="control-content">
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
            <text :class="{ placeholder: probeIndex < 0 }">
              {{ probeIndex >= 0 ? temperatureSensors[probeIndex].sensorName : '请选择探头' }}
            </text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">启动温度:</text>
        <input
          class="form-input"
          v-model="tempUpper"
          type="number"
          placeholder="35"
        />
        <text class="form-unit">°C</text>
      </view>

      <view class="form-item">
        <text class="form-label">停止温度:</text>
        <input
          class="form-input"
          v-model="tempLower"
          type="number"
          placeholder="10"
        />
        <text class="form-unit">°C</text>
      </view>

      <view class="form-item">
        <text class="form-label">低温运行:</text>
        <input
          class="form-input-small"
          v-model="runMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="runSeconds"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="form-item">
        <text class="form-label">低温暂停:</text>
        <input
          class="form-input-small"
          v-model="pauseMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="pauseSeconds"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="instructions">
        <text class="instruction-item">1、实时温度达到或超过温度上限，设备一直工作。</text>
        <text class="instruction-item">2、实时温度在温度上限与温度下限之间，设备按照运行时间和暂停时间循环工作。</text>
        <text class="instruction-item">3、实时温度低于温度下限，设备停止工作。</text>
      </view>
    </view>

    <!-- 循环模式内容 -->
    <view v-if="controlMode === 2" class="control-content">
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
            <text :class="{ placeholder: probeIndex < 0 }">
              {{ probeIndex >= 0 ? temperatureSensors[probeIndex].sensorName : '请选择探头' }}
            </text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">温度上限:</text>
        <input
          class="form-input"
          v-model="startTemp"
          type="number"
          placeholder="35"
        />
        <text class="form-unit">°C</text>
      </view>

      <view class="form-item">
        <text class="form-label">温度下限:</text>
        <input
          class="form-input"
          v-model="stopTemp"
          type="number"
          placeholder="10"
        />
        <text class="form-unit">°C</text>
      </view>

      <view class="form-item">
        <text class="form-label">运行时间:</text>
        <input
          class="form-input-small"
          v-model="lowTempRunMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="lowTempRunSeconds"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="form-item">
        <text class="form-label">暂停时间:</text>
        <input
          class="form-input-small"
          v-model="lowTempPauseMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="lowTempPauseSeconds"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="instructions">
        <text class="instruction-item">1、实时温度大于等于启动温度值启动设备，低于停止温度值设备停止工作。</text>
        <text class="instruction-item">2、低温循环设置方法（最小通风模式）：设置所需低温运行时间和低温暂停时间即可。</text>
        <text class="instruction-item">3、低温循环关闭方法：设置低温运行为0分0秒，低温暂停为0分1秒。</text>
      </view>
    </view>

    <!-- 湿控模式内容 -->
    <view v-if="controlMode === 3" class="control-content">
      <view class="form-item">
        <text class="form-label">湿度上限:</text>
        <input
          class="form-input"
          v-model="humidityUpper"
          type="number"
          placeholder="90"
        />
        <text class="form-unit">%</text>
      </view>

      <view class="form-item">
        <text class="form-label">湿度下限:</text>
        <input
          class="form-input"
          v-model="humidityLower"
          type="number"
          placeholder="30"
        />
        <text class="form-unit">%</text>
      </view>

      <view class="form-item">
        <text class="form-label">运行时间:</text>
        <input
          class="form-input-small"
          v-model="runMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="runSeconds"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="form-item">
        <text class="form-label">暂停时间:</text>
        <input
          class="form-input-small"
          v-model="pauseMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="pauseSeconds"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="instructions">
        <text class="instruction-item">1、实时湿度大于等于湿度上限值启动设备，低于湿度下限值设备停止工作。</text>
        <text class="instruction-item">2、循环设置方法：设置所需运行时间和暂停时间即可。</text>
        <text class="instruction-item">3、循环关闭方法：设置运行时间为0分0秒，暂停时间为0分1秒。</text>
      </view>
    </view>

    <!-- 气体模式内容 -->
    <view v-if="controlMode === 4" class="control-content">
      <view class="form-item">
        <text class="form-label">气体上限:</text>
        <input
          class="form-input"
          v-model="gasUpper"
          type="number"
          placeholder="35"
        />
        <text class="form-unit">ppm</text>
      </view>

      <view class="form-item">
        <text class="form-label">气体下限:</text>
        <input
          class="form-input"
          v-model="gasLower"
          type="number"
          placeholder="10"
        />
        <text class="form-unit">ppm</text>
      </view>

      <view class="form-item">
        <text class="form-label">运行时间:</text>
        <input
          class="form-input-small"
          v-model="runMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="runSeconds"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="form-item">
        <text class="form-label">暂停时间:</text>
        <input
          class="form-input-small"
          v-model="pauseMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="pauseSeconds"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="instructions">
        <text class="instruction-item">1、实时气体大于等于气体上限值启动设备，低于气体下限值设备停止工作。</text>
        <text class="instruction-item">2、循环设置方法：设置所需运行时间和暂停时间即可。</text>
        <text class="instruction-item">3、循环关闭方法：设置运行时间为0分0秒，暂停时间为0分1秒。</text>
      </view>
    </view>

    <!-- 定时模式内容 -->
    <view v-if="controlMode === 5" class="control-content">
      <!-- 定时组切换 -->
      <view class="timer-tabs">
        <view
          class="timer-tab"
          :class="{ active: currentTimerGroup === 1 }"
          @tap="switchTimerGroup(1)"
        >
          定时1
        </view>
        <view
          class="timer-tab"
          :class="{ active: currentTimerGroup === 2 }"
          @tap="switchTimerGroup(2)"
        >
          定时2
        </view>
        <view
          class="timer-tab"
          :class="{ active: currentTimerGroup === 3 }"
          @tap="switchTimerGroup(3)"
        >
          定时3
        </view>
      </view>

      <!-- 定时1开关 -->
      <view class="form-item switch-item">
        <text class="form-label">定时{{ currentTimerGroup }}开关</text>
        <MySwitch
          :value="timerGroups[currentTimerGroup - 1].enabled"
          @input="onTimerSwitchChange"
        />
      </view>

      <!-- 定时开启时间 -->
      <view class="form-item">
        <text class="form-label">定时{{ currentTimerGroup }}开:</text>
        <input
          class="form-input-small"
          :value="timerGroups[currentTimerGroup - 1].startHour"
          @input="updateTimerField(currentTimerGroup, 'startTime', `${$event.detail.value}:${timerGroups[currentTimerGroup - 1].startMinute}`)"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">时</text>
        <input
          class="form-input-small"
          :value="timerGroups[currentTimerGroup - 1].startMinute"
          @input="updateTimerField(currentTimerGroup, 'startTime', `${timerGroups[currentTimerGroup - 1].startHour}:${$event.detail.value}`)"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">分</text>
      </view>

      <!-- 定时关闭时间 -->
      <view class="form-item">
        <text class="form-label">定时{{ currentTimerGroup }}关:</text>
        <input
          class="form-input-small"
          :value="timerGroups[currentTimerGroup - 1].endHour"
          @input="updateTimerField(currentTimerGroup, 'endTime', `${$event.detail.value}:${timerGroups[currentTimerGroup - 1].endMinute}`)"
          type="number"
          placeholder="6"
        />
        <text class="form-unit-small">时</text>
        <input
          class="form-input-small"
          :value="timerGroups[currentTimerGroup - 1].endMinute"
          @input="updateTimerField(currentTimerGroup, 'endTime', `${timerGroups[currentTimerGroup - 1].endHour}:${$event.detail.value}`)"
          type="number"
          placeholder="6"
        />
        <text class="form-unit-small">分</text>
      </view>

      <!-- 探头选择 -->
      <view class="form-item">
        <text class="form-label">探头选择:</text>
        <picker
          class="picker-wrapper"
          @change="onTimerProbeChange"
          :value="timerGroups[currentTimerGroup - 1].probeIndex"
          :range="temperatureSensors"
          range-key="sensorName"
        >
          <view class="picker-input">
            <text :class="{ placeholder: timerGroups[currentTimerGroup - 1].probeIndex < 0 }">
              {{ timerGroups[currentTimerGroup - 1].probeIndex >= 0 ? temperatureSensors[timerGroups[currentTimerGroup - 1].probeIndex].sensorName : '请选择探头' }}
            </text>
          </view>
        </picker>
      </view>

      <!-- 启动温度 -->
      <view class="form-item">
        <text class="form-label">启动温度:</text>
        <input
          class="form-input"
          :value="timerGroups[currentTimerGroup - 1].startTemp"
          @input="updateTimerField(currentTimerGroup, 'startTemp', parseFloat($event.detail.value) || 0)"
          type="number"
          placeholder="20"
        />
        <text class="form-unit">°C</text>
      </view>

      <!-- 停止温度 -->
      <view class="form-item">
        <text class="form-label">停止温度:</text>
        <input
          class="form-input"
          :value="timerGroups[currentTimerGroup - 1].stopTemp"
          @input="updateTimerField(currentTimerGroup, 'stopTemp', parseFloat($event.detail.value) || 0)"
          type="number"
          placeholder="30"
        />
        <text class="form-unit">°C</text>
      </view>

      <view class="instructions">
        <text class="instruction-item">此模式为北京时间定时控制功能，分三个时间段:</text>
        <text class="instruction-item">定时开关打开：设备在设置的北京时间内，按照设定启动和停止温度执行。</text>
      </view>
    </view>

    <!-- 保存按钮 -->
    <button class="save-btn" @tap="handleSave">保存</button>
  </view>
</template>

<script>
import SvgIcon from "@/components/SvgIcon.vue";
import MySwitch from "@/components/MySwitch.vue";
import { request } from "@/utils/request";

export default {
  name: "MotorFanDetail",
  components: {
    SvgIcon,
    MySwitch,
  },
  computed: {
    currFan() {
      return this.$store.state.deviceDetail.currentMotorFan || {};
    },
    temperatureSensors() {
      // 从设备信息中获取温度传感器列表
      const deviceInfo = this.$store.state.deviceDetail.deviceInfo;
      if (deviceInfo && deviceInfo.sensors) {
        return deviceInfo.sensors.filter(sensor => sensor.sensorTypeId === 5);
      }
      return [];
    },
    // 风机名称
    fanName: {
      get() {
        return this.currFan.fanName || '';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "fanName",
          value,
        });
      },
    },
    // 控制模式
    controlMode: {
      get() {
        return this.currFan.controlMode || 1;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "controlMode",
          value,
        });
      },
    },
    // 自动模式
    autoMode: {
      get() {
        return this.currFan.autoMode || 1;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "autoMode",
          value,
        });
      },
    },
    // 探头索引
    probeIndex: {
      get() {
        if (!this.currFan.probeSensorId || !this.temperatureSensors.length) return -1;
        return this.temperatureSensors.findIndex(s => s.id === this.currFan.probeSensorId);
      },
      set(value) {
        const sensorId = value >= 0 && this.temperatureSensors[value] ? this.temperatureSensors[value].id : null;
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "probeSensorId",
          value: sensorId,
        });
      },
    },
    // 温度上限/启动温度
    tempUpper: {
      get() {
        return this.currFan.tempUpper !== undefined ? String(this.currFan.tempUpper) : '35';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tempUpper",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 温度下限/停止温度
    tempLower: {
      get() {
        return this.currFan.tempLower !== undefined ? String(this.currFan.tempLower) : '10';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tempLower",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 运行时间（分钟）
    runMinutes: {
      get() {
        return this.currFan.runTime ? Math.floor(this.currFan.runTime / 60) : 0;
      },
      set(value) {
        const minutes = parseInt(value) || 0;
        const seconds = this.currFan.runTime ? this.currFan.runTime % 60 : 0;
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "runTime",
          value: minutes * 60 + seconds,
        });
      },
    },
    // 运行时间（秒）
    runSeconds: {
      get() {
        return this.currFan.runTime ? this.currFan.runTime % 60 : 5;
      },
      set(value) {
        const seconds = parseInt(value) || 0;
        const minutes = this.currFan.runTime ? Math.floor(this.currFan.runTime / 60) : 0;
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "runTime",
          value: minutes * 60 + seconds,
        });
      },
    },
    // 暂停时间（分钟）
    pauseMinutes: {
      get() {
        return this.currFan.pauseTime ? Math.floor(this.currFan.pauseTime / 60) : 0;
      },
      set(value) {
        const minutes = parseInt(value) || 0;
        const seconds = this.currFan.pauseTime ? this.currFan.pauseTime % 60 : 0;
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "pauseTime",
          value: minutes * 60 + seconds,
        });
      },
    },
    // 暂停时间（秒）
    pauseSeconds: {
      get() {
        return this.currFan.pauseTime ? this.currFan.pauseTime % 60 : 5;
      },
      set(value) {
        const seconds = parseInt(value) || 0;
        const minutes = this.currFan.pauseTime ? Math.floor(this.currFan.pauseTime / 60) : 0;
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "pauseTime",
          value: minutes * 60 + seconds,
        });
      },
    },
    // 湿度上限
    humidityUpper: {
      get() {
        return this.currFan.humidityUpper !== undefined ? String(this.currFan.humidityUpper) : '90';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "humidityUpper",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 湿度下限
    humidityLower: {
      get() {
        return this.currFan.humidityLower !== undefined ? String(this.currFan.humidityLower) : '30';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "humidityLower",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 气体上限
    gasUpper: {
      get() {
        return this.currFan.gasUpper !== undefined ? String(this.currFan.gasUpper) : '3000';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "gasUpper",
          value: parseInt(value) || 0,
        });
      },
    },
    // 气体下限
    gasLower: {
      get() {
        return this.currFan.gasLower !== undefined ? String(this.currFan.gasLower) : '1000';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "gasLower",
          value: parseInt(value) || 0,
        });
      },
    },
    // 循环模式 - 启动温度（使用tempUpper）
    startTemp: {
      get() {
        return this.currFan.tempUpper !== undefined ? String(this.currFan.tempUpper) : '35';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tempUpper",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 循环模式 - 停止温度（使用tempLower）
    stopTemp: {
      get() {
        return this.currFan.tempLower !== undefined ? String(this.currFan.tempLower) : '10';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tempLower",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 循环模式 - 低温运行时间（分钟）
    lowTempRunMinutes: {
      get() {
        return this.currFan.runTime ? Math.floor(this.currFan.runTime / 60) : 0;
      },
      set(value) {
        const minutes = parseInt(value) || 0;
        const seconds = this.currFan.runTime ? this.currFan.runTime % 60 : 0;
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "runTime",
          value: minutes * 60 + seconds,
        });
      },
    },
    // 循环模式 - 低温运行时间（秒）
    lowTempRunSeconds: {
      get() {
        return this.currFan.runTime ? this.currFan.runTime % 60 : 0;
      },
      set(value) {
        const seconds = parseInt(value) || 0;
        const minutes = this.currFan.runTime ? Math.floor(this.currFan.runTime / 60) : 0;
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "runTime",
          value: minutes * 60 + seconds,
        });
      },
    },
    // 循环模式 - 低温暂停时间（分钟）
    lowTempPauseMinutes: {
      get() {
        return this.currFan.pauseTime ? Math.floor(this.currFan.pauseTime / 60) : 0;
      },
      set(value) {
        const minutes = parseInt(value) || 0;
        const seconds = this.currFan.pauseTime ? this.currFan.pauseTime % 60 : 0;
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "pauseTime",
          value: minutes * 60 + seconds,
        });
      },
    },
    // 循环模式 - 低温暂停时间（秒）
    lowTempPauseSeconds: {
      get() {
        return this.currFan.pauseTime ? this.currFan.pauseTime % 60 : 5;
      },
      set(value) {
        const seconds = parseInt(value) || 0;
        const minutes = this.currFan.pauseTime ? Math.floor(this.currFan.pauseTime / 60) : 0;
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "pauseTime",
          value: minutes * 60 + seconds,
        });
      },
    },
    // 定时组数据
    timerGroups() {
      return [
        {
          enabled: this.currFan.timer1Enabled === 1,
          startHour: this.currFan.timer1StartTime ? this.currFan.timer1StartTime.split(':')[0] : '5',
          startMinute: this.currFan.timer1StartTime ? this.currFan.timer1StartTime.split(':')[1] : '5',
          endHour: this.currFan.timer1EndTime ? this.currFan.timer1EndTime.split(':')[0] : '6',
          endMinute: this.currFan.timer1EndTime ? this.currFan.timer1EndTime.split(':')[1] : '6',
          probeIndex: this.currFan.timer1ProbeSensorId ? this.temperatureSensors.findIndex(s => s.id === this.currFan.timer1ProbeSensorId) : 0,
          startTemp: this.currFan.timer1StartTemp !== undefined ? String(this.currFan.timer1StartTemp) : '20',
          stopTemp: this.currFan.timer1StopTemp !== undefined ? String(this.currFan.timer1StopTemp) : '30',
        },
        {
          enabled: this.currFan.timer2Enabled === 1,
          startHour: this.currFan.timer2StartTime ? this.currFan.timer2StartTime.split(':')[0] : '12',
          startMinute: this.currFan.timer2StartTime ? this.currFan.timer2StartTime.split(':')[1] : '0',
          endHour: this.currFan.timer2EndTime ? this.currFan.timer2EndTime.split(':')[0] : '14',
          endMinute: this.currFan.timer2EndTime ? this.currFan.timer2EndTime.split(':')[1] : '0',
          probeIndex: this.currFan.timer2ProbeSensorId ? this.temperatureSensors.findIndex(s => s.id === this.currFan.timer2ProbeSensorId) : 0,
          startTemp: this.currFan.timer2StartTemp !== undefined ? String(this.currFan.timer2StartTemp) : '20',
          stopTemp: this.currFan.timer2StopTemp !== undefined ? String(this.currFan.timer2StopTemp) : '30',
        },
        {
          enabled: this.currFan.timer3Enabled === 1,
          startHour: this.currFan.timer3StartTime ? this.currFan.timer3StartTime.split(':')[0] : '18',
          startMinute: this.currFan.timer3StartTime ? this.currFan.timer3StartTime.split(':')[1] : '0',
          endHour: this.currFan.timer3EndTime ? this.currFan.timer3EndTime.split(':')[0] : '20',
          endMinute: this.currFan.timer3EndTime ? this.currFan.timer3EndTime.split(':')[1] : '0',
          probeIndex: this.currFan.timer3ProbeSensorId ? this.temperatureSensors.findIndex(s => s.id === this.currFan.timer3ProbeSensorId) : 0,
          startTemp: this.currFan.timer3StartTemp !== undefined ? String(this.currFan.timer3StartTemp) : '20',
          stopTemp: this.currFan.timer3StopTemp !== undefined ? String(this.currFan.timer3StopTemp) : '30',
        },
      ];
    },
    realtimeTemp(){
      if (this.probeIndex >= 0 && this.temperatureSensors[this.probeIndex]) {
        return this.temperatureSensors[this.probeIndex].sensorValue;
      }
      return '--';
    }
    
  },
  data() {
    return {
      currentTimerGroup: 1,
    };
  },
  onLoad() {
    console.log('风机详情:', this.currFan);
  },
  methods: {
    switchControlMode(mode) {
      this.controlMode = mode;
    },
    switchAutoMode(mode) {
      this.autoMode = mode;
    },
    onProbeChange(e) {
      this.probeIndex = parseInt(e.detail.value);
    },
    switchTimerGroup(group) {
      this.currentTimerGroup = group;
    },
    onTimerSwitchChange(value) {
      const field = `timer${this.currentTimerGroup}Enabled`;
      this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
        field,
        value: value ? 1 : 0,
      });
    },
    onTimerProbeChange(e) {
      const index = parseInt(e.detail.value);
      const sensorId = index >= 0 && this.temperatureSensors[index] ? this.temperatureSensors[index].id : null;
      const field = `timer${this.currentTimerGroup}ProbeSensorId`;
      this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
        field,
        value: sensorId,
      });
    },
    updateTimerField(timerNum, fieldName, value) {
      const field = `timer${timerNum}${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)}`;
      this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
        field,
        value,
      });
    },
    async handleSave() {
      if (!this.currFan.id) {
        uni.showToast({
          title: "风机信息不存在",
          icon: "none",
        });
        return;
      }

      try {
        // 准备要提交的数据
        const requestData = {
          id: this.currFan.id,
          fanName: this.currFan.fanName,
          controlMode: this.currFan.controlMode,
          autoMode: this.currFan.autoMode,
          probeSensorId: this.currFan.probeSensorId,
          tempUpper: this.currFan.tempUpper,
          tempLower: this.currFan.tempLower,
          runTime: this.currFan.runTime,
          pauseTime: this.currFan.pauseTime,
          humidityUpper: this.currFan.humidityUpper,
          humidityLower: this.currFan.humidityLower,
          gasUpper: this.currFan.gasUpper,
          gasLower: this.currFan.gasLower,
          timer1Enabled: this.currFan.timer1Enabled,
          timer1StartTime: `${this.timerGroups[0].startHour}:${this.timerGroups[0].startMinute}`,
          timer1EndTime: `${this.timerGroups[0].endHour}:${this.timerGroups[0].endMinute}`,
          timer1ProbeSensorId: this.currFan.timer1ProbeSensorId,
          timer1StartTemp: this.currFan.timer1StartTemp,
          timer1StopTemp: this.currFan.timer1StopTemp,
          timer2Enabled: this.currFan.timer2Enabled,
          timer2StartTime: `${this.timerGroups[1].startHour}:${this.timerGroups[1].startMinute}`,
          timer2EndTime: `${this.timerGroups[1].endHour}:${this.timerGroups[1].endMinute}`,
          timer2ProbeSensorId: this.currFan.timer2ProbeSensorId,
          timer2StartTemp: this.currFan.timer2StartTemp,
          timer2StopTemp: this.currFan.timer2StopTemp,
          timer3Enabled: this.currFan.timer3Enabled,
          timer3StartTime: `${this.timerGroups[2].startHour}:${this.timerGroups[2].startMinute}`,
          timer3EndTime: `${this.timerGroups[2].endHour}:${this.timerGroups[2].endMinute}`,
          timer3ProbeSensorId: this.currFan.timer3ProbeSensorId,
          timer3StartTemp: this.currFan.timer3StartTemp,
          timer3StopTemp: this.currFan.timer3StopTemp,
        };

        // 调用API保存
        const response = await request({
          url: '/motor-fan/update',
          method: 'PUT',
          data: requestData,
        });

        uni.showToast({
          title: "保存成功",
          icon: "success",
        });

        uni.navigateBack();

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

/* 控制模式选项 */
.control-options {
  display: flex;
  justify-content: space-evenly;
  gap: 30rpx;
  padding: 40rpx 0;

  flex-wrap: wrap;
}

.control-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 90rpx;
  gap: 10rpx;
  padding: 15rpx;
  border-radius: 20rpx;
  background: #1a1a3a;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.control-option.active {
  background: rgba(0, 255, 255, 0.1);
  border-color: #6a5acd;
}

.control-icon {
  width: 70rpx;
  height: 70rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  border-radius: 50%;
  background: rgba(26, 26, 58, 0.5);
}

.temp-icon {
  background: linear-gradient(
    135deg,
    rgba(220, 20, 60, 0.3),
    rgba(255, 105, 180, 0.3)
  );
}

.fire-icon {
  background: linear-gradient(
    135deg,
    rgba(0, 255, 0, 0.3),
    rgba(50, 205, 50, 0.3)
  );
}

.humidity-icon {
  background: linear-gradient(
    135deg,
    rgba(255, 165, 0, 0.3),
    rgba(255, 215, 0, 0.3)
  );
}

.gas-icon {
  background: linear-gradient(
    135deg,
    rgba(70, 130, 180, 0.3),
    rgba(135, 206, 250, 0.3)
  );
}

.timer-icon {
  background: linear-gradient(
    135deg,
    rgba(147, 112, 219, 0.3),
    rgba(186, 85, 211, 0.3)
  );
}

.control-label {
  color: #fff;
  font-size: 22rpx;
}

.control-option.active .control-label {
  color: var(--error-color);
  font-weight: bold;
}

/* 状态切换栏 */
.status-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 30rpx;
  gap: 0;
}

.status-item {
  width: 150rpx;
  height: 60rpx;
  line-height: 60rpx;
  text-align: center;
  color: #fff;
  font-size: 26rpx;
  background: #1a1a3a;
  border: 2rpx solid var(--accent-color);
}

.status-item:first-child {
  border-radius: 30rpx 0 0 30rpx;
}

.status-item:last-child {
  border-radius: 0 30rpx 30rpx 0;
}

.status-item.active {
  background: var(--accent-color);
  font-weight: bold;
}

/* 风机信息 */
.fan-info {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  gap: 20rpx;
}

.fan-icon-wrapper {
  width: 72rpx;
  height: 72rpx;
  padding: 12rpx;
  border: 3rpx dashed #6a5acd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(106, 90, 205, 0.1);
}

.fan-name {
  color: #fff;
  font-size: 28rpx;
  font-weight: bold;
}

.fan-name-input {
  color: #fff;
  font-size: 28rpx;
  font-weight: bold;
  background: transparent;
  border: none;
  border-bottom: 2rpx solid rgba(106, 90, 205, 0.5);
  padding: 5rpx 10rpx;
  min-width: 150rpx;
}

.realtime-value {
  color: #fff;
  font-size: 26rpx;
  text-align: right;
  margin-left: auto;
  min-width: 220rpx;
}

/* 控制内容 */
.control-content {
  padding-bottom: 40rpx;
}

/* 表单项 */
.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  gap: 15rpx;
}

.switch-item {
  justify-content: space-between;
}

.form-label {
  color: #fff;
  font-size: 26rpx;
  width: 200rpx;
  text-align: right;
  margin-right: 20rpx;
}

.form-input {
  width: 300rpx;
  height: 60rpx;
  background: #1c1c26;
  border: 2rpx solid rgba(106, 90, 205, 0.3);
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

/* 定时标签页 */
.timer-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 30rpx;
  gap: 0;
}

.timer-tab {
  width: 150rpx;
  height: 60rpx;
  line-height: 60rpx;
  text-align: center;
  color: #fff;
  font-size: 26rpx;
  background: #1a1a3a;
  border: 2rpx solid rgba(106, 90, 205, 0.3);
}

.timer-tab:first-child {
  border-radius: 30rpx 0 0 30rpx;
}

.timer-tab:last-child {
  border-radius: 0 30rpx 30rpx 0;
}

.timer-tab.active {
  background: var(--accent-color);
  border-color: var(--accent-color);
  font-weight: bold;
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
</style>
