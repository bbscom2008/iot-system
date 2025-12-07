<template>
	<view class="page">
		<view class="device-info" v-if="deviceInfo.id">
			<view class="info-title">{{ deviceInfo.deviceName }}</view>
			<view class="info-item">
				<view class="label">设备编号：</view>
				<view class="value">{{ deviceInfo.deviceNum }}</view>
			</view>
			<view class="info-item">
				<view class="label">设备状态：</view>
				<view class="value">
					<text class="state-online" v-if="deviceInfo.deviceLineState == 1">在线</text>
					<text class="state-offline" v-else>离线</text>
				</view>
			</view>
			<view class="info-item">
				<view class="label">设备类型：</view>
				<view class="value">报警器</view>
			</view>
			<view class="info-item">
				<view class="label">信号强度：</view>
				<view class="value">{{ deviceInfo.signal }}</view>
			</view>
			<view class="info-item">
				<view class="label">电池电量：</view>
				<view class="value">{{ deviceInfo.electricQuantity }}%</view>
			</view>

			<!-- 传感器数据 -->
			<view class="sensor-data">
				<view class="sensor-title">传感器数据</view>
				<view class="sensor-grid">
					<!-- 温度传感器 -->
					<view class="sensor-item" v-for="sensor in getTemperatureSensors(deviceInfo.sensors)" :key="sensor.id">
						<view class="sensor-label">{{ sensor.sensorName }}</view>
						<view class="sensor-value">{{ sensor.sensorValue || '--' }}°C</view>
					</view>
					<!-- 其他传感器 -->
					<view class="sensor-item" v-for="sensor in getOtherSensors(deviceInfo.sensors)" :key="sensor.id">
						<view class="sensor-label">{{ sensor.sensorName }}</view>
						<view class="sensor-value">{{ sensor.sensorValue || '--' }}{{ sensor.unit }}</view>
					</view>
				</view>
			</view>

			<!-- 操作按钮 -->
			<view class="action-btns">
				<button class="action-btn" @tap="handleRefresh">刷新数据</button>
				<button class="action-btn delete-btn" @tap="handleDelete">删除设备</button>
			</view>
		</view>
		<view class="no-data" v-else>
			<view>暂无设备信息</view>
		</view>
	</view>
</template>

<script>
import request from '@/utils/request.js'

export default {
	data() {
		return {
			deviceInfo: {},
			deviceId: ''
		}
	},
	onLoad(options) {
		if (options.deviceId) {
			this.deviceId = options.deviceId
			this.getDeviceInfo()
		}
	},
	methods: {
		// 获取设备信息
		async getDeviceInfo() {
			try {
				const res = await request.get(`/device/detail/${this.deviceId}`)
				this.deviceInfo = res.data || {}
			} catch (err) {
				console.log('获取设备信息失败', err)
			}
		},
		// 刷新数据
		handleRefresh() {
			this.getDeviceInfo()
		},
		// 删除设备
		handleDelete() {
			uni.showModal({
				title: '提示',
				content: '确定要删除该设备吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							await request.put('/device/un/bind', {
								deviceNum: this.deviceInfo.deviceNum
							})
							uni.showToast({
								title: '删除成功',
								icon: 'success'
							})
							setTimeout(() => {
								uni.navigateBack()
							}, 1500)
						} catch (err) {
							uni.showToast({
								title: err.msg || '删除失败',
								icon: 'none'
							})
						}
					}
				}
			})
		},
		// 获取温度传感器（sensor_type_id = 5）
		getTemperatureSensors(sensors) {
			if (!sensors || !Array.isArray(sensors)) return [];
			return sensors.filter(s => s.sensorTypeId === 5);
		},
		// 获取其他传感器（sensor_type_id != 5）
		getOtherSensors(sensors) {
			if (!sensors || !Array.isArray(sensors)) return [];
			return sensors.filter(s => s.sensorTypeId !== 5);
		}
	}
}
</script>

<style scoped>
page {
	background-color: #080822;
}

.page {
	min-height: 100vh;
	background-color: #080822;
	padding: 40rpx 30rpx;
}

.device-info {
	background: #04041a;
	border: 1px solid #026b78;
	border-radius: 8rpx;
	box-shadow: inset 0 0 10px 0 #026b78;
	padding: 40rpx 30rpx;
}

.info-title {
	color: #0ff;
	font-size: 36rpx;
	text-align: center;
	margin-bottom: 40rpx;
	font-weight: bold;
}

.info-item {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
	font-size: 28rpx;
}

.label {
	color: #999;
	width: 180rpx;
}

.value {
	flex: 1;
	color: #fff;
}

.state-online {
	color: #0cb818;
}

.state-offline {
	color: #e3873d;
}

.sensor-data {
	margin-top: 40rpx;
	padding-top: 40rpx;
	border-top: 1px solid #026b78;
}

.sensor-title {
	color: #0ff;
	font-size: 32rpx;
	margin-bottom: 30rpx;
}

.sensor-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 30rpx;
}

.sensor-item {
	background-color: #1c1c26;
	border-radius: 8rpx;
	padding: 30rpx;
	text-align: center;
}

.sensor-label {
	color: #999;
	font-size: 24rpx;
	margin-bottom: 15rpx;
}

.sensor-value {
	color: #0ff;
	font-size: 32rpx;
	font-weight: bold;
}

.action-btns {
	margin-top: 40rpx;
	display: flex;
	gap: 20rpx;
}

.action-btn {
	flex: 1;
	height: 80rpx;
	line-height: 80rpx;
	background: #0ff;
	color: #333;
	border-radius: 8rpx;
	text-align: center;
	font-size: 28rpx;
	border: none;
}

.delete-btn {
	background: #ce0f1f;
	color: #fff;
}

.no-data {
	text-align: center;
	color: #999;
	margin-top: 200rpx;
	font-size: 28rpx;
}
</style>

