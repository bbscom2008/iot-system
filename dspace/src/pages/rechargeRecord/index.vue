<template>
	<view class="page">
		<!-- 标题 -->
		<view class="section-title">
			<view class="title-bar"></view>
			<text>充值记录</text>
		</view>
		
		<!-- 充值记录列表 -->
		<view class="record-list">
			<view class="record-card" v-for="(item, index) in recordList" :key="index">
				<!-- 充值金额 -->
				<view class="amount-section">
					<text class="amount-label">充值金额</text>
					<text class="amount-value">¥{{ item.amount }}</text>
				</view>
				
				<!-- 记录详情 -->
				<view class="detail-section">
					<view class="detail-row">
						<text class="detail-label">充值时间：</text>
						<text class="detail-value">{{ item.rechargeTime }}</text>
					</view>
					<view class="detail-row">
						<text class="detail-label">充值类型：</text>
						<text class="detail-value">{{ item.rechargeType }}</text>
					</view>
					<view class="detail-row">
						<text class="detail-label">设备类型：</text>
						<text class="detail-value">{{ item.deviceType }}</text>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view class="empty-state" v-if="!recordList.length">
				<view class="empty-icon">📝</view>
				<view class="empty-text">暂无充值记录</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			recordList: [
				{
					amount: '299.00',
					rechargeTime: '2025-10-21 14:30:25',
					rechargeType: '支付宝',
					deviceType: '报警器'
				},
				{
					amount: '199.00',
					rechargeTime: '2025-10-20 10:15:18',
					rechargeType: '微信支付',
					deviceType: '环控仪'
				},
				{
					amount: '499.00',
					rechargeTime: '2025-10-19 16:45:32',
					rechargeType: '支付宝',
					deviceType: '变频器'
				},
				{
					amount: '99.00',
					rechargeTime: '2025-10-18 09:20:10',
					rechargeType: '微信支付',
					deviceType: '报警器'
				},
				{
					amount: '399.00',
					rechargeTime: '2025-10-17 15:38:45',
					rechargeType: '支付宝',
					deviceType: '环控仪'
				},
				{
					amount: '299.00',
					rechargeTime: '2025-10-16 11:25:55',
					rechargeType: '微信支付',
					deviceType: '报警器'
				},
				{
					amount: '599.00',
					rechargeTime: '2025-10-15 13:50:20',
					rechargeType: '支付宝',
					deviceType: '变频器'
				},
				{
					amount: '199.00',
					rechargeTime: '2025-10-14 08:15:30',
					rechargeType: '微信支付',
					deviceType: '环控仪'
				}
			]
		}
	},
	onLoad() {
		console.log('充值记录列表:', this.recordList)
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

/* 标题样式（统一） */
.section-title {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
}

.title-bar {
	width: 6rpx;
	height: 40rpx;
	background-color: #6A5ACD;
	border-radius: 3rpx;
	margin-right: 15rpx;
}

.section-title text {
	color: #FFFFFF;
	font-size: 36rpx;
	font-weight: bold;
}

/* 记录列表 */
.record-list {
	padding-bottom: 40rpx;
}

/* 记录卡片 */
.record-card {
	background-color: #04041a;
	border: 2rpx solid rgba(106, 90, 205, 0.3);
	border-radius: 12rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	transition: all 0.3s;
}

.record-card:active {
	transform: scale(0.98);
	border-color: #6A5ACD;
}

/* 金额部分 */
.amount-section {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-bottom: 25rpx;
	margin-bottom: 25rpx;
	border-bottom: 1rpx solid rgba(106, 90, 205, 0.2);
}

.amount-label {
	color: #6A5ACD;
	font-size: 28rpx;
	font-weight: 500;
}

.amount-value {
	color: #05df72;
	font-size: 30rpx;
	font-weight: bold;
}

/* 详情部分 */
.detail-section {
	display: flex;
	flex-direction: column;
	gap: 15rpx;
}

.detail-row {
	display: flex;
	align-items: center;
}

.detail-label {
	color: #999;
	font-size: 26rpx;
	width: 160rpx;
	flex-shrink: 0;
}

.detail-value {
	color: #FFFFFF;
	font-size: 26rpx;
	flex: 1;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 0;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 30rpx;
	opacity: 0.3;
}

.empty-text {
	color: #999;
	font-size: 28rpx;
}
</style>

