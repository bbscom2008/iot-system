<template>
	<view class="recharge-page">
		<!-- 搜索栏 -->
		<view class="search-container">
			<view class="search-icon iconfont icon-sousuo"></view>
			<input class="search-input" placeholder="搜索设备" type="text" v-model="searchValue" />
		</view>

		<!-- 设备类型标题 -->
		<view class="section-title">
			<view class="title-bar"></view>
			<text>环控仪</text>
		</view>

		<!-- 设备卡片列表 -->
		<view class="device-list">
			<view class="device-card" v-for="item in deviceList" :key="item.id">
				<!-- 设备头部 -->
				<view class="device-header">
					<view class="device-id">{{ item.deviceNum }}</view>
					<button class="recharge-btn" @tap="handleRecharge(item)">续费充值</button>
				</view>
				
				<!-- 设备编号 -->
				<view class="device-number">设备号: {{ item.deviceNum }}</view>
				
				<!-- 订阅状态 -->
				<view class="subscription-info">
					<text class="remaining-days">剩余{{ item.remainingDays }}天</text>
					<text class="expiry-time"> | 到期时间: {{ item.expiryTime }}</text>
				</view>
				
				<!-- 剩余数量 -->
				<view class="quantity-info">
					<text>剩余数量: 允许</text>
					<text class="quantity-number">{{ item.allowedQuantity }}</text>
					<text>台 允许</text>
					<text class="quantity-number">{{ item.currentQuantity }}</text>
					<text>台</text>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view class="empty-state" v-if="!deviceList.length">
			<view class="empty-icon">📱</view>
			<view class="empty-text">暂无设备</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'RechargeView',
	data() {
		return {
			searchValue: '',
			deviceList: [
				{
					id: 1,
					deviceNum: '867920077581750',
					remainingDays: 35,
					expiryTime: '2026-08-09 00:00:00',
					allowedQuantity: 97,
					currentQuantity: 96
				},
				{
					id: 2,
					deviceNum: '867920077581751',
					remainingDays: 28,
					expiryTime: '2026-08-02 00:00:00',
					allowedQuantity: 50,
					currentQuantity: 48
				},
				{
					id: 3,
					deviceNum: '867920077581752',
					remainingDays: 15,
					expiryTime: '2026-07-20 00:00:00',
					allowedQuantity: 30,
					currentQuantity: 30
				}
			]
		}
	},
	methods: {
		// 处理续费充值
		handleRecharge(item) {
			uni.showModal({
				title: '续费充值',
				content: `确定要为设备 ${item.deviceNum} 进行续费充值吗？`,
				success: (res) => {
					if (res.confirm) {
						uni.showToast({
							title: '跳转到充值页面',
							icon: 'none'
						})
						// TODO: 跳转到充值页面
					}
				}
			})
		}
	}
}
</script>

<style scoped>
.recharge-page {
	min-height: 100vh;
	background-color: var(--primary-bg);
}

/* 搜索栏 */
.search-container {
	position: relative;
	margin-bottom: 20rpx;
}

.search-icon {
	position: absolute;
	left: 20rpx;
	top: 50%;
	transform: translateY(-50%);
	color: var(--text-muted);
	font-size: 32rpx;
	z-index: 2;
}

.search-input {
	width: 100%;
	height: 80rpx;
    box-sizing: border-box;
	background-color: var(--card-bg);
	border-radius: 40rpx;
	padding-left: 70rpx;
	color: var(--text-primary);
	font-size: 28rpx;
	border: none;
}

.search-input::placeholder {
	color: var(--text-muted);
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
	font-size: 36rpx;
	font-weight: bold;
}

/* 设备列表 */
.device-list {
	margin-bottom: 40rpx;
}

.device-card {
	background-color: var(--card-bg);
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

/* 设备头部 */
.device-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
}

.device-id {
	font-size: 30rpx;
	font-weight: bold;
	color: var(--text-primary);
}

.recharge-btn {
	background-color: var(--accent-color);
	color: var(--text-primary);
	border: none;
	border-radius: 20rpx;
	padding: 12rpx 24rpx;
	font-size: 24rpx;
	font-weight: 500;
}

.device-number {
	color: var(--text-primary);
	font-size: 28rpx;
	margin-bottom: 15rpx;
}

/* 订阅信息 */
.subscription-info {
	margin-bottom: 15rpx;
}

.remaining-days {
	color: var(--success-color);
	font-size: 28rpx;
	font-weight: 500;
}

.expiry-time {
	color: var(--text-primary);
	font-size: 28rpx;
}

/* 数量信息 */
.quantity-info {
	color: var(--text-primary);
	font-size: 28rpx;
}

.quantity-number {
	color: var(--success-color);
	font-weight: 500;
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

