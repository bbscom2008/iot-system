<template>
	<view class="page">
		<view class="login-container">
			<view class="login-title">农牧物联网</view>
			
			<!-- 登录方式切换 -->
			<view class="login-tabs">
				<view 
					class="tab-item" 
					:class="{ active: loginType === 'password' }"
					@tap="switchLoginType('password')"
				>
					密码登录
				</view>
				<view 
					class="tab-item" 
					:class="{ active: loginType === 'code' }"
					@tap="switchLoginType('code')"
				>
					验证码登录
				</view>
			</view>
			
			<view class="login-form">
				<view class="form-item">
					<view class="iconfont icon-shoujihao"></view>
					<input class="input" type="number" placeholder="请输入手机号" v-model="phone" />
				</view>
				
				<!-- 密码登录 -->
				<view v-if="loginType === 'password'" class="form-item">
					<view class="iconfont icon-mima"></view>
					<input 
						class="input" 
						:type="showPassword ? 'text' : 'password'" 
						placeholder="请输入密码" 
						v-model="password" 
					/>
					<view class="password-toggle" @tap="togglePassword">
						<SvgIcon 
							:name="showPassword ? 'eye-off' : 'eye'" 
							size="18" 
						/>
					</view>
				</view>
				
		<!-- 验证码登录 -->
		<view v-if="loginType === 'code'" class="form-item">
			<view class="iconfont icon-yanzhengma"></view>
			<input class="input" type="text" placeholder="请输入验证码" v-model="verifyCode" />
			<view 
				class="verify-btn" 
				:class="{ 'disabled': countdown > 0 }"
				@tap.stop="handleSendVerifyCode"
			>
				{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
			</view>
		</view>
				
			<button type="default" class="login-btn" @tap="handleLogin">
				{{ loginType === 'password' ? '登录' : '验证码登录' }}
			</button>
				
				<view class="register-link">
					<text @tap="toRegister">注册账号</text>
					<text v-if="loginType === 'password'" @tap="toForget">忘记密码</text>
				</view>
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
			phone: '',
			password: '',
			verifyCode: '',
			loginType: 'password', // 'password' 或 'code'
			countdown: 0,
			showPassword: false
		}
	},
	onLoad(options) {
		// 如果从注册页面跳转过来，自动填入手机号
		if (options.phone) {
			this.phone = options.phone
		}
	},
	methods: {
		// 切换登录方式
		switchLoginType(type) {
			console.log('===== 切换登录方式 =====');
			console.log('切换前:', this.loginType);
			console.log('切换后:', type);
			console.trace('调用栈:'); // 打印调用栈，看是谁调用了这个方法
			
			this.loginType = type
			// 清空相关输入
			if (type === 'password') {
				this.verifyCode = ''
			} else {
				this.password = ''
			}
		},
		
		// 处理发送验证码点击（增加防抖）
		handleSendVerifyCode() {
			console.log('===== 点击获取验证码 =====');
			console.log('当前登录类型:', this.loginType);
			
			// 防止重复点击
			if (this.countdown > 0) {
				return
			}
			
			this.sendVerifyCode()
		},
		
		// 发送验证码（登录用）
		async sendVerifyCode() {
			if (!this.phone) {
				uni.showToast({
					title: '请先输入手机号',
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
				console.log('准备发送验证码请求...');
				// 登录时使用 sendLoginCode 接口
				await request.post('/user/sendLoginCode', {
					phone: this.phone
				})
				console.log('验证码发送成功');
				console.log('发送后登录类型:', this.loginType);
				
				uni.showToast({
					title: '验证码已发送，请查看控制台',
					icon: 'success',
					duration: 3000
				})
				this.startCountdown()
			} catch (err) {
				console.error("发送验证码失败:", err);
				console.log('失败后登录类型:', this.loginType);
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
		
		// 登录
		async handleLogin() {
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
			
			if (this.loginType === 'password') {
				// 密码登录
				if (!this.password) {
					uni.showToast({
						title: '请输入密码',
						icon: 'none'
					})
					return
				}
				
				try {
					const res = await request.post('/user/login', {
						phone: this.phone,
						password: this.password
					})
					this.handleLoginSuccess(res)
				} catch (err) {
					console.error("登录失败:", err);
					uni.showToast({
						title: err.msg || '登录失败',
						icon: 'none'
					})
				}
			} else {
				// 验证码登录
				if (!this.verifyCode) {
					uni.showToast({
						title: '请输入验证码',
						icon: 'none'
					})
					return
				}
				
				try {
					const res = await request.post('/user/loginByCode', {
						phone: this.phone,
						verifyCode: this.verifyCode
					})
					this.handleLoginSuccess(res)
				} catch (err) {
					console.error("登录失败:", err);
					uni.showToast({
						title: err.msg || '登录失败',
						icon: 'none'
					})
				}
			}
		},
		
		// 处理登录成功
		handleLoginSuccess(res) {
			console.log("====== login ====", res);
			
			if (res.token) {
				uni.setStorageSync('token', res.token)
				uni.setStorageSync('userInfo', res.user)
				uni.showToast({
					title: '登录成功',
					icon: 'success'
				})
				setTimeout(() => {
					uni.reLaunch({
						url: '/pages/main/main'
					})
				}, 1500)
			}
		},
		
		// 跳转注册
		toRegister() {
			uni.navigateTo({
				url: '/pages/register/register'
			})
		},
		// 切换密码显示
		togglePassword() {
			this.showPassword = !this.showPassword
		},
		// 跳转忘记密码
		toForget() {
			// uni.navigateTo({
			// 	url: '/pages/password/password'
			// })
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
	display: flex;
	justify-content: center;
	align-items:flex-start;
	padding-top: 100rpx;
}

.login-container {
	width: 600rpx;
	padding: 60rpx 40rpx;
	background: #04041a;
	border: 1px solid #6A5ACD;
	border-radius: 8rpx;
	box-shadow: inset 0 0 10px 0 rgba(106, 90, 205, 0.3);
}

.login-title {
	color: #6A5ACD;
	font-size: 48rpx;
	text-align: center;
	margin-bottom: 40rpx;
	font-weight: bold;
}

.login-tabs {
	display: flex;
	margin-bottom: 40rpx;
	border-bottom: 1px solid #333;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 20rpx 0;
	color: #666;
	font-size: 28rpx;
	cursor: pointer;
	transition: all 0.3s;
}

.tab-item.active {
	color: #6A5ACD;
	border-bottom: 2px solid #6A5ACD;
	font-weight: bold;
}

.login-form {
	width: 100%;
}

.form-item {
	display: flex;
	align-items: center;
	background-color: #1c1c26;
	border-radius: 8rpx;
	padding: 0 20rpx;
	margin-bottom: 30rpx;
}

.form-item .iconfont {
	color: #6A5ACD;
	font-size: 36rpx;
	margin-right: 20rpx;
}

.input {
	flex: 1;
	height: 80rpx;
	color: #fff;
	font-size: 28rpx;
	background: transparent;
	border: none;
	outline: none;
	padding: 0;
}

.password-toggle {
	color: #6A5ACD !important;
	margin-left: 20rpx;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 0 10rpx;
	height: 80rpx;
}

.verify-btn {
	background: #6A5ACD;
	color: #FFFFFF;
	border-radius: 6rpx;
	padding: 10rpx 20rpx;
	font-size: 24rpx;
	border: none;
	margin-left: 20rpx;
}

.verify-btn:disabled {
	background: #666;
	color: #999;
}

.verify-btn.disabled {
	background: #666;
	color: #999;
	cursor: not-allowed;
}

.login-btn {
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
}

.register-link {
	display: flex;
	justify-content: space-between;
	margin-top: 30rpx;
	color: #6A5ACD;
	font-size: 28rpx;
}

.register-link text {
	cursor: pointer;
	transition: opacity 0.3s;
}

.register-link text:hover {
	opacity: 0.8;
}
</style>
