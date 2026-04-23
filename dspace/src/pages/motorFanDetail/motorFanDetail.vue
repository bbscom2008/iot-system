<template>
  <view class="page">
    <!-- 控制模式选项 -->
    <view class="control-options">
      <view
        class="control-option"
        :class="{ active: controlMode === 0 }"
        @tap="switchControlMode(0)"
      >
        <view class="control-icon temp-icon">
          <SvgIcon
            name="temperature"
            :color="controlMode === 0 ? 'red' : 'white'"
            :fill="controlMode === 0 ? 'red' : 'white'"
          />
        </view>
        <text class="control-label">温控</text>
      </view>

      <view
        class="control-option"
        :class="{ active: controlMode === 1 }"
        @tap="switchControlMode(1)"
      >
        <view class="control-icon fire-icon">
          <SvgIcon
            name="recycle"
            :color="controlMode === 1 ? 'red' : 'white'"
            :fill="controlMode === 1 ? 'red' : 'white'"
          />
        </view>
        <text class="control-label">循环</text>
      </view>

      <view
        class="control-option"
        :class="{ active: controlMode === 2 }"
        @tap="switchControlMode(2)"
      >
        <view class="control-icon humidity-icon">
          <SvgIcon
            name="humidity"
            :color="controlMode === 2 ? 'red' : 'white'"
            :fill="controlMode === 2 ? 'red' : 'white'"
          />
        </view>
        <text class="control-label">湿控</text>
      </view>

      <view
        class="control-option"
        :class="{ active: controlMode === 3 }"
        @tap="switchControlMode(3)"
      >
        <view class="control-icon gas-icon">
          <SvgIcon
            name="gas"
            :color="controlMode === 3 ? 'red' : 'white'"
            :fill="controlMode === 3 ? 'red' : 'white'"
          />
        </view>
        <text class="control-label">气体</text>
      </view>

      <view
        class="control-option"
        :class="{ active: controlMode === 4 }"
        @tap="switchControlMode(4)"
      >
        <view class="control-icon timer-icon">
          <SvgIcon
            name="timer"
            :color="controlMode === 4 ? 'red' : 'white'"
            :fill="controlMode === 4 ? 'red' : 'white'"
          />
        </view>
        <text class="control-label">定时</text>
      </view>
    </view>

    <!-- 状态切换栏 -->
    <!-- <view class="status-bar">
      <SingleButtonSelect
        v-model="autoMode"
        :options="[
          { label: '自动', value: 1 },
          { label: '开', value: 2 },
          { label: '关', value: 3 }
        ]"
      />
    </view> -->

    <!-- 风机信息 -->
    <view class="fan-info">
      <view class="fan-icon-wrapper">
        <SvgIcon name="fan" color="#6a5acd" :fill="'#6a5acd'" />
      </view>
      <input class="fan-name-input" v-model="fanName" placeholder="请输入风机名称" />
      <!-- <text class="realtime-value">实时温度: {{ realtimeTemp }}°C</text> -->
      <text class="realtime-value"></text>
    </view>

    <!-- 温控模式内容 -->
    <view v-if="controlMode === 0" class="control-content">
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

      <view class="form-item">
        <text class="form-label">温控模式:</text>
        <SingleButtonSelect
          v-model="tctcm"
          :options="[
            { label: '降温', value: 0 },
            { label: '升温', value: 1 }
          ]"
          itemMinWidth="100rpx"
          fontSize="24rpx"
        />
          <!-- variant="chip" -->
      </view>

      <view class="instructions">
        <text class="instruction-item">1、实时温度达到或超过温度上限，设备一直工作。</text>
        <text class="instruction-item">2、实时温度在温度上限与温度下限之间，设备按照运行时间和暂停时间循环工作。</text>
        <text class="instruction-item">3、实时温度低于温度下限，设备停止工作。</text>
      </view>
    </view>

    <!-- 循环模式内容 -->
    <view v-if="controlMode === 1" class="control-content">
      <view class="form-item">
        <text class="form-label">探头选择:</text>
        <picker
          class="picker-wrapper"
          @change="onCycleProbeChange"
          :value="cycleProbeIndex"
          :range="temperatureSensors"
          range-key="sensorName"
        >
          <view class="picker-input">
            <text :class="{ placeholder: cycleProbeIndex < 0 }">
              {{ cycleProbeIndex >= 0 ? temperatureSensors[cycleProbeIndex].sensorName : '请选择探头' }}
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

      <view class="form-item">
        <text class="form-label">循环模式:</text>
        <SingleButtonSelect
          v-model="cccm"
          :options="[
            { label: '降温', value: 0 },
            { label: '升温', value: 1 },
            { label: '时间', value: 2 }
          ]"
          itemMinWidth="120rpx"
          variant="chip"
          fontSize="24rpx"
        />
      </view>

      <view class="instructions">
        <text class="instruction-item">1、实时温度大于等于启动温度值启动设备，低于停止温度值设备停止工作。</text>
        <text class="instruction-item">2、低温循环设置方法（最小通风模式）：设置所需低温运行时间和低温暂停时间即可。</text>
        <text class="instruction-item">3、低温循环关闭方法：设置低温运行为0分0秒，低温暂停为0分1秒。</text>
      </view>
    </view>

    <!-- 湿控模式内容 -->
    <view v-if="controlMode === 2" class="control-content">
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
          v-model="humidityRunMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="humidityRunSeconds"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="form-item">
        <text class="form-label">暂停时间:</text>
        <input
          class="form-input-small"
          v-model="humidityPauseMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="humidityPauseSeconds"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="form-item">
        <text class="form-label">湿控模式:</text>
        <SingleButtonSelect
          v-model="hchcm"
          :options="[
            { label: '除湿', value: 0 },
            { label: '加湿', value: 1 }
          ]"
          itemMinWidth="100rpx"
          fontSize="24rpx"
        />
          <!-- variant="chip" -->
      </view>

      <view class="instructions">
        <text class="instruction-item">1、实时湿度大于等于湿度上限值启动设备，低于湿度下限值设备停止工作。</text>
        <text class="instruction-item">2、循环设置方法：设置所需运行时间和暂停时间即可。</text>
        <text class="instruction-item">3、循环关闭方法：设置运行时间为0分0秒，暂停时间为0分1秒。</text>
      </view>
    </view>

    <!-- 气体模式内容 -->
    <view v-if="controlMode === 3" class="control-content">
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
          v-model="gasRunMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="gasRunSeconds"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">秒</text>
      </view>

      <view class="form-item">
        <text class="form-label">暂停时间:</text>
        <input
          class="form-input-small"
          v-model="gasPauseMinutes"
          type="number"
          placeholder="0"
        />
        <text class="form-unit-small">分</text>
        <input
          class="form-input-small"
          v-model="gasPauseSeconds"
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
    <view v-if="controlMode === 4" class="control-content">
      <!-- 定时组切换 -->
      <view class="timer-tabs">
        <SingleButtonSelect
          v-model="timerGroupModel"
          :options="[
            { label: '定时1', value: 1 },
            { label: '定时2', value: 2 },
            { label: '定时3', value: 3 }
          ]"
        />
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
          @input="onTimerStartHourInput"
          type="number"
          placeholder="5"
        />
        <text class="form-unit-small">时</text>
        <input
          class="form-input-small"
          :value="timerGroups[currentTimerGroup - 1].startMinute"
          @input="onTimerStartMinuteInput"
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
          @input="onTimerEndHourInput"
          type="number"
          placeholder="6"
        />
        <text class="form-unit-small">时</text>
        <input
          class="form-input-small"
          :value="timerGroups[currentTimerGroup - 1].endMinute"
          @input="onTimerEndMinuteInput"
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
          @input="onTimerStartTempInput"
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
          @input="onTimerStopTempInput"
          type="number"
          placeholder="30"
        />
        <text class="form-unit">°C</text>
      </view>

      <view class="form-item">
        <text class="form-label">定时温控:</text>
        <SingleButtonSelect
          v-model="tictitm"
          :options="[
            { label: '降温', value: 0 },
            { label: '升温', value: 1 }
          ]"
          itemMinWidth="100rpx"
          fontSize="24rpx"
        />
          <!-- variant="chip" -->
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
import SingleButtonSelect from "@/components/SingleButtonSelect.vue";
import { request } from "@/utils/request";

