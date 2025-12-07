<template>
	<view class="page">
		<view class="register-container">
			<view class="register-title">注册账号</view>
			<view class="register-form">
				<!-- 养殖类型 -->
				<view class="form-item">
					<view class="iconfont icon-yangzhi"></view>
					<picker 
						@change="onBreedingTypeChange" 
						:value="breedingTypeIndex" 
						:range="breedingTypes"
						class="picker"
					>
						<view class="picker-text" :class="{ 'placeholder': breedingTypeIndex < 0 }">
							{{ breedingTypeIndex >= 0 ? breedingTypes[breedingTypeIndex] : '请选择养殖类型' }}
						</view>
					</picker>
				</view>
				
				<!-- 角色 -->
				<view class="form-item">
					<SvgIcon name="user" size="18" class="form-icon" />
					<picker 
						@change="onRoleChange" 
						:value="roleIndex" 
						:range="roles"
						class="picker"
					>
						<view class="picker-text" :class="{ 'placeholder': roleIndex < 0 }">
							{{ roleIndex >= 0 ? roles[roleIndex] : '请选择角色' }}
						</view>
					</picker>
				</view>
				
				<!-- 手机号 -->
				<view class="form-item">
					<view class="iconfont icon-shoujihao"></view>
					<input class="input" type="number" placeholder="请输入手机号" v-model="phone" />
				</view>
				
			<!-- 验证码 -->
			<view class="form-item">
				<view class="iconfont icon-yanzhengma"></view>
				<input class="input" type="text" placeholder="请输入验证码" v-model="verifyCode" />
				<button 
					type="default" 
					class="verify-btn" 
					@tap.stop="sendVerifyCode" 
					:disabled="countdown > 0"
				>
					{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
				</button>
			</view>
				
				<!-- 密码 -->
				<view class="form-item">
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
				
				<button type="default" class="register-btn" @tap="handleRegister">注册</button>
				<view class="login-link">
					<text @tap="toLogin">已有账号？立即登录</text>
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
			countdown: 0,
			showPassword: false,
			// 养殖类型选项（与后端对应：0-猪 1-羊 2-牛 3-鸡 4-鸭 5-其他）
			breedingTypes: ['养猪', '养羊', '养牛', '养鸡', '养鸭', '其他'],
			breedingTypeIndex: -1, // 默认未选择
			// 角色选项（与后端对应：0-老板 1-饲养员 2-其他）
			roles: ['老板', '饲养员', '其他'],
			roleIndex: -1 // 默认未选择
		}
	},
	methods: {
		// 养殖类型选择
		onBreedingTypeChange(e) {
			this.breedingTypeIndex = e.detail.value
		},
		
		// 角色选择
		onRoleChange(e) {
			this.roleIndex = e.detail.value
		},
		
		// 注册
		async handleRegister() {
			// 验证养殖类型
			if (this.breedingTypeIndex < 0) {
				uni.showToast({
					title: '请选择养殖类型',
					icon: 'none'
				})
				return
			}
			
			// 验证角色
			if (this.roleIndex < 0) {
				uni.showToast({
					title: '请选择角色',
					icon: 'none'
				})
				return
			}
			
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
			
			// 验证验证码
			if (!this.verifyCode) {
				uni.showToast({
					title: '请输入验证码',
					icon: 'none'
				})
				return
			}
			
			// 验证密码
			if (!this.password) {
				uni.showToast({
					title: '请输入密码',
					icon: 'none'
				})
				return
			}
			
			// 密码长度验证
			if (this.password.length < 6) {
				uni.showToast({
					title: '密码长度不能少于6位',
					icon: 'none'
				})
				return
			}

			try {
				// 发送注册请求，字段名和类型与后端保持一致
				const res = await request.post('/user/register', {
					phone: this.phone,
					password: this.password,
					verifyCode: this.verifyCode,
					breedingType: parseInt(this.breedingTypeIndex), // 发送索引值（Integer）
					role: parseInt(this.roleIndex) // 注意：后端字段名是 role，不是 position
				})
				console.log("====== register ====", res);
				
				uni.showToast({
					title: '注册成功',
					icon: 'success'
				})
				setTimeout(() => {
					uni.reLaunch({
						url: `/pages/login/login?phone=${this.phone}`
					})
				}, 1000)
			} catch (err) {
				console.error("注册失败:", err);
				uni.showToast({
					title: err.msg || '注册失败',
					icon: 'none'
				})
			}
		},
		// 发送验证码（注册用）
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
				// 注册时使用 sendRegisterCode 接口
				await request.post('/user/sendRegisterCode', {
					phone: this.phone
				})
				uni.showToast({
					title: '验证码已发送，请查看控制台',
					icon: 'success',
					duration: 3000
				})
				this.startCountdown()
			} catch (err) {
				console.error("发送验证码失败:", err);
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
		// 切换密码显示
		togglePassword() {
			this.showPassword = !this.showPassword
		},
		// 跳转登录
		toLogin() {
			uni.navigateBack()
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

.register-container {
	width: 600rpx;
	padding: 60rpx 40rpx;
	background: #04041a;
	border: 1px solid #6A5ACD;
	border-radius: 8rpx;
	box-shadow: inset 0 0 10px 0 rgba(106, 90, 205, 0.3);
}

.register-title {
	color: #6A5ACD;
	font-size: 48rpx;
	text-align: center;
	margin-bottom: 60rpx;
	font-weight: bold;
}

.register-form {
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

.form-icon {
	color: #6A5ACD !important;
	margin-right: 20rpx;
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

.picker {
	flex: 1;
	height: 80rpx;
}

.picker-text {
	height: 80rpx;
	line-height: 80rpx;
	color: #fff;
	font-size: 28rpx;
}

.picker-text.placeholder {
	color: #999;
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

.register-btn {
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

.login-link {
	text-align: center;
	margin-top: 30rpx;
	color: #6A5ACD;
	font-size: 28rpx;
}

.login-link text {
	cursor: pointer;
	transition: opacity 0.3s;
}

.login-link text:hover {
	opacity: 0.8;
}
</style>