<template>
	<view class="page">
		<!-- 个人信息区域 -->
		<view class="user-section">
			<view class="avatar-container" @tap="addcopyreader">
				<!-- 如果有头像，显示头像图片；否则显示默认图标 -->
				<image 
					v-if="userInfo.icon" 
					:src="userInfo.icon" 
					class="avatar avatar-image"
					mode="aspectFill"
				></image>
				<view v-else class="avatar avatar-default">👤</view>
			</view>
			<view class="user-info">
				<view class="username">{{ userInfo.nikeName || '微信用户' }}</view>
				<view class="phone">{{ userInfo.phone || '未设置' }}</view>
			</view>
			<button type="default" class="edit-info-btn" @tap="addcopyreader">编辑信息</button>
		</view>

		<!-- 菜单列表 -->
		<view class="menu-section">
			<view class="menu-item" @tap="chanpassword">
				<view class="menu-dot"></view>
				<view class="menu-text">修改密码</view>
				<view class="menu-arrow">></view>
			</view>
			<view class="menu-item" @tap="rechargeRecordTo">
				<view class="menu-dot"></view>
				<view class="menu-text">充值记录</view>
				<view class="menu-arrow">></view>
			</view>
			<!-- <view class="menu-item" @tap="getUserProfiles">
				<view class="menu-dot"></view>
				<view class="menu-text">更新个人信息</view>
				<view class="menu-arrow">></view>
			</view> -->
		</view>

		<!-- 退出登录按钮 -->
		<button class="logout-btn" @tap="gameBack">退出登录</button>
	</view>
</template>

<script>
import request from '@/utils/request.js'

export default {
	name: 'MyMineView',
	data() {
		return {
			userInfo: {},
			version: 'v.1.0.30'
		}
	},
	mounted() {
		this.getUserInfo()
	},
	// 每次显示时刷新用户信息
	onShow() {
		this.getUserInfo()
	},
	methods: {
		// 获取用户信息
		async getUserInfo() {
			try {
				const res = await request.get('/user/info')
				this.userInfo = res.data || {}
				console.log('我的页面 - 用户信息:', this.userInfo)
				console.log('头像信息:', this.userInfo.icon ? '有头像' : '无头像')
				
				// 同步到本地存储
				if (this.userInfo.id) {
					uni.setStorageSync('userInfo', this.userInfo)
				}
			} catch (err) {
				console.log('获取用户信息失败', err)
			}
		},
		// 编辑个人信息
		addcopyreader() {
			if (!this.userInfo.id) {
				uni.showToast({
					title: '请先登录',
					icon: 'none'
				})
				return
			}
			uni.navigateTo({
				url: `/pages/user/user?id=${this.userInfo.id}`
			})
		},
		// 修改密码
		chanpassword() {
			uni.navigateTo({
				url: '/pages/password/password'
			})
		},
		// 充值记录
		rechargeRecordTo() {
			uni.navigateTo({
				url: '/pages/rechargeRecord/index'
			})
		},
		// 更新个人信息
		getUserProfiles() {
			// #ifdef MP-WEIXIN
			uni.getUserProfile({
				desc: '用于完善会员资料',
				success: (res) => {
					console.log('用户信息', res)
					this.updateUserInfo(res.userInfo)
				},
				fail: (err) => {
					console.log('获取用户信息失败', err)
				}
			})
			// #endif
			// #ifdef H5
			uni.showToast({
				title: 'H5 不支持获取用户信息',
				icon: 'none'
			})
			// #endif
		},
		// 更新用户信息
		async updateUserInfo(userInfo) {
			try {
				await request.post('/user/updateInfo', {
					nikeName: userInfo.nickName,
					icon: userInfo.avatarUrl
				})
				uni.showToast({
					title: '更新成功',
					icon: 'none'
				})
				this.getUserInfo()
			} catch (err) {
				uni.showToast({
					title: err.msg || '更新失败',
					icon: 'none'
				})
			}
		},
		// 退出登录
		gameBack() {
			uni.showModal({
				title: '提示',
				content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						uni.removeStorageSync('token')
						uni.removeStorageSync('userInfo')
						uni.reLaunch({
							url: '/pages/login/login'
						})
					}
				}
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

/* 个人信息区域 */
.user-section {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 60rpx 0;
}

.avatar-container {
	width: 120rpx;
	height: 120rpx;
	margin-bottom: 30rpx;
	cursor: pointer;
}

.avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	overflow: hidden;
	border: 3rpx solid #6A5ACD;
}

/* 默认头像样式 */
.avatar-default {
	background-color: var(--card-bg);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 60rpx;
	color: var(--text-muted);
}

/* 头像图片样式 */
.avatar-image {
	width: 100%;
	height: 100%;
	display: block;
	object-fit: cover;
}

.user-info {
	text-align: center;
	margin-bottom: 40rpx;
}

.username {
	font-size: 36rpx;
	color: var(--text-primary);
	margin-bottom: 10rpx;
}

.phone {
	font-size: 28rpx;
	color: var(--text-primary);
}

.edit-info-btn {
	background-color: var(--accent-color);
	color: var(--text-primary);
	border: none;
	border-radius: 35rpx;
	line-height: 1.2;
	padding: 20rpx 60rpx;
	font-size: 28rpx;
}

/* 菜单区域 */
.menu-section {
	margin-bottom: 60rpx;
}

.menu-item {
	display: flex;
	align-items: center;
	padding: 30rpx 0;
	border-bottom: 1rpx solid var(--border-color);
}

.menu-item:last-child {
	border-bottom: none;
}

.menu-dot {
	width: 12rpx;
	height: 12rpx;
	border-radius: 50%;
	background-color: var(--accent-color);
	margin-right: 20rpx;
}

.menu-text {
	flex: 1;
	color: var(--text-primary);
	font-size: 30rpx;
}

.menu-arrow {
	color: var(--text-muted);
	font-size: 28rpx;
}

/* 退出登录按钮 */
.logout-btn {
	width: 100%;
	line-height: 1.2;
	background-color: var(--accent-color);
	color: var(--text-primary);
	border: none;
	border-radius: 35rpx;
	padding: 25rpx 0;
	font-size: 32rpx;
	margin-bottom: 40rpx;
}
</style>