export default {
  name: "MotorFanDetail",
  components: {
    SvgIcon,
    MySwitch,
    SingleButtonSelect,
  },
  computed: {
    currFan() {
      return this.$store.state.deviceDetail.currentMotorFan || {};
    },
    temperatureSensors() {
      // 固定探头枚举：0~7
      return [
        { id: 0, sensorName: '探头1' },
        { id: 1, sensorName: '探头2' },
        { id: 2, sensorName: '探头3' },
        { id: 3, sensorName: '探头4' },
        { id: 4, sensorName: '探头12' },
        { id: 5, sensorName: '探头34' },
        { id: 6, sensorName: '探头1234' },
        { id: 7, sensorName: '探头1234' },
      ];
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
        return this.currFan.wm !== undefined && this.currFan.wm !== null ? Number(this.currFan.wm) : 0;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "wm",
          value: Number(value) || 0,
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
        const sensorId = this.currFan.tcps;
        if (sensorId === undefined || sensorId === null || !this.temperatureSensors.length) return -1;
        return this.temperatureSensors.findIndex(s => s.id === Number(sensorId));
      },
      set(value) {
        const sensorId = value >= 0 && this.temperatureSensors[value] ? this.temperatureSensors[value].id : null;
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tcps",
          value: sensorId,
        });
      },
    },
    // 温度上限/启动温度
    tempUpper: {
      get() {
        return this.currFan.tcat !== undefined && this.currFan.tcat !== null ? String(this.currFan.tcat) : '35';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tcat",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 温度下限/停止温度
    tempLower: {
      get() {
        return this.currFan.tcot !== undefined && this.currFan.tcot !== null ? String(this.currFan.tcot) : '10';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tcot",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 运行时间（分钟）
    runMinutes: {
      get() {
        return this.currFan.tcltrm || 0;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tcltrm",
          value: parseInt(value) || 0,
        });
      },
    },
    // 运行时间（秒）
    runSeconds: {
      get() {
        return this.currFan.tcltrs || 5;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tcltrs",
          value: parseInt(value) || 0,
        });
      },
    },
    // 暂停时间（分钟）
    pauseMinutes: {
      get() {
        return this.currFan.tcltpm || 0;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tcltpm",
          value: parseInt(value) || 0,
        });
      },
    },
    // 暂停时间（秒）
    pauseSeconds: {
      get() {
        return this.currFan.tcltps || 5;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tcltps",
          value: parseInt(value) || 0,
        });
      },
    },
    // 温控模式：0降温 1升温
    tctcm: {
      get() {
        return this.currFan.tctcm !== undefined && this.currFan.tctcm !== null ? Number(this.currFan.tctcm) : 0;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tctcm",
          value: Number(value) || 0,
        });
      },
    },
    // 循环模式：0降温 1升温 2时间
    cccm: {
      get() {
        return this.currFan.cccm !== undefined && this.currFan.cccm !== null ? Number(this.currFan.cccm) : 0;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "cccm",
          value: Number(value) || 0,
        });
      },
    },
    // 湿控模式：0除湿 1加湿
    hchcm: {
      get() {
        return this.currFan.hchcm !== undefined && this.currFan.hchcm !== null ? Number(this.currFan.hchcm) : 0;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "hchcm",
          value: Number(value) || 0,
        });
      },
    },
    // 定时温控：0降温 1升温
    tictitm: {
      get() {
        return this.currFan.tictitm !== undefined && this.currFan.tictitm !== null ? Number(this.currFan.tictitm) : 0;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "tictitm",
          value: Number(value) || 0,
        });
      },
    },
    // 湿度上限
    humidityUpper: {
      get() {
        return this.currFan.hchu !== undefined && this.currFan.hchu !== null ? String(this.currFan.hchu) : '90';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "hchu",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 湿度下限
    humidityLower: {
      get() {
        return this.currFan.hchd !== undefined && this.currFan.hchd !== null ? String(this.currFan.hchd) : '30';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "hchd",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 气体上限
    gasUpper: {
      get() {
        return this.currFan.ncnu !== undefined && this.currFan.ncnu !== null ? String(this.currFan.ncnu) : '3000';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "ncnu",
          value: parseInt(value) || 0,
        });
      },
    },
    // 气体下限
    gasLower: {
      get() {
        return this.currFan.ncnd !== undefined && this.currFan.ncnd !== null ? String(this.currFan.ncnd) : '1000';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "ncnd",
          value: parseInt(value) || 0,
        });
      },
    },
    // 循环模式 - 启动温度（使用tempUpper）
    startTemp: {
      get() {
        return this.currFan.cctu !== undefined ? String(this.currFan.cctu) : '35';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "cctu",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 循环模式 - 停止温度（使用tempLower）
    stopTemp: {
      get() {
        return this.currFan.cctd !== undefined ? String(this.currFan.cctd) : '10';
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "cctd",
          value: parseFloat(value) || 0,
        });
      },
    },
    // 循环模式 - 探头索引
    cycleProbeIndex: {
      get() {
        if (this.currFan.ccps === undefined || this.currFan.ccps === null) return -1;
        return Number(this.currFan.ccps);
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "ccps",
          value: Number(value),
        });
      },
    },
    // 循环模式 - 低温运行时间（分钟）
    lowTempRunMinutes: {
      get() {
        return this.currFan.ccrm || 0;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "ccrm",
          value: parseInt(value) || 0,
        });
      },
    },
    // 循环模式 - 低温运行时间（秒）
    lowTempRunSeconds: {
      get() {
        return this.currFan.ccrs || 0;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "ccrs",
          value: parseInt(value) || 0,
        });
      },
    },
    // 循环模式 - 低温暂停时间（分钟）
    lowTempPauseMinutes: {
      get() {
        return this.currFan.ccpm || 0;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "ccpm",
          value: parseInt(value) || 0,
        });
      },
    },
    // 循环模式 - 低温暂停时间（秒）
    lowTempPauseSeconds: {
      get() {
        return this.currFan.ccpss || 5;
      },
      set(value) {
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
          field: "ccpss",
          value: parseInt(value) || 0,
        });
      },
    },
    // 湿控运行暂停时间
    humidityRunMinutes: {
      get() { return this.currFan.hcrm || 0; },
      set(value) { this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", { field: "hcrm", value: parseInt(value) || 0 }); },
    },
    humidityRunSeconds: {
      get() { return this.currFan.hcrs || 0; },
      set(value) { this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", { field: "hcrs", value: parseInt(value) || 0 }); },
    },
    humidityPauseMinutes: {
      get() { return this.currFan.hcpm || 0; },
      set(value) { this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", { field: "hcpm", value: parseInt(value) || 0 }); },
    },
    humidityPauseSeconds: {
      get() { return this.currFan.hcps || 0; },
      set(value) { this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", { field: "hcps", value: parseInt(value) || 0 }); },
    },
    // 氨气运行暂停时间
    gasRunMinutes: {
      get() { return this.currFan.ncrm || 0; },
      set(value) { this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", { field: "ncrm", value: parseInt(value) || 0 }); },
    },
    gasRunSeconds: {
      get() { return this.currFan.ncrs || 0; },
      set(value) { this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", { field: "ncrs", value: parseInt(value) || 0 }); },
    },
    gasPauseMinutes: {
      get() { return this.currFan.ncpm || 0; },
      set(value) { this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", { field: "ncpm", value: parseInt(value) || 0 }); },
    },
    gasPauseSeconds: {
      get() { return this.currFan.ncps || 0; },
      set(value) { this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", { field: "ncps", value: parseInt(value) || 0 }); },
    },
    // 定时组数据
    timerGroups() {
      return [
        {
          enabled: this.currFan.tict1nf === 0,
          startHour: this.currFan.tict1nh ?? '',
          startMinute: this.currFan.tict1nm ?? '',
          endHour: this.currFan.tict1fh ?? '',
          endMinute: this.currFan.tict1fm ?? '',
          probeIndex: this.currFan.ticps ?? 0,
          startTemp: this.currFan.ticat !== undefined ? String(this.currFan.ticat) : '',
          stopTemp: this.currFan.ticot !== undefined ? String(this.currFan.ticot) : '',
        },
        {
          enabled: this.currFan.tict2nf === 0,
          startHour: this.currFan.tict2nh ?? '',
          startMinute: this.currFan.tict2nm ?? '',
          endHour: this.currFan.tict2fh ?? '',
          endMinute: this.currFan.tict2fm ?? '',
          probeIndex: this.currFan.ticps ?? 0,
          startTemp: this.currFan.ticat !== undefined ? String(this.currFan.ticat) : '',
          stopTemp: this.currFan.ticot !== undefined ? String(this.currFan.ticot) : '',
        },
        {
          enabled: this.currFan.tict3nf === 0,
          startHour: this.currFan.tict3nh ?? '',
          startMinute: this.currFan.tict3nm ?? '',
          endHour: this.currFan.tict3fh ?? '',
          endMinute: this.currFan.tict3fm ?? '',
          probeIndex: this.currFan.ticps ?? 0,
          startTemp: this.currFan.ticat !== undefined ? String(this.currFan.ticat) : '',
          stopTemp: this.currFan.ticot !== undefined ? String(this.currFan.ticot) : '',
        },
      ];
    },
    realtimeTemp(){
      if (this.probeIndex >= 0 && this.temperatureSensors[this.probeIndex]) {
        return this.temperatureSensors[this.probeIndex].sensorValue ?? '--';
      }
      return '--';
    },
    timerGroupModel: {
      get() {
        return this.currentTimerGroup;
      },
      set(value) {
        this.currentTimerGroup = Number(value) || 1;
      }
    }
    
  },
  data() {
    return {
      currentTimerGroup: 1,
    };
  },
  onLoad() {
    uni.$on("device/motorFanUpdate", this.fetchMotorFanDetail);
  },
  onUnload() {
    // 离开页面时重置风机详情数据
    this.$store.commit("deviceDetail/SET_CURRENT_MOTOR_FAN", null);
    uni.$off("device/motorFanUpdate", this.fetchMotorFanDetail);
  },  
  methods: {
    async fetchMotorFanDetail({ deviceNum } = {}) {
      try {
        const currDevice = this.$store.state.deviceDetail.currDevice || {};
        const currDeviceNum = currDevice.deviceNum || currDevice.id;

        // 只处理当前设备的更新
        if (deviceNum && currDeviceNum && String(deviceNum) !== String(currDeviceNum)) {
          return;
        }

        const currentFanId = this.currFan && this.currFan.id;
        if (!currentFanId) return;

        const res = await this.$store.dispatch("deviceDetail/fetchDeviceInfo");
        const deviceInfo = (res && res.deviceInfo) || this.$store.state.deviceDetail.deviceInfo || {};
        const motorFans = Array.isArray(deviceInfo.motorFans) ? deviceInfo.motorFans : [];
        const latest = motorFans.find((item) => String(item.id) === String(currentFanId));

        if (latest) {
          this.$store.commit("deviceDetail/SET_CURRENT_MOTOR_FAN", { ...latest });
        }
      } catch (error) {
        console.error("刷新风机详情失败:", error);
      }
    },
    switchControlMode(mode) {
      this.controlMode = mode;
    },
    onProbeChange(e) {
      this.probeIndex = parseInt(e.detail.value);
    },
    onCycleProbeChange(e) {
      this.cycleProbeIndex = parseInt(e.detail.value);
    },
    onTimerSwitchChange(value) {
      const field = `tict${this.currentTimerGroup}nf`;
      this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
        field,
        value: value ? 0 : 1,
      });
    },
    onTimerProbeChange(e) {
      const index = parseInt(e.detail.value);
      const sensorId = index >= 0 && this.temperatureSensors[index] ? this.temperatureSensors[index].id : null;
      this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
        field: "ticps",
        value: sensorId,
      });
    },
    updateTimerField(timerNum, fieldName, value) {
      const mapping = {
        1: { startHour: 'tict1nh', startMinute: 'tict1nm', endHour: 'tict1fh', endMinute: 'tict1fm' },
        2: { startHour: 'tict2nh', startMinute: 'tict2nm', endHour: 'tict2fh', endMinute: 'tict2fm' },
        3: { startHour: 'tict3nh', startMinute: 'tict3nm', endHour: 'tict3fh', endMinute: 'tict3fm' },
      };
      const field = mapping[timerNum]?.[fieldName];
      if (!field) return;
      this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
        field,
        value,
      });
    },
    onTimerStartHourInput(e) {
      this.updateTimerField(this.currentTimerGroup, "startHour", parseInt(e.detail.value) || 0);
    },
    onTimerStartMinuteInput(e) {
      this.updateTimerField(this.currentTimerGroup, "startMinute", parseInt(e.detail.value) || 0);
    },
    onTimerEndHourInput(e) {
      this.updateTimerField(this.currentTimerGroup, "endHour", parseInt(e.detail.value) || 0);
    },
    onTimerEndMinuteInput(e) {
      this.updateTimerField(this.currentTimerGroup, "endMinute", parseInt(e.detail.value) || 0);
    },
    onTimerStartTempInput(e) {
      this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
        field: "ticat",
        value: parseFloat(e.detail.value) || 0,
      });
    },
    onTimerStopTempInput(e) {
      this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_FIELD", {
        field: "ticot",
        value: parseFloat(e.detail.value) || 0,
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
        const timerGroups = this.timerGroups;
        // 准备要提交的数据
        const requestData = {
          id: this.currFan.id,
          fanName: this.currFan.fanName,
          wm: this.currFan.wm,
          autoMode: this.currFan.autoMode,
          tcps: this.currFan.tcps,
          tcat: this.currFan.tcat,
          tcot: this.currFan.tcot,
          tcltrm: this.currFan.tcltrm,
          tcltrs: this.currFan.tcltrs,
          tcltpm: this.currFan.tcltpm,
          tcltps: this.currFan.tcltps,
          tctcm: this.currFan.tctcm,
          ccps: this.currFan.ccps,
          cctu: this.currFan.cctu,
          cctd: this.currFan.cctd,
          ccrm: this.currFan.ccrm,
          ccrs: this.currFan.ccrs,
          ccpm: this.currFan.ccpm,
          ccpss: this.currFan.ccpss,
          cccm: this.currFan.cccm,
          hchu: this.currFan.hchu,
          hchd: this.currFan.hchd,
          hcrm: this.currFan.hcrm,
          hcrs: this.currFan.hcrs,
          hcpm: this.currFan.hcpm,
          hcps: this.currFan.hcps,
          hchcm: this.currFan.hchcm,
          ncnu: this.currFan.ncnu,
          ncnd: this.currFan.ncnd,
          ncrm: this.currFan.ncrm,
          ncrs: this.currFan.ncrs,
          ncpm: this.currFan.ncpm,
          ncps: this.currFan.ncps,
          tictitm: this.currFan.tictitm,
          tict1nf: timerGroups[0].enabled ? 0 : 1,
          tict1nh: Number(timerGroups[0].startHour) || 0,
          tict1nm: Number(timerGroups[0].startMinute) || 0,
          tict1fh: Number(timerGroups[0].endHour) || 0,
          tict1fm: Number(timerGroups[0].endMinute) || 0,
          tict2nf: timerGroups[1].enabled ? 0 : 1,
          tict2nh: Number(timerGroups[1].startHour) || 0,
          tict2nm: Number(timerGroups[1].startMinute) || 0,
          tict2fh: Number(timerGroups[1].endHour) || 0,
          tict2fm: Number(timerGroups[1].endMinute) || 0,
          tict3nf: timerGroups[2].enabled ? 0 : 1,
          tict3nh: Number(timerGroups[2].startHour) || 0,
          tict3nm: Number(timerGroups[2].startMinute) || 0,
          tict3fh: Number(timerGroups[2].endHour) || 0,
          tict3fm: Number(timerGroups[2].endMinute) || 0,
          ticps: (this.currFan.ticps ?? Number(timerGroups[this.currentTimerGroup - 1].probeIndex)) || 0,
          ticat: (this.currFan.ticat ?? Number(timerGroups[this.currentTimerGroup - 1].startTemp)) || 0,
          ticot: (this.currFan.ticot ?? Number(timerGroups[this.currentTimerGroup - 1].stopTemp)) || 0,
        };

        // 调用API保存
        const response = await request({
          url: '/motor-fan/update',
          method: 'PUT',
          data: requestData,
        });

        this.$store.commit("deviceDetail/SET_CURRENT_MOTOR_FAN", {
          ...this.currFan,
          ...requestData,
        });
        this.$store.commit("deviceDetail/UPDATE_MOTOR_FAN_IN_DEVICE_INFO", {
          ...this.currFan,
          ...requestData,
        });

        uni.showToast({
          title: "保存成功",
          icon: "success",
        });

        // uni.navigateBack();

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
