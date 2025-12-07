<template>
	<view class="page">
		<!-- 设置名称 -->
		<view class="section">
			<view class="section-title">
				<view class="title-bar"></view>
				<text class="title-text">设置名称</text>
			</view>
			
			<view class="setting-card">
				<view class="setting-item">
					<view class="setting-label">设备名称:</view>
          <input
            class="setting-input"
            v-model="deviceName"
            placeholder="867920077581750"
          />
				</view>
				<view class="setting-item">
					<view class="setting-label">设备编号:</view>
          <input
            class="setting-input"
            v-model="deviceCode"
            placeholder="867920077581750"
          />
				</view>
				<view class="setting-item">
					<view class="setting-label">阶梯时间:</view>
          <input
            class="setting-input"
            v-model="levelTime"
            placeholder="1"
          />
					<view class="unit">秒</view>
				</view>
			</view>
		</view>

		<!-- 极值设置 -->
		<view class="section">
			<view class="section-title">
				<view class="title-bar"></view>
				<text class="title-text">极值设置</text>
			</view>
			
			<view class="setting-card">
				<view class="setting-item">
					<view class="setting-label">气体上限:</view>
          <input
            class="setting-input"
            v-model="gasUpperLimit"
            placeholder="35"
          />
					<view class="unit">ppm</view>
				</view>
				<view class="setting-item">
					<view class="setting-label">气体下限:</view>
          <input
            class="setting-input"
            v-model="gasLowerLimit"
            placeholder="0"
          />
					<view class="unit">ppm</view>
				</view>
				<view class="setting-item">
					<view class="setting-label">温度上限:</view>
          <input
            class="setting-input"
            v-model="tempUpperLimit"
            placeholder="39"
          />
					<view class="unit">°C</view>
				</view>
				<view class="setting-item">
					<view class="setting-label">温度下限:</view>
          <input
            class="setting-input"
            v-model="tempLowerLimit"
            placeholder="1"
          />
					<view class="unit">°C</view>
				</view>
				<view class="setting-item">
					<view class="setting-label">湿度上限:</view>
          <input
            class="setting-input"
            v-model="humidityUpperLimit"
            placeholder="90"
          />
					<view class="unit">%</view>
				</view>
				<view class="setting-item">
					<view class="setting-label">湿度下限:</view>
          <input
            class="setting-input"
            v-model="humidityLowerLimit"
            placeholder="30"
          />
					<view class="unit">%</view>
				</view>
				<!-- <button class="save-btn">保存</button> -->
			</view>
		</view>

		<!-- 开关设置 -->
		<view class="section">
			<view class="section-title">
				<view class="title-bar"></view>
				<text class="title-text">开关设置</text>
				<view class="title-switch">
					<text class="title-switch-text">总开关</text>
          <MySwitch v-model="masterAlarm" />
				</view>
			</view>
			
			<view class="switch-grid">
				<view class="switch-item">
					<view class="switch-label">断电开关</view>
          <MySwitch v-model="powerOff" />
				</view>
				<view class="switch-item">
					<view class="switch-label">温度开关</view>
          <MySwitch v-model="tempSwitch" />
				</view>
				<view class="switch-item">
					<view class="switch-label">湿度开关</view>
          <MySwitch v-model="humiditySwitch" />
				</view>
				<view class="switch-item">
					<view class="switch-label">气体开关</view>
          <MySwitch v-model="gasSwitch" />
				</view>
			</view>
		</view>

		<!-- 报警方式 -->
		<view class="section">
			<view class="section-title">
				<view class="title-bar"></view>
				<text class="title-text">报警设置</text>
			</view>
			
			<view class="alarm-method-card">
				<view class="method-options">
					<view class="method-label">拨打方式:</view>
          <RadioGroup v-model="dialingMethod" :options="dialingMethodOptions" />
				</view>

				<view class="method-options">
					<view class="method-label">报警方式:</view>
          <RadioGroup v-model="alarmMethod" :options="alarmMethodOptions" />
				</view>
				
				<view class="phone-list">
          <view
            class="phone-input-row"
            v-for="(phone, index) in phoneNumbers"
            :key="index"
          >
            <input
              class="phone-input"
              :value="phone"
              @input="updatePhoneNumber(index, $event)"
              :placeholder="'电话号码' + (index + 1)"
            />
            <button class="add-btn" @tap="addPhoneNumber" v-if="index == 0">
              +
            </button>
            <button
              class="remove-btn"
              @tap="removePhoneNumber(index)"
              v-if="phoneNumbers.length > 1 && index != 0"
            >
              -
            </button>
					</view>
				</view>
			</view>
      <button class="save-btn" @tap="saveSettings">保存</button>
		</view>

		<!-- 恢复出厂设置 -->
		<view class="section">
			<view class="section-title">
				<view class="title-bar"></view>
				<text class="title-text">恢复出厂设置</text>
			</view>
			
			<view class="reset-card">
        <button class="reset-btn" @tap="resetToDefault">恢复出厂设置</button>
			</view>
		</view>
	</view>
