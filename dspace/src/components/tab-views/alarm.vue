<template>
	<view class="page">
		<!-- 报警信息标题 -->
		<view class="section-title">
			<view class="title-bar"></view>
			<text>报警信息</text>
		</view>

		<!-- 日期筛选 -->
		<view class="filter-container">
			<view class="date-input">
				<input class="date-field" type="text" placeholder="2025/10/14" />
				<view class="calendar-icon">📅</view>
			</view>
			<view class="filter-buttons">
				<button class="filter-btn primary">查询</button>
				<button class="filter-btn secondary">重置</button>
			</view>
		</view>

		<!-- 报警列表 -->
		<view class="alarm-list">
			<view class="alarm-card" v-for="item in poList" :key="item.id" @tap="particulars(item.deviceNum)">
				<!-- 设备 ID -->
				<view class="alarm-device-id">{{ item.deviceNum }}</view>
				
				<!-- 报警详情 -->
				<view class="alarm-details">
					<view class="detail-row">
						<text class="detail-label">设备编号:</text>
						<text class="detail-value">{{ item.deviceNum }}</text>
					</view>
					<view class="detail-row">
						<text class="detail-label">设备类型:</text>
						<text class="detail-value">{{ typeMap[item.deviceType] }}</text>
					</view>
					<view class="detail-row">
						<text class="detail-label">报警时间:</text>
						<text class="detail-value">{{ item.alarmTime || '2025-08-21 11:31:22' }}</text>
					</view>
					<view class="detail-row">
						<text class="detail-label">报警原因:</text>
						<text class="detail-value alarm-reason" :class="getAlarmReasonClass(item.alarmReason)">
							{{ item.alarmReason || '高温' }}
						</text>
					</view>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-if="!poList.length">
				<view class="empty-icon">🔔</view>
				<view class="empty-text">暂无报警信息</view>
			</view>
		</view>
	</view>
</template>

<script>
import request from '@/utils/request.js'

export default {
	name: 'AlarmView',
	data() {
		return {
			poList: [],
			typeMap: {}
		}
	},
	mounted() {
		this.getAlarmList()
		this.getDictData()
	},
	methods: {
		// 获取报警原因样式类
		getAlarmReasonClass(reason) {
			if (!reason) return 'high-temp'
			if (reason.includes('高温') || reason.includes('温度')) return 'high-temp'
			if (reason.includes('断电') || reason.includes('离线')) return 'power-off'
			if (reason.includes('气体')) return 'gas-alarm'
			return 'default'
		},
		// 获取报警列表
		async getAlarmList() {
			try {
				const res = await request.get('/device/warning/list')
				this.poList = res.list || []
			} catch (err) {
				console.log('获取报警列表失败', err)
			}
		},
		// 获取设备类型字典
		async getDictData() {
			try {
				const res = await request.get('/user/dict/device-type/list')
				const typeMap = {}
				res.data.forEach((item) => {
					typeMap[item.typeId] = item.name
				})
				this.typeMap = typeMap
			} catch (err) {
				console.log('获取字典数据失败', err)
			}
		},
		// 跳转详情
		particulars(deviceNum) {
			uni.navigateTo({
				url: `/pages/particulars/particulars?deviceNum=${deviceNum}`
			})
		}
	}
}
</script>

<style scoped>
.page {
	min-height: 100vh;
	background-color: var(--primary-bg);
	padding: 0 30rpx 0 30rpx;
}

/* 章节标题 */
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

.section-title text {
	color: var(--text-primary);
	font-size: 36rpx;
	font-weight: bold;
}

/* 筛选容器 */
.filter-container {
	display: flex;
	align-items: center;
	gap: 20rpx;
	margin-bottom: 30rpx;
}

.date-input {
	position: relative;
	flex: 1;
}

.date-field {
	width: 100%;
	height: 70rpx;
	background-color: var(--card-bg);
	border-radius: 35rpx;
	padding: 0 50rpx 0 20rpx;
	color: var(--text-primary);
	font-size: 28rpx;
	border: none;
}

.calendar-icon {
	position: absolute;
	right: 20rpx;
	top: 50%;
	transform: translateY(-50%);
	font-size: 32rpx;
}

.filter-buttons {
	display: flex;
	gap: 15rpx;
}

.filter-btn {
	height: 70rpx;
	padding: 0 30rpx;
	border-radius: 35rpx;
	font-size: 28rpx;
	border: none;
	line-height: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}

.filter-btn.primary {
	background-color: var(--accent-color);
	color: var(--text-primary);
}

.filter-btn.secondary {
	background-color: transparent;
	color: var(--text-primary);
	border: 2rpx solid var(--text-primary);
}

/* 报警列表 */
.alarm-list {
	margin-bottom: 40rpx;
}

.alarm-card {
	background-color: var(--card-bg);
	border-radius: 16rpx;
	padding: 15rpx;
	margin-bottom: 20rpx;
	border: 2rpx solid var(--accent-color);
}

.alarm-device-id {
	font-size: 36rpx;
	font-weight: bold;
	color: var(--text-primary);
	margin-bottom: 20rpx;
}

.alarm-details {
	margin-bottom: 20rpx;
}

.detail-row {
	display: flex;
	margin-bottom: 15rpx;
}

.detail-label {
	color: var(--text-secondary);
	font-size: 28rpx;
	width: 180rpx;
}

.detail-value {
	color: var(--text-primary);
	font-size: 28rpx;
	flex: 1;
}

.alarm-reason {
	font-weight: bold;
}

.alarm-reason.high-temp {
	color: var(--error-color);
}

.alarm-reason.power-off {
	color: var(--warning-color);
}

.alarm-reason.gas-alarm {
	color: #FF6B6B;
}

/* 空状态 */
.empty-state {
	text-align: center;
	padding: 100rpx 0;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 30rpx;
	opacity: 0.3;
}

.empty-text {
	color: var(--text-muted);
	font-size: 28rpx;
}
</style>

