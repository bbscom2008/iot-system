<template>
	<view class="page">
		<!-- 第一部分：通过原密码修改 -->
		<view class="section">
			<view class="section-title">
				<view class="title-bar"></view>
				<text>通过原密码修改</text>
			</view>
			<view class="form-container">
				<!-- 原密码 -->
				<view class="form-item">
					<view class="label">原密码</view>
					<view class="input-wrapper">
						<input 
							class="input" 
							:type="showOldPassword ? 'text' : 'password'" 
							placeholder="请输入原密码" 
							v-model="oldPassword" 
						/>
						<view class="eye-icon" @tap="toggleOldPassword">
							<SvgIcon 
								:name="showOldPassword ? 'eye-off' : 'eye'" 
								size="18" 
							/>
						</view>
					</view>
				</view>
				
				<!-- 新密码 -->
				<view class="form-item">
					<view class="label">新密码</view>
					<view class="input-wrapper">
						<input 
							class="input" 
							:type="showNewPassword1 ? 'text' : 'password'" 
							placeholder="请输入新密码（至少6位）" 
							v-model="newPassword1" 
						/>
						<view class="eye-icon" @tap="toggleNewPassword1">
							<SvgIcon 
								:name="showNewPassword1 ? 'eye-off' : 'eye'" 
								size="18" 
							/>
						</view>
					</view>
				</view>
				
				<button type="default" class="save-btn" @tap="handleChangeByOldPassword">保存</button>
			</view>
		</view>
		
		<!-- 第二部分：通过手机号修改 -->
		<view class="section" style="padding-top: 30rpx;" >
			<view class="section-title">
				<view class="title-bar"></view>
				<text>通过手机号修改</text>
			</view>
			<view class="form-container">
				<!-- 手机号 -->
				<view class="form-item">
					<view class="label">手机号</view>
					<input 
						class="input" 
						disabled
						type="number" 
						placeholder="请输入手机号" 
						v-model="phone" 
					/>
				</view>
				
				<!-- 验证码 -->
				<view class="form-item">
					<view class="label">验证码</view>
					<view class="input-wrapper">
						<input 
							class="input verify-input" 
							type="text" 
							placeholder="请输入验证码" 
							v-model="verifyCode" 
						/>
						<view 
							class="verify-btn" 
							:class="{ 'disabled': countdown > 0 }"
							@tap.stop="handleSendVerifyCode"
						>
							{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
						</view>
					</view>
				</view>
				
				<!-- 新密码 -->
				<view class="form-item">
					<view class="label">新密码</view>
					<view class="input-wrapper">
						<input 
							class="input" 
							:type="showNewPassword2 ? 'text' : 'password'" 
							placeholder="请输入新密码（至少6位）" 
							v-model="newPassword2" 
						/>
						<view class="eye-icon" @tap="toggleNewPassword2">
							<SvgIcon 
								:name="showNewPassword2 ? 'eye-off' : 'eye'" 
								size="18" 
							/>
						</view>
					</view>
				</view>
				
				<button type="default" class="save-btn" @tap="handleChangeByPhone">保存</button>
			</view>
		</view>
	</view>
</template>

<script>
import request from '@/utils/request.js'
import SvgIcon from '@/components/SvgIcon.vue'

export default {
	components: {
		SvgIcon
	},
	data() {
		return {
			// 通过原密码修改
			oldPassword: '',
			newPassword1: '',
			showOldPassword: false,
			showNewPassword1: false,
			
			// 通过手机号修改
			phone: '',
			verifyCode: '',
			newPassword2: '',
			showNewPassword2: false,
			countdown: 0,
		}
	},
	onLoad() {
		// 获取当前用户手机号
		this.getUserPhone()
	},
	methods: {
		// 获取当前用户手机号
		async getUserPhone() {
			try {
				const res = await request.get('/user/info')
				if (res.data && res.data.phone) {
					this.phone = res.data.phone
					console.log('当前用户手机号:', this.phone)
				}
			} catch (err) {
				console.error('获取用户信息失败:', err)
			}
		},
		
		// 切换原密码显示
		toggleOldPassword() {
			this.showOldPassword = !this.showOldPassword
		},
		
		// 切换新密码1显示
		toggleNewPassword1() {
			this.showNewPassword1 = !this.showNewPassword1
		},
		
		// 切换新密码2显示
		toggleNewPassword2() {
			this.showNewPassword2 = !this.showNewPassword2
		},
		
		// 通过原密码修改
		async handleChangeByOldPassword() {
			console.log('===== 通过原密码修改 =====')
			
			// 验证原密码
			if (!this.oldPassword) {
				uni.showToast({
					title: '请输入原密码',
					icon: 'none'
				})
				return
			}
			
			// 验证新密码
			if (!this.newPassword1) {
				uni.showToast({
					title: '请输入新密码',
					icon: 'none'
				})
				return
			}
			
			// 密码长度验证
			if (this.newPassword1.length < 6) {
				uni.showToast({
					title: '新密码至少6位',
					icon: 'none'
				})
				return
			}
			
			// 新旧密码不能相同
			if (this.oldPassword === this.newPassword1) {
				uni.showToast({
					title: '新密码不能与原密码相同',
					icon: 'none'
				})
				return
			}
			
			try {
				await request.post('/user/changePasswordByOld', {
					oldPassword: this.oldPassword,
					newPassword: this.newPassword1
				})
				
				uni.showToast({
					title: '密码修改成功，请重新登录',
					icon: 'success',
					duration: 2000
				})
				
				// 清除登录信息，跳转到登录页
				setTimeout(() => {
					uni.removeStorageSync('token')
					uni.removeStorageSync('userInfo')
					uni.reLaunch({
						url: '/pages/login/login?phone=' + this.phone
					})
				}, 2000)
				
			} catch (err) {
				console.error('修改密码失败:', err)
				uni.showToast({
					title: err.msg || '修改失败',
					icon: 'none'
				})
			}
		},
		
		// 发送验证码
		handleSendVerifyCode() {
			console.log('点击获取验证码')
			
			// 防止重复点击
			if (this.countdown > 0) {
				return
			}
			
			this.sendVerifyCode()
		},
		
		// 发送修改密码验证码
		async sendVerifyCode() {
			// 验证手机号
			if (!this.phone) {
				uni.showToast({
					title: '请输入手机号',
					icon: 'none'
				})
				return
			}
			
			// 手机号格式验证
			const phoneReg = /^1[3-9]\d{9}$/
			if (!phoneReg.test(this.phone)) {
				uni.showToast({
					title: '请输入正确的手机号',
					icon: 'none'
				})
				return
			}
			
			try {
				// 发送修改密码验证码
				await request.post('/user/sendResetPasswordCode', {
					phone: this.phone
				})
				
				uni.showToast({
					title: '验证码已发送，请查看控制台',
					icon: 'success',
					duration: 3000
				})
				
				this.startCountdown()
			} catch (err) {
				console.error('发送验证码失败:', err)
				uni.showToast({
					title: err.msg || '发送失败',
					icon: 'none'
				})
			}
		},
		
		// 开始倒计时
		startCountdown() {
			this.countdown = 60
			const timer = setInterval(() => {
				this.countdown--
				if (this.countdown <= 0) {
					clearInterval(timer)
				}
			}, 1000)
		},
		
		// 通过手机号修改
		async handleChangeByPhone() {
			console.log('===== 通过手机号修改 =====')
			
			// 验证手机号
			if (!this.phone) {
				uni.showToast({
					title: '请输入手机号',
					icon: 'none'
				})
				return
			}
			
			// 手机号格式验证
			const phoneReg = /^1[3-9]\d{9}$/
			if (!phoneReg.test(this.phone)) {
				uni.showToast({
					title: '请输入正确的手机号',
					icon: 'none'
				})
				return
			}
			
			// 验证码验证
			if (!this.verifyCode) {
				uni.showToast({
					title: '请输入验证码',
					icon: 'none'
				})
				return
			}
			
			// 新密码验证
			if (!this.newPassword2) {
				uni.showToast({
					title: '请输入新密码',
					icon: 'none'
				})
				return
			}
			
			// 密码长度验证
			if (this.newPassword2.length < 6) {
				uni.showToast({
					title: '新密码至少6位',
					icon: 'none'
				})
				return
			}
			
			try {
				await request.post('/user/changePasswordByPhone', {
					phone: this.phone,
					verifyCode: this.verifyCode,
					newPassword: this.newPassword2
				})
				
				uni.showToast({
					title: '密码修改成功，请重新登录',
					icon: 'success',
					duration: 2000
				})
				
				// 清除登录信息，跳转到登录页
				setTimeout(() => {
					uni.removeStorageSync('token')
					uni.removeStorageSync('userInfo')
					uni.reLaunch({
						url: '/pages/login/login?phone=' + this.phone
					})
				}, 2000)
				
			} catch (err) {
				console.error('修改密码失败:', err)
				uni.showToast({
					title: err.msg || '修改失败',
					icon: 'none'
				})
			}
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

.section {
	margin-bottom: 40rpx;
}

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

.form-container {
	padding: 0 20rpx;
}

.form-item {
	margin-bottom: 35rpx;
}

.label {
	color: #6A5ACD;
	font-size: 30rpx;
	margin-bottom: 20rpx;
	font-weight: 500;
}

.input-wrapper {
	display: flex;
	align-items: center;
	background-color: #1c1c26;
	border-radius: 12rpx;
	padding: 0 25rpx;
	border: 2rpx solid rgba(106, 90, 205, 0.3);
	position: relative;
	transition: border-color 0.3s;
}

.input-wrapper:focus-within {
	border-color: #6A5ACD;
}

.input {
	flex: 1;
	height: 70rpx;
	color: #fff;
	font-size: 28rpx;
	background: transparent;
	border: none;
	outline: none;
}

.input:focus {
	outline: none;
}

/* 眼睛图标 */
.eye-icon {
	color: #6A5ACD !important;
	margin-left: 20rpx;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 0 10rpx;
	height: 70rpx;
	flex-shrink: 0;
}

/* 验证码输入 */
.verify-input {
	flex: 1;
}

/* 验证码按钮 */
.verify-btn {
	flex-shrink: 0;
	background: #6A5ACD;
	color: #FFFFFF;
	border-radius: 6rpx;
	padding: 0 30rpx;
	height: 50rpx;
	line-height: 50rpx;
	font-size: 24rpx;
	text-align: center;
	margin-left: 20rpx;
	cursor: pointer;
	transition: background-color 0.3s;
}

.verify-btn.disabled {
	background: #666;
	color: #999;
	cursor: not-allowed;
}

.verify-btn:active:not(.disabled) {
	background-color: #5A4ACD;
}

/* 保存按钮 */
.save-btn {
	width: 100%;
	height: 80rpx;
	line-height: 80rpx;
	background: #6A5ACD;
	color: #FFFFFF;
	border-radius: 8rpx;
	text-align: center;
	font-size: 32rpx;
	margin-top: 40rpx;
	border: none;
	font-weight: 500;
	transition: background-color 0.3s;
}

.save-btn:active {
	background-color: #5A4ACD;
}
</style>

