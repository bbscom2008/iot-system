<template>
  <view class="page">
    <!-- Tab 导航 -->
    <view class="tab-navigation">
      <view
        class="tab-item"
        :class="{ active: currentTab === 0 }"
        @tap="switchTab(0)"
        >设备列表</view
      >
      <view
        class="tab-item"
        :class="{ active: currentTab === 1 }"
        @tap="switchTab(1)"
        >续费充值</view
      >
    </view>

    <!-- 设备列表组件 -->
    <view v-if="currentTab === 0" class="tab-content">
      <!-- 设备统计卡片（使用 React 项目导出的图标） -->
      <view class="stats-container">
        <view class="stat-card">
          <view class="stat-number">{{ allDevice || "0" }}</view>
          <view class="stat-bottom">
            <view class="stat-icon">
              <SvgIcon name="devices" :size="18" />
            </view>
            <view class="stat-label">总设备</view>
          </view>
        </view>
        <view class="stat-card">
          <view class="stat-number">{{ lineDevice || "0" }}</view>
          <view class="stat-bottom">
            <view class="stat-icon">
              <SvgIcon name="wifi" :size="18" color="#05df72" />
            </view>
            <view class="stat-label">在线设备</view>
          </view>
        </view>
        <view class="stat-card">
          <view class="stat-number">{{ warningDevice || "0" }}</view>
          <view class="stat-bottom">
            <view class="stat-icon">
              <SvgIcon name="alarm" :size="18" color="#cda109" />
            </view>
            <view class="stat-label">报警设备</view>
          </view>
        </view>
      </view>

      <!-- 搜索栏 -->
      <view class="search-container">
        <view class="search-left">
          <view class="search-icon iconfont icon-sousuo"></view>
          <input
            class="search-input"
            placeholder="搜索设备名称或编号..."
            type="text"
            v-model="searchValue"
            @input="handleInput"
          />
        </view>
        <button
          class="search-btn"
          :class="{ 'add-btn': !searchValue }"
          @tap="handleSearchOrAdd"
        >
          {{ searchValue ? "搜索" : "添加" }}
        </button>
      </view>

      <!-- 设备列表 -->
      <view class="device-list-container">
        <!-- 设备类型标题 -->
        <view class="section-title" v-if="deviceList.length">
          <view class="title-bar"></view>
          <text>设备列表</text>
        </view>

        <!-- 设备卡片 -->
        <view
          class="device-card"
          v-for="(item) in deviceList"
          :key="item.id"
          @tap="toDetail(item)"
        >
          <view class="device-info">
            <!-- 设备编号 -->
            <view class="base-info">
              <view v-if="hasAnyOutOfRange(item)" class="alarm-icon">
                <SvgIcon name="alarm" :size="18" color="#cda109" />
              </view>
              <view class="device-number">{{ item.deviceName }}</view>
              <view class="device-number">编号: {{ item.deviceNum.length > 8 ? item.deviceNum.slice(-8) : item.deviceNum }}</view>
            </view>

            <!-- 状态指示器 -->
            <view class="status-indicators">
              <view class="status-item">
                <view
                  class="status-icon wifi-icon"
                  :class="{ active: item.signal > 1 }"
                >
                  <SvgIcon name="wifi" :size="18" />
                </view>
                <text
                  class="status-text"
                  :class="{ active: item.signal > 1 }"
                  >{{ item.signal > 1 ? "网络正常" : "网络异常" }}</text
                >
              </view>
              <view class="status-item">
                <view
                  class="status-icon battery-icon"
                  :class="{ active: item.electricQuantity > 20 }"
                >
                  <SvgIcon
                    v-if="item.electricQuantity > 20"
                    name="power-full"
                    :size="18"
                  />
                  <SvgIcon v-else name="power-empty" :size="18" />
                </view>
                <text
                  class="status-text"
                  :class="{ active: item.electricQuantity > 20 }"
                  >{{
                    item.electricQuantity > 20 ? "电量充足" : "电量不足"
                  }}</text
                >
              </view>
              <view class="status-item">
                <view
                  class="status-dot"
                  :class="{ active: item.deviceLineState == 1 }"
                ></view>
                <text
                  class="status-text"
                  :class="{ active: item.deviceLineState == 1 }"
                  >{{ item.deviceLineState ? "在线" : "离线" }}</text
                >
              </view>
            </view>
          </view>

          <!-- 传感器数据 -->
          <view class="sensor-data">
            <!-- 第一行：温度传感器（sensor_type_id = 5） -->
            <view
              class="sensor-row"
            >
              <!-- v-if="getTemperatureSensors(item.sensors).length > 0" -->
              <view
                class="sensor-item"
                v-for="sensor in getTemperatureSensors(item.sensors)"
                :key="sensor.id"
              >
                <text class="sensor-label">{{ sensor.sensorName }}</text>
                <text class="sensor-text" :class="{ 'out-of-range': item.deviceLineState == 1 && isTemperatureOutOfRange(sensor, item) }"
                  >{{ item.deviceLineState == 1 ? (sensor.sensorValue || "--") : "--" }}°C</text
                >
              </view>
            </view>
            <!-- 第二行：其他传感器（湿度、气体等） -->
            <view
              class="sensor-row"
              v-if="getOtherSensors(item.sensors).length > 0"
            >
              <view
                class="sensor-item"
                v-for="sensor in getOtherSensors(item.sensors)"
                :key="sensor.id"
              >
                <text class="sensor-label">{{ sensor.sensorName }}</text>
                <text class="sensor-text" 
                  :class="{ 
                    'out-of-range': item.deviceLineState == 1 && (isHumidityOutOfRange(sensor, item) || isGasOutOfRange(sensor, item))
                  }"
                  >{{ item.deviceLineState == 1 ? (sensor.sensorValue || "--") : "--" }}{{ sensor.unit }}</text
                >
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="!deviceList.length">
          <view class="empty-icon">📱</view>
          <view class="empty-text">暂无设备</view>
        </view>
      </view>
    </view>

    <!-- 续费充值组件 -->
    <RechargeView v-if="currentTab === 1" />

    <!-- 未登录提示 -->
    <view class="login_btn" v-if="!isLogin" @tap="loginClick">立即登录</view>
  </view>