</template>

<script>
import RadioGroup from "@/components/RadioGroup.vue";
import request from "@/utils/request.js";

export default {
  components: {
    RadioGroup,
  },
	data() {
		return {
      defaultValue: {
		// 设备基本信息的默认值
        levelTime: 10,
        //极值 的默认值
        gasUpperLimit: 3000,
        gasLowerLimit: 1000,
        tempUpperLimit: 38.0,
        tempLowerLimit: 30.0,
        humidityUpperLimit: 75.0,
        humidityLowerLimit: 45.0,
        //开关 的默认值
        masterAlarm: 1,
        powerOff: 1,
        tempSwitch: 1,
        humiditySwitch: 1,
        gasSwitch: 1,
        //拨打方式 的默认值
        dialingMethod: 1,
        //报警方式 的默认值
        alarmMethod: 3,
      },
    };
  },
  computed: {
    deviceInfo() {
      return this.$store.state.app.deviceInfo || {};
    },
    // 设备基本信息
    deviceName: {
      get() {
        return this.deviceInfo.deviceName || "";
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "deviceName",
          value,
        });
      },
    },
    deviceCode: {
      get() {
        return this.deviceInfo.deviceNum || "";
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "deviceNum",
          value,
        });
      },
    },
    levelTime: {
      get() {
        return String(this.deviceInfo.levelTime );
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "levelTime",
          value: parseInt(value) || 0,
        });
      },
    },
    // 极值设置
    gasUpperLimit: {
      get() {
        return this.deviceInfo.gasUpperLimit
          ? String(this.deviceInfo.gasUpperLimit)
          : '0';
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "gasUpperLimit",
          value: parseInt(value) || null,
        });
      },
    },
    gasLowerLimit: {
      get() {
        return this.deviceInfo.gasLowerLimit
          ? String(this.deviceInfo.gasLowerLimit)
          : '0';
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "gasLowerLimit",
          value: parseInt(value) || null,
        });
      },
    },
    tempUpperLimit: {
      get() {
        return this.deviceInfo.tempUpperLimit
          ? String(this.deviceInfo.tempUpperLimit)
          : '0';
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "tempUpperLimit",
          value: parseFloat(value) || null,
        });
      },
    },
    tempLowerLimit: {
      get() {
        return this.deviceInfo.tempLowerLimit
          ? String(this.deviceInfo.tempLowerLimit)
          : '0';
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "tempLowerLimit",
          value: parseFloat(value) || null,
        });
      },
    },
    humidityUpperLimit: {
      get() {
        return this.deviceInfo.humidityUpperLimit
          ? String(this.deviceInfo.humidityUpperLimit)
          : '0';
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "humidityUpperLimit",
          value: parseFloat(value) || null,
        });
      },
    },
    humidityLowerLimit: {
      get() {
        return this.deviceInfo.humidityLowerLimit
          ? String(this.deviceInfo.humidityLowerLimit)
        : '0';
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "humidityLowerLimit",
          value: parseFloat(value) || null,
        });
      },
    },

    masterAlarm: {
      get() {
        return this.deviceInfo.masterSwitch === 1;
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "masterSwitch",
          value: value ? 1 : 0,
        });
        // 总开关关闭时，关闭所有子开关
        if (!value) {
          this.$store.commit("app/UPDATE_DEVICE_FIELD", {
            field: "powerOffSwitch",
            value: 0,
          });
          this.$store.commit("app/UPDATE_DEVICE_FIELD", {
            field: "tempSwitch",
            value: 0,
          });
          this.$store.commit("app/UPDATE_DEVICE_FIELD", {
            field: "humiditySwitch",
            value: 0,
          });
          this.$store.commit("app/UPDATE_DEVICE_FIELD", {
            field: "gasSwitch",
            value: 0,
          });
        }
      },
    },
    powerOff: {
      get() {
        return this.deviceInfo.powerOffSwitch === 1;
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "powerOffSwitch",
          value: value ? 1 : 0,
        });
        // 子开关打开时，打开总开关
        if (value) {
          this.$store.commit("app/UPDATE_DEVICE_FIELD", {
            field: "masterSwitch",
            value: 1,
          });
        }
      },
    },
    tempSwitch: {
      get() {
        return this.deviceInfo.tempSwitch === 1;
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "tempSwitch",
          value: value ? 1 : 0,
        });
        // 子开关打开时，打开总开关
        if (value) {
          this.$store.commit("app/UPDATE_DEVICE_FIELD", {
            field: "masterSwitch",
            value: 1,
          });
        }
      },
    },
    humiditySwitch: {
      get() {
        return this.deviceInfo.humiditySwitch === 1;
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "humiditySwitch",
          value: value ? 1 : 0,
        });
        // 子开关打开时，打开总开关
        if (value) {
          this.$store.commit("app/UPDATE_DEVICE_FIELD", {
            field: "masterSwitch",
            value: 1,
          });
        }
      },
    },
    gasSwitch: {
      get() {
        return this.deviceInfo.gasSwitch === 1;
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "gasSwitch",
          value: value ? 1 : 0,
        });
        // 子开关打开时，打开总开关
        if (value) {
          this.$store.commit("app/UPDATE_DEVICE_FIELD", {
            field: "masterSwitch",
            value: 1,
          });
        }
      },
    },
    // 拨打方式
    dialingMethod: {
      get() {
        return this.deviceInfo.dialingMethod || 1;
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "dialingMethod",
          value,
        });
      },
    },
    // 报警方式
    alarmMethod: {
      get() {
        return this.deviceInfo.alarmMethod || 3;
      },
      set(value) {
        this.$store.commit("app/UPDATE_DEVICE_FIELD", {
          field: "alarmMethod",
          value,
        });
      },
    },
    // 报警电话
    phoneNumbers: {
      get() {
        if (this.deviceInfo.alarmPhones) {
          return Array.isArray(this.deviceInfo.alarmPhones) ? this.deviceInfo.alarmPhones : [""];
        }
        return [""];
      },
      set(value) {
        if (Array.isArray(value)) {
          this.$store.commit("app/UPDATE_DEVICE_FIELD", {
            field: "alarmPhones",
            value: value,
          });
        }
      },
    },
    // 拨打方式选项
    dialingMethodOptions() {
      return [
        { value: 1, label: "同时拨打" },
        { value: 2, label: "依次拨打" },
      ];
    },
    // 报警方式选项
    alarmMethodOptions() {
      return [
        { value: 1, label: "打电话" },
        { value: 2, label: "发短信" },
        { value: 3, label: "打电话和发短信" },
      ];
    },
  },
  methods: {
    // 更新单个电话号码
    updatePhoneNumber(index, event) {
      const value = event.detail.value;
      const phones = [...this.phoneNumbers];
      phones[index] = value;
      this.phoneNumbers = phones;
		},
		// 添加电话号码输入框
		addPhoneNumber() {
      const phones = [...this.phoneNumbers, ""];
      this.phoneNumbers = phones;
		},
		// 删除电话号码输入框
		removePhoneNumber(index) {
			if (this.phoneNumbers.length > 1) {
        const phones = [...this.phoneNumbers];
        phones.splice(index, 1);
        this.phoneNumbers = phones;
      }
    },
    // 恢复出厂设置
    resetToDefault() {
      uni.showModal({
        title: '确认恢复',
        content: '确定要恢复出厂设置吗？所有设置将被重置为默认值',
        confirmText: '确定',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            // 将 defaultValue 的值赋给仓库
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'levelTime', value: this.defaultValue.levelTime });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'gasUpperLimit', value: this.defaultValue.gasUpperLimit });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'gasLowerLimit', value: this.defaultValue.gasLowerLimit });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'tempUpperLimit', value: this.defaultValue.tempUpperLimit });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'tempLowerLimit', value: this.defaultValue.tempLowerLimit });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'humidityUpperLimit', value: this.defaultValue.humidityUpperLimit });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'humidityLowerLimit', value: this.defaultValue.humidityLowerLimit });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'masterSwitch', value: this.defaultValue.masterAlarm });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'powerOffSwitch', value: this.defaultValue.powerOff });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'tempSwitch', value: this.defaultValue.tempSwitch });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'humiditySwitch', value: this.defaultValue.humiditySwitch });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'gasSwitch', value: this.defaultValue.gasSwitch });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'dialingMethod', value: this.defaultValue.dialingMethod });
            this.$store.commit('app/UPDATE_DEVICE_FIELD', { field: 'alarmMethod', value: this.defaultValue.alarmMethod });
            
            uni.showToast({
              title: '恢复成功',
              icon: 'success'
            });
          }
        }
      });
    },
    // 保存设置
    async saveSettings() {
      if (!this.deviceInfo.id) {
        uni.showToast({
          title: '设备信息不存在',
          icon: 'none'
        });
        return;
      }

      // 校验设备名称不能为空
      if (!this.deviceInfo.deviceName || this.deviceInfo.deviceName.trim() === '') {
        uni.showToast({
          title: '设备名称不能为空',
          icon: 'none'
        });
        return;
      }

      // 校验电话号码格式
      if (this.deviceInfo.alarmPhones && Array.isArray(this.deviceInfo.alarmPhones) && this.deviceInfo.alarmPhones.length > 0) {
        const phoneReg = /^1[3-9]\d{9}$/;
        const invalidPhones = this.deviceInfo.alarmPhones.filter(phone => phone && phone.trim() && !phoneReg.test(phone));
        if (invalidPhones.length > 0) {
          uni.showModal({
            title: '电话号码格式错误',
            content: `请输入正确的手机号码格式`,
            showCancel: false,
            confirmText: '确定'
          });
          return;
        }
      }

      // 校验极值范围（如果同时设置了上限和下限，上限必须大于下限）
      if (this.deviceInfo.gasUpperLimit !== undefined && this.deviceInfo.gasUpperLimit !== null &&
          this.deviceInfo.gasLowerLimit !== undefined && this.deviceInfo.gasLowerLimit !== null) {
        if (this.deviceInfo.gasUpperLimit <= this.deviceInfo.gasLowerLimit) {
          uni.showModal({
            title: '数值范围错误',
            content: '气体上限必须大于气体下限',
            showCancel: false,
            confirmText: '确定'
          });
          return;
        }
      }

      if (this.deviceInfo.tempUpperLimit !== undefined && this.deviceInfo.tempUpperLimit !== null &&
          this.deviceInfo.tempLowerLimit !== undefined && this.deviceInfo.tempLowerLimit !== null) {
        if (this.deviceInfo.tempUpperLimit <= this.deviceInfo.tempLowerLimit) {
          uni.showModal({
            title: '数值范围错误',
            content: '温度上限必须大于温度下限',
            showCancel: false,
            confirmText: '确定'
          });
          return;
        }
      }

      if (this.deviceInfo.humidityUpperLimit !== undefined && this.deviceInfo.humidityUpperLimit !== null &&
          this.deviceInfo.humidityLowerLimit !== undefined && this.deviceInfo.humidityLowerLimit !== null) {
        if (this.deviceInfo.humidityUpperLimit <= this.deviceInfo.humidityLowerLimit) {
          uni.showModal({
            title: '数值范围错误',
            content: '湿度上限必须大于湿度下限',
            showCancel: false,
            confirmText: '确定'
          });
          return;
        }
      }

      // 校验阶梯时间必须大于等于0
      if (this.deviceInfo.levelTime !== undefined && this.deviceInfo.levelTime !== null && this.deviceInfo.levelTime < 0) {
        uni.showModal({
          title: '数值范围错误',
          content: '阶梯时间必须大于等于0',
          showCancel: false,
          confirmText: '确定'
        });
        return;
      }

      try {
        // 只提交有值的字段，不提交默认值
        const requestData = {
          deviceName: this.deviceInfo.deviceName,
          deviceNum: this.deviceInfo.deviceNum
        };

        // 只添加有值的字段
        if (this.deviceInfo.levelTime !== undefined && this.deviceInfo.levelTime !== null) {
          requestData.levelTime = this.deviceInfo.levelTime;
        }
        if (this.deviceInfo.gasUpperLimit !== undefined && this.deviceInfo.gasUpperLimit !== null) {
          requestData.gasUpperLimit = this.deviceInfo.gasUpperLimit;
        }
        if (this.deviceInfo.gasLowerLimit !== undefined && this.deviceInfo.gasLowerLimit !== null) {
          requestData.gasLowerLimit = this.deviceInfo.gasLowerLimit;
        }
        if (this.deviceInfo.tempUpperLimit !== undefined && this.deviceInfo.tempUpperLimit !== null) {
          requestData.tempUpperLimit = this.deviceInfo.tempUpperLimit;
        }
        if (this.deviceInfo.tempLowerLimit !== undefined && this.deviceInfo.tempLowerLimit !== null) {
          requestData.tempLowerLimit = this.deviceInfo.tempLowerLimit;
        }
        if (this.deviceInfo.humidityUpperLimit !== undefined && this.deviceInfo.humidityUpperLimit !== null) {
          requestData.humidityUpperLimit = this.deviceInfo.humidityUpperLimit;
        }
        if (this.deviceInfo.humidityLowerLimit !== undefined && this.deviceInfo.humidityLowerLimit !== null) {
          requestData.humidityLowerLimit = this.deviceInfo.humidityLowerLimit;
        }
        if (this.deviceInfo.masterSwitch !== undefined && this.deviceInfo.masterSwitch !== null) {
          requestData.masterSwitch = this.deviceInfo.masterSwitch;
        }
        if (this.deviceInfo.powerOffSwitch !== undefined && this.deviceInfo.powerOffSwitch !== null) {
          requestData.powerOffSwitch = this.deviceInfo.powerOffSwitch;
        }
        if (this.deviceInfo.tempSwitch !== undefined && this.deviceInfo.tempSwitch !== null) {
          requestData.tempSwitch = this.deviceInfo.tempSwitch;
        }
        if (this.deviceInfo.humiditySwitch !== undefined && this.deviceInfo.humiditySwitch !== null) {
          requestData.humiditySwitch = this.deviceInfo.humiditySwitch;
        }
        if (this.deviceInfo.gasSwitch !== undefined && this.deviceInfo.gasSwitch !== null) {
          requestData.gasSwitch = this.deviceInfo.gasSwitch;
        }
        if (this.deviceInfo.dialingMethod !== undefined && this.deviceInfo.dialingMethod !== null) {
          requestData.dialingMethod = this.deviceInfo.dialingMethod;
        }
        if (this.deviceInfo.alarmMethod !== undefined && this.deviceInfo.alarmMethod !== null) {
          requestData.alarmMethod = this.deviceInfo.alarmMethod;
        }
        if (this.deviceInfo.alarmPhones !== undefined && this.deviceInfo.alarmPhones !== null && Array.isArray(this.deviceInfo.alarmPhones) && this.deviceInfo.alarmPhones.length > 0) {
          requestData.alarmPhones = this.deviceInfo.alarmPhones;
        }

        await request.put(`/device/settings/${this.deviceInfo.id}`, requestData);
        
        uni.showToast({
          title: '保存成功',
          icon: 'success'
        });
      } catch (error) {
        console.error('保存失败:', error);
        uni.showToast({
          title: error.msg || '保存失败',
          icon: 'none'
        });
      }
    },
  },
};
</script>