</template>

<script>
import request from "@/utils/request.js";
import SvgIcon from "@/components/SvgIcon.vue";
import RechargeView from "./recharge.vue";
import { mapState, mapActions } from 'vuex';

export default {
  name: "HomeView",
  components: { SvgIcon, RechargeView },
  data() {
    return {
      searchValue: "",
      isLogin: true,
      currentTab: 0,
      QueryParams: {
        pageNum: 1,
        pageSize: 10,
        type: 1,
      },
    };
  },
  computed: {
    ...mapState('device', {
      deviceList: state => state.deviceList,
      allDevice: state => state.deviceStats.allDevice,
      lineDevice: state => state.deviceStats.lineDevice,
      warningDevice: state => state.deviceStats.warningDevice,
    })
  },
  
  mounted() {
    this.getSwiperList();
    this.equipmentState();
  },
  methods: {
    ...mapActions('device', [
      'setDeviceList',
      'setDeviceStats',
      'fetchDeviceList',
      'fetchDeviceStats'
    ]),
    // 切换 Tab
    switchTab(index) {
      this.currentTab = index;
      if (index === 0) {
        this.getSwiperList();
      } else {
        this.getRechargeList();
      }
    },
    // 获取充值列表
    async getRechargeList() {
      console.log("获取充值列表");
    },
    // 获取设备列表（使用 Vuex action）
    async getSwiperList() {
      try {
        await this.fetchDeviceList();
      } catch (err) {
        console.log('获取设备列表失败', err)
      }
    },
    // 获取设备统计（使用 Vuex action）
    async equipmentState() {
      try {
        await this.fetchDeviceStats();
      } catch (err) {
        console.log('获取设备统计失败', err)
      }
    },
    // 搜索输入处理（预留方法）
    handleInput(e) {
      // 输入框变化时的处理逻辑（当前不需要）
    },
    // 处理搜索或添加按钮点击
    async handleSearchOrAdd() {
      if (this.searchValue) {
        // 有内容，执行搜索
        await this.qsearch(this.searchValue);
      } else {
        // 没有内容，执行添加
        this.handleAdd();
      }
    },
    async bindDevice(deviceNum){
      try {
        await request.post("/device/bind", {
          deviceNum: deviceNum,
        });
        uni.showToast({
          title: "绑定成功",
          icon: "success",
        });
        // 刷新设备列表
        this.getSwiperList();
        this.equipmentState();
      } catch (err) {
        uni.showToast({
          title: err.msg || "绑定失败",
          icon: "none",
        });
      }
    },
    // 处理添加设备
    handleAdd() {
      // #ifdef MP-WEIXIN
      uni.scanCode({
        onlyFromCamera: false,
        success: async (res) => {
          console.log("扫码结果:", res.result);
          // 扫码成功后，绑定设备
          this.bindDevice(res.result)
        },
        fail: (err) => {
          console.log("扫码失败:", err);
          uni.showToast({
            title: "扫码失败",
            icon: "none",
          });
        },
      });
      // #endif
      // #ifdef H5
      uni.showModal({
        title: "添加设备", // 弹窗标题
        content: "", // 可留空，focus 为 true 时输入框自动聚焦
        editable: true, // 关键：显示输入框
        placeholderText: "请输入设备编号", // 输入框占位符
        success: (res) => {
          if (res.confirm) {
            // 点击「确定」，res.content 为输入的内容
            console.log("用户输入：", res.content);
            // 业务逻辑：如提交、校验等
            if (res.content.trim() === "") {
              uni.showToast({ title: "内容不能为空", icon: "none" });
              return;
            }
            this.bindDevice(res.content)
          } else if (res.cancel) {
            // 点击「取消」
            console.log("用户取消输入");
          }
        },
        fail: (err) => {
          console.error("当前端不支持输入框", err);
          uni.showToast({
              title: `当前端不支持输入框:${err.errMsg}`,
              icon: "error",
            });
        },
      });
      // #endif
    },
    async qsearch(keyword) {
      try {
        const res = await request.get("/device/list", {
          search: keyword,
        });
        // 保存搜索结果到 Vuex 仓库
        this.setDeviceList(res.list || []);
        if (res.list && res.list.length === 0) {
          uni.showToast({
            title: "未找到相关设备",
            icon: "none",
          });
        }
      } catch (err) {
        console.log("搜索失败", err);
      }
    },
    // 跳转详情
    toDetail(item) {

      this.$store.commit('deviceDetail/SET_CURR_DEVICE', item);

      if (item.deviceType == 0) {
        uni.navigateTo({
          url: `/pages/particulars/particulars?deviceId=${item.id}`,
        });
      } else if (item.deviceType == 1) {
        // 进入设备详情
        // 通知更新设备数据

        request.post("/device/notifyUpdateStatus", {
          deviceNum: item.deviceNum,
        });


        uni.navigateTo({
          url: `/pages/equipmentDetails/equipmentDetails`,
        });
      }
    },
    // 跳转登录
    loginClick() {
      uni.navigateTo({
        url: "/pages/login/login",
      });
    },
    // 获取温度传感器（sensor_type_id = 5）
    getTemperatureSensors(sensors) {
      if (!sensors || !Array.isArray(sensors)) return [];
      // 根据 ts1 - 4 排序
      let sortSensors =  sensors.filter((s) => s.sensorTypeId === 5).sort((a, b) => {
        const aNum = parseInt(a.sensorCode.replace("ts", ""));
        const bNum = parseInt(b.sensorCode.replace("ts", ""));
        return aNum - bNum;
      });
      return sortSensors;
    },
    // 获取其他传感器（sensor_type_id != 5）
    getOtherSensors(sensors) {
      if (!sensors || !Array.isArray(sensors)) return [];
      return sensors.filter((s) => s.sensorTypeId !== 5);
    },
    // 判断温度是否超过上下限
    isTemperatureOutOfRange(sensor, device) {
      if (!sensor || sensor.sensorTypeId !== 5) return false;
      if (!device) return false;
      
      const sensorValue = parseFloat(sensor.sensorValue);
      if (isNaN(sensorValue)) return false;
      
      const upperLimit = parseFloat(device.tempUpperLimit);
      const lowerLimit = parseFloat(device.tempLowerLimit);
      
      // 如果上下限都设置为0，表示未设置限制
      if (upperLimit === 0 && lowerLimit === 0) return false;
      
      // 超过上限或低于下限
      if ((upperLimit > 0 && sensorValue > upperLimit) || (lowerLimit > 0 && sensorValue < lowerLimit)) {
        return true;
      }
      return false;
    },
    // 判断湿度是否超过上下限
    isHumidityOutOfRange(sensor, device) {
      if (!sensor || sensor.sensorTypeId !== 6) return false;
      if (!device) return false;
      
      const sensorValue = parseFloat(sensor.sensorValue);
      if (isNaN(sensorValue)) return false;
      
      const upperLimit = parseFloat(device.humidityUpperLimit);
      const lowerLimit = parseFloat(device.humidityLowerLimit);
      
      // 如果上下限都设置为0，表示未设置限制
      if (upperLimit === 0 && lowerLimit === 0) return false;
      
      // 超过上限或低于下限
      if ((upperLimit > 0 && sensorValue > upperLimit) || (lowerLimit > 0 && sensorValue < lowerLimit)) {
        return true;
      }
      return false;
    },
    // 判断气体是否超过上下限
    isGasOutOfRange(sensor, device) {
      if (!sensor || sensor.sensorTypeId !== 7) return false;
      if (!device) return false;
      
      const sensorValue = parseFloat(sensor.sensorValue);
      if (isNaN(sensorValue)) return false;
      
      const upperLimit = parseInt(device.gasUpperLimit);
      const lowerLimit = parseInt(device.gasLowerLimit);
      
      // 如果上下限都设置为0，表示未设置限制
      if (upperLimit === 0 && lowerLimit === 0) return false;
      
      // 超过上限或低于下限
      if ((upperLimit > 0 && sensorValue > upperLimit) || (lowerLimit > 0 && sensorValue < lowerLimit)) {
        return true;
      }
      return false;
    },
    // 判断设备是否有任何传感器超出限制
    hasAnyOutOfRange(device) {
      if (!device || device.deviceLineState !== 1) return false;
      if (!device.sensors || !Array.isArray(device.sensors)) return false;
      
      // 检查是否有任何传感器超出限制
      for (let sensor of device.sensors) {
        if (this.isTemperatureOutOfRange(sensor, device) ||
            this.isHumidityOutOfRange(sensor, device) ||
            this.isGasOutOfRange(sensor, device)) {
          return true;
        }
      }
      return false;
    },
  },
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: var(--primary-bg);
  padding: 0 30rpx;
}