<style scoped>
page {
	background-color: var(--primary-bg);
}

.page {
	min-height: 100vh;
	background-color: var(--primary-bg);
	padding: 0 30rpx 100rpx;
}

/* 章节 */
.section {
	margin-bottom: 40rpx;
}

.section-title {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
	padding-top: 20rpx;
}

.title-bar {
	width: 6rpx;
	height: 40rpx;
	background-color: var(--accent-color);
	border-radius: 3rpx;
	margin-right: 20rpx;
}

.title-text {
	color: var(--text-primary);
	font-size: 36rpx;
	font-weight: bold;
	flex: 1;
}

.title-switch {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.title-switch-text {
	color: var(--text-primary);
	font-size: 28rpx;
	font-weight: bold;
}

/* 设置卡片 */
.setting-card {
	background-color: var(--card-bg);
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.setting-item {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.setting-label {
	color: var(--text-primary);
	font-size: 28rpx;
	margin-right: 20rpx;
	min-width: 140rpx;
}

.setting-input {
	flex: 1;
	height: 60rpx;
	background-color: var(--secondary-bg);
	border: 1rpx solid var(--border-color);
	border-radius: 30rpx;
	padding: 0 20rpx;
	color: var(--text-primary);
	font-size: 28rpx;
}

.unit {
	color: var(--text-primary);
	font-size: 28rpx;
	margin-left: 10rpx;
}

.save-btn {
  width: 90%;
	background-color: var(--accent-color);
	color: var(--text-primary);
	border: none;
	border-radius: 30rpx;
  padding: 20rpx 0;
	font-size: 28rpx;
  margin-top: 40rpx;
}

/* 探头校正 */
.calibration-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;
	margin-bottom: 30rpx;
}

.calibration-item {
	background-color: var(--card-bg);
	border-radius: 12rpx;
	padding: 20rpx;
}

.calibration-label {
	color: var(--text-primary);
	font-size: 28rpx;
	text-align: center;
	margin-bottom: 15rpx;
}

.calibration-controls {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.control-btn {
	width: 60rpx;
	height: 60rpx;
	background-color: var(--accent-color);
	color: var(--text-primary);
	border: none;
	border-radius: 50%;
	font-size: 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.calibration-input {
	flex: 1;
	height: 60rpx;
	background-color: var(--secondary-bg);
	border: 1rpx solid var(--border-color);
	border-radius: 30rpx;
	text-align: center;
	color: var(--text-primary);
	font-size: 28rpx;
}

/* 开关设置 */
.switch-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;
	margin-top: 20rpx;
}

.switch-item {
	display: flex;
	flex-direction: row;
	justify-content: center;
	align-items: center;
	/* padding: 30rpx 20rpx; */
	/* background-color: var(--card-bg); */
	/* border-radius: 16rpx; */
	gap: 30rpx;
}

.switch-label {
	color: var(--text-primary);
	font-size: 28rpx;
}

/* 报警方式 */
.alarm-method-card {
	background-color: var(--card-bg);
	border: 2rpx solid var(--accent-color);
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
}

.method-options {
  display: flex;
  flex-direction: column;
  margin-bottom: 30rpx;
}

.method-label {
	color: var(--text-primary);
	font-size: 28rpx;
	margin-bottom: 20rpx;
}

.phone-list {
	margin-top: 20rpx;
}

.phone-input-row {
	display: flex;
	align-items: center;
	gap: 15rpx;
	margin-bottom: 15rpx;
}

.phone-input {
	flex: 1;
	height: 60rpx;
	background-color: var(--secondary-bg);
	border: 1rpx solid var(--border-color);
	border-radius: 30rpx;
	padding: 0 20rpx;
	color: var(--text-primary);
	font-size: 28rpx;
}

.add-btn {
	width: 60rpx;
	height: 60rpx;
	background-color: var(--accent-color);
	color: var(--text-primary);
	border: none;
	border-radius: 50%;
	font-size: 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.remove-btn {
	width: 60rpx;
	height: 60rpx;
	background-color: var(--error-color);
	color: var(--text-primary);
	border: none;
	border-radius: 50%;
	font-size: 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

/* 恢复出厂设置 */
.reset-card {
	display: flex;
	justify-content: space-between;
	align-items: center;
	background-color: var(--card-bg);
	border-radius: 16rpx;
	padding: 30rpx;
}

.reset-label {
	color: var(--text-primary);
	font-size: 28rpx;
}

.reset-btn {
	background-color: var(--error-color);
	color: var(--text-primary);
	border: none;
	border-radius: 30rpx;
	padding: 15rpx 30rpx;
	font-size: 28rpx;
}
</style>