/* Tab 导航 */
.tab-navigation {
  display: flex;
  margin-bottom: 30rpx;
  padding-top: 20rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  color: var(--text-secondary);
  font-size: 28rpx;
  padding: 20rpx 0;
  position: relative;
}

.tab-item.active {
  color: var(--text-primary);
}

.tab-item.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background-color: var(--accent-color);
  border-radius: 2rpx;
}

/* 搜索栏 */
.search-container {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 30rpx;
}

.search-left {
  position: relative;
  flex: 1;
}

.search-icon {
  position: absolute;
  left: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  font-size: 30rpx;
  z-index: 2;
}

.search-input {
  width: 100%;
  height: 70rpx;
  box-sizing: border-box;
  background-color: var(--card-bg);
  border-radius: 40rpx;
  padding-left: 70rpx;
  color: var(--text-primary);
  font-size: 24rpx;
  border: none;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-btn {
  height: 60rpx;
  line-height: 56rpx;
  padding: 0 30rpx;
  background-color: var(--accent-color);
  color: var(--text-primary);
  border: none;
  border-radius: 40rpx;
  font-size: 26rpx;
  white-space: nowrap;
}

.search-btn.add-btn {
  background-color: var(--green-color);
}

/* 设备统计卡片 */
.stats-container {
  display: flex;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.stat-card {
  flex: 1;
  background-color: var(--card-bg);
  border-radius: 16rpx;
  padding: 10rpx 10rpx;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
}

.stat-number {
  font-size: 36rpx;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 10rpx;
}

.stat-bottom {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}

.stat-icon {
  font-size: 36rpx;
}

.stat-label {
  font-size: 24rpx;
  color: var(--text-secondary);
}

/* 设备列表容器 */
.device-list-container {
  margin-bottom: 40rpx;
}

/* 章节标题 */
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
  font-size: 38rpx;
  font-weight: bold;
}
.device-info {
  padding-left: 40rpx;

  
}
.device-info .base-info {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
}

.alarm-icon {
  margin-right: 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
/* 设备卡片 */
.device-card {
  background-color: var(--card-bg);
  border-radius: 16rpx;
  padding: 20rpx 0;
  margin-bottom: 30rpx;
  border: 2rpx solid var(--accent-color);
}

.device-number {
  flex: 1;
  color: var(--text-primary);
  font-size: 30rpx;
  font-weight: 500;
}

/* 状态指示器 */
.status-indicators {
  display: flex;
  gap: 40rpx;
  margin-bottom: 20rpx;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.status-icon {
  font-size: 16rpx;
  opacity: 0.5;
  transition: opacity 0.3s;
}

.status-icon.active {
  opacity: 1;
}

.wifi-icon.active {
  color: #00ff00;
}

.battery-icon.active {
  color: #00bfff;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: #666;
  transition: background-color 0.3s;
}

.status-dot.active {
  background-color: #00ff00;
}

.status-text {
  font-size: 22rpx;
  color: var(--text-muted);
  transition: color 0.3s;
}

.status-text.active {
  color: var(--text-primary);
  font-weight: 500;
}

/* 传感器数据 */
.sensor-data {
  background-color: #08082a;
  border-radius: 12rpx;
  padding: 20rpx;
}

.sensor-row {
  display: flex;
  /* justify-content: space-between; */
  /* gap: 20rpx; */
  margin-bottom: 15rpx;
}

.sensor-row:last-child {
  margin-bottom: 0;
}

.sensor-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex: 1;
}

.sensor-text {
  font-size: 26rpx;
  font-weight: bold;
  color: var(--success-color);
}

.sensor-text.out-of-range {
  color: #ff3333;
}

.sensor-label {
  font-size: 24rpx;
  color: var(--text-secondary);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 30rpx;
  opacity: 0.3;
}

.empty-text {
  color: var(--text-muted);
  font-size: 26rpx;
}

.login_btn {
  background-color: #0ff;
  border-radius: 200rpx;
  bottom: 100rpx;
  color: #000;
  left: 50%;
  line-height: 70rpx;
  position: fixed;
  text-align: center;
  transform: translateX(-50%);
  width: 400rpx;
}
</style>
