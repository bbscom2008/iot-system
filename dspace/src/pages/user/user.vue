<template>
	<view class="page">
		<!-- 第一部分：修改基本信息 -->
		<view class="section">
			<view class="section-title">
				<view class="title-bar"></view>
				<text>基本信息</text>
			</view>
			<view class="form-container">
				<!-- 头像上传 -->
				<view class="form-item avatar-item">
					<view class="label">头像</view>
					<view class="avatar-upload" @tap="chooseAvatar">
						<image v-if="userInfo.icon" :src="userInfo.icon" class="avatar-img"></image>
						<view v-else class="avatar-placeholder">
							<text class="iconfont icon-touxiang"></text>
							<text class="avatar-text">点击上传</text>
						</view>
					</view>
				</view>
				
				<!-- 昵称 -->
				<view class="form-item">
					<view class="label">昵称</view>
					<input class="input" type="text" placeholder="请输入昵称" v-model="userInfo.nikeName" />
				</view>
				
				<!-- 养殖类型 -->
				<view class="form-item">
					<view class="label">养殖类型</view>
					<picker 
						@change="onBreedingTypeChange" 
						:value="breedingTypeIndex" 
						:range="breedingTypes"
						class="picker-wrapper"
					>
						<view class="picker-input">
							<text :class="{ 'placeholder': breedingTypeIndex < 0 }">
								{{ breedingTypeIndex >= 0 ? breedingTypes[breedingTypeIndex] : '请选择养殖类型' }}
							</text>
							<text class="iconfont icon-xiala"></text>
						</view>
					</picker>
				</view>
				
				<!-- 岗位角色 -->
				<view class="form-item">
					<view class="label">岗位角色</view>
					<picker 
						@change="onRoleChange" 
						:value="roleIndex" 
						:range="roles"
						class="picker-wrapper"
					>
						<view class="picker-input">
							<text :class="{ 'placeholder': roleIndex < 0 }">
								{{ roleIndex >= 0 ? roles[roleIndex] : '请选择岗位角色' }}
							</text>
							<text class="iconfont icon-xiala"></text>
						</view>
					</picker>
				</view>
				
				<button type="default" class="save-btn" @tap="handleSaveInfo">保存基本信息</button>
			</view>
		</view>
		
		<!-- 第二部分：修改手机号 -->
		<view class="section">
			<view class="section-title">
				<view class="title-bar"></view>
				<text>修改手机号</text>
			</view>
			<view class="form-container">
				<!-- 原手机号 -->
				<view class="form-item">
					<view class="label">原手机号</view>
					<input class="input" type="text" :value="userInfo.phone" disabled />
				</view>
				
				<!-- 新手机号 -->
				<view class="form-item">
					<view class="label">新手机号</view>
					<input class="input" type="number" placeholder="请输入新手机号" v-model="newPhone" />
				</view>
				
				<!-- 验证码 -->
				<view class="form-item verify-item">
					<view class="label">验证码</view>
					<view class="verify-row">
						<input class="input verify-input" type="text" placeholder="请输入验证码" v-model="verifyCode" />
						<view 
							class="verify-btn" 
							:class="{ 'disabled': countdown > 0 }"
							@tap.stop="handleSendVerifyCode"
						>
							{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
						</view>
					</view>
				</view>
				
				<button type="default" class="save-btn" @tap="handleChangePhone">保存手机号</button>
			</view>
		</view>
	</view>
</template>

<script>
import request from '@/utils/request.js'

export default {
	data() {
		return {
			userInfo: {
				nikeName: '',
				phone: '',
				icon: '',
				breedingType: null,
				role: null
			},
			// 养殖类型选项
			breedingTypes: ['养猪', '养羊', '养牛', '养鸡', '养鸭', '其他'],
			breedingTypeIndex: -1,
			// 角色选项
			roles: ['老板', '饲养员', '其他'],
			roleIndex: -1,
			// 修改手机号相关
			newPhone: '',
			verifyCode: '',
			countdown: 0
		}
	},
	onLoad() {
		this.getUserInfo()
	},
	methods: {
		// 获取用户信息
		async getUserInfo() {
			try {
				const res = await request.get('/user/info')
				console.log('用户信息:', res);
				
				this.userInfo = res.data || {}
				
				// 设置养殖类型索引
				if (this.userInfo.breedingType !== null && this.userInfo.breedingType !== undefined) {
					this.breedingTypeIndex = this.userInfo.breedingType
				}
				
				// 设置角色索引
				if (this.userInfo.role !== null && this.userInfo.role !== undefined) {
					this.roleIndex = this.userInfo.role
				}
			} catch (err) {
				console.log('获取用户信息失败', err)
				uni.showToast({
					title: err.msg || '获取用户信息失败',
					icon: 'none'
				})
			}
		},
		
		// 选择头像
		chooseAvatar() {
			// #ifdef H5
			// H5环境：显示选择菜单（从相册、拍照）
			uni.showActionSheet({
				itemList: ['从相册选择', '拍照'],
				success: (res) => {
					if (res.tapIndex === 0) {
						// 从相册选择
						this.chooseImageFromAlbum()
					} else if (res.tapIndex === 1) {
						// 拍照
						this.chooseImageFromCamera()
					}
				}
			})
			// #endif
			
			// #ifdef MP-WEIXIN
			// 微信小程序环境：显示选择菜单（使用微信头像、从相册、拍照）
			uni.showActionSheet({
				itemList: ['使用微信头像', '从相册选择', '拍照'],
				success: (res) => {
					if (res.tapIndex === 0) {
						// 使用微信头像
						this.chooseWeChatAvatar()
					} else if (res.tapIndex === 1) {
						// 从相册选择
						this.chooseImageFromAlbum()
					} else if (res.tapIndex === 2) {
						// 拍照
						this.chooseImageFromCamera()
					}
				}
			})
			// #endif
			
			// #ifndef H5 || MP-WEIXIN
			// 其他平台：直接选择图片
			this.chooseImageFromAlbum()
			// #endif
		},
		
		// 从相册选择图片
		chooseImageFromAlbum() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album'], // 只从相册选择
				success: (res) => {
					const tempFilePath = res.tempFilePaths[0]
					console.log('从相册选择的图片:', tempFilePath)
					// 跳转到裁剪页面
					this.goToCropImage(tempFilePath)
				},
				fail: (err) => {
					console.error('选择图片失败:', err)
					uni.showToast({
						title: '选择图片失败',
						icon: 'none'
					})
				}
			})
		},
		
		// 拍照
		chooseImageFromCamera() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['camera'], // 只拍照
				success: (res) => {
					const tempFilePath = res.tempFilePaths[0]
					console.log('拍照获得的图片:', tempFilePath)
					// 跳转到裁剪页面
					this.goToCropImage(tempFilePath)
				},
				fail: (err) => {
					console.error('拍照失败:', err)
					uni.showToast({
						title: '拍照失败',
						icon: 'none'
					})
				}
			})
		},
		
		// 跳转到裁剪页面
		goToCropImage(imagePath) {
			console.log('跳转到裁剪页面:', imagePath)
			uni.navigateTo({
				url: '/pages/cropImage/cropImage?src=' + encodeURIComponent(imagePath)
			})
		},
		
		// 处理裁剪后的图片（由裁剪页面调用）
		handleCroppedImage(croppedImage) {
			console.log('接收到裁剪后的图片:', croppedImage.substring(0, 100))
			
			// 判断是Base64还是文件路径
			if (croppedImage.startsWith('data:image')) {
				// H5环境：直接是Base64，直接使用
				console.log('接收到Base64格式')
				this.userInfo.icon = croppedImage
				
				// 计算大小
				const sizeInKB = Math.round(croppedImage.length * 0.75 / 1024)
				console.log('裁剪后图片大小:', sizeInKB, 'KB')
				
				uni.showToast({
					title: '头像已设置，记得保存',
					icon: 'success'
				})
			} else {
				// 小程序环境：是文件路径，需要转换为Base64
				console.log('接收到文件路径，开始转换Base64')
				this.convertImageToBase64(croppedImage)
			}
		},
		
		// 使用微信头像（仅微信小程序）
		chooseWeChatAvatar() {
			// #ifdef MP-WEIXIN
			uni.showLoading({
				title: '获取中...',
				mask: true
			})
			
			// 获取用户信息（包括头像）
			uni.getUserProfile({
				desc: '用于完善用户资料',
				success: (res) => {
					console.log('微信用户信息:', res.userInfo)
					const avatarUrl = res.userInfo.avatarUrl
					
					if (avatarUrl) {
						// 下载微信头像
						uni.downloadFile({
							url: avatarUrl,
							success: (downloadRes) => {
								if (downloadRes.statusCode === 200) {
									console.log('微信头像下载成功:', downloadRes.tempFilePath)
									// 跳转到裁剪页面
									this.goToCropImage(downloadRes.tempFilePath)
								} else {
									uni.hideLoading()
									uni.showToast({
										title: '获取头像失败',
										icon: 'none'
									})
								}
							},
							fail: (err) => {
								console.error('下载头像失败:', err)
								uni.hideLoading()
								uni.showToast({
									title: '下载头像失败',
									icon: 'none'
								})
							}
						})
					} else {
						uni.hideLoading()
						uni.showToast({
							title: '未获取到微信头像',
							icon: 'none'
						})
					}
				},
				fail: (err) => {
					console.error('获取用户信息失败:', err)
					uni.hideLoading()
					uni.showToast({
						title: '获取用户信息失败',
						icon: 'none'
					})
				}
			})
			// #endif
			
			// #ifndef MP-WEIXIN
			uni.showToast({
				title: '此功能仅在微信小程序中可用',
				icon: 'none'
			})
			// #endif
		},
		
		// 将图片转换为Base64
		convertImageToBase64(filePath) {
			uni.showLoading({
				title: '处理中...',
				mask: true
			})
			
			// #ifdef H5
			// H5环境：使用FileReader
			this.convertImageToBase64H5(filePath)
			// #endif
			
			// #ifndef H5
			// 小程序环境：使用uni.getFileSystemManager
			this.convertImageToBase64WeChat(filePath)
			// #endif
		},
		
		// H5环境：使用FileReader转换Base64
		convertImageToBase64H5(filePath) {
			// 在H5中，filePath是blob URL，需要获取实际的File对象
			// 通过input元素获取文件
			const input = document.createElement('input')
			input.type = 'file'
			input.accept = 'image/*'
			
			// 或者直接从blob URL获取
			fetch(filePath)
				.then(res => res.blob())
				.then(blob => {
					const reader = new FileReader()
					
					reader.onload = (e) => {
						const base64String = e.target.result
						
						// 检查大小（建议不超过500KB）
						const sizeInKB = Math.round(base64String.length * 0.75 / 1024)
						console.log('Base64图片大小:', sizeInKB, 'KB')
						
						uni.hideLoading()
						
						if (sizeInKB > 500) {
							uni.showModal({
								title: '提示',
								content: `图片太大(${sizeInKB}KB)，建议压缩后再上传`,
								showCancel: true,
								confirmText: '继续使用',
								success: (modalRes) => {
									if (modalRes.confirm) {
										this.userInfo.icon = base64String
										uni.showToast({
											title: '头像已选择，记得保存',
											icon: 'success'
										})
									}
								}
							})
						} else {
							this.userInfo.icon = base64String
							uni.showToast({
								title: '头像已选择，记得保存',
								icon: 'success',
								duration: 2000
							})
							console.log('头像Base64已设置，大小:', sizeInKB, 'KB')
						}
					}
					
					reader.onerror = (err) => {
						console.error('读取文件失败:', err)
						uni.hideLoading()
						uni.showToast({
							title: '读取图片失败，请重试',
							icon: 'none'
						})
					}
					
					// 读取为DataURL（Base64）
					reader.readAsDataURL(blob)
				})
				.catch(err => {
					console.error('获取图片失败:', err)
					uni.hideLoading()
					uni.showToast({
						title: '获取图片失败，请重试',
						icon: 'none'
					})
				})
		},
		
		// 小程序环境：使用uni.getFileSystemManager转换Base64
		convertImageToBase64WeChat(filePath) {
			uni.getFileSystemManager().readFile({
				filePath: filePath,
				encoding: 'base64',
				success: (res) => {
					// 获取图片信息以确定图片类型
					uni.getImageInfo({
						src: filePath,
						success: (imageInfo) => {
							// 判断图片格式
							let imageType = 'jpeg' // 默认
							if (filePath.toLowerCase().includes('.png')) {
								imageType = 'png'
							} else if (filePath.toLowerCase().includes('.jpg') || filePath.toLowerCase().includes('.jpeg')) {
								imageType = 'jpeg'
							} else if (filePath.toLowerCase().includes('.gif')) {
								imageType = 'gif'
							}
							
							// 构建完整的Base64字符串
							const base64String = `data:image/${imageType};base64,${res.data}`
							
							// 检查大小（建议不超过500KB）
							const sizeInKB = Math.round(base64String.length * 0.75 / 1024)
							console.log('Base64图片大小:', sizeInKB, 'KB')
							
							if (sizeInKB > 500) {
								uni.hideLoading()
								uni.showModal({
									title: '提示',
									content: `图片太大(${sizeInKB}KB)，建议压缩后再上传`,
									showCancel: true,
									confirmText: '继续使用',
									success: (modalRes) => {
										if (modalRes.confirm) {
											this.userInfo.icon = base64String
											uni.showToast({
												title: '头像已选择，记得保存',
												icon: 'success'
											})
										}
									}
								})
							} else {
								this.userInfo.icon = base64String
								uni.hideLoading()
								uni.showToast({
									title: '头像已选择，记得保存',
									icon: 'success',
									duration: 2000
								})
								console.log('头像Base64已设置，大小:', sizeInKB, 'KB')
							}
						},
						fail: (err) => {
							console.error('获取图片信息失败:', err)
							// 即使获取图片信息失败，也尝试使用默认格式
							const base64String = `data:image/jpeg;base64,${res.data}`
							this.userInfo.icon = base64String
							uni.hideLoading()
							uni.showToast({
								title: '头像已选择，记得保存',
								icon: 'success'
							})
						}
					})
				},
				fail: (err) => {
					console.error('读取文件失败:', err)
					uni.hideLoading()
					uni.showToast({
						title: '读取图片失败，请重试',
						icon: 'none'
					})
				}
			})
		},
		
		// 养殖类型选择
		onBreedingTypeChange(e) {
			this.breedingTypeIndex = parseInt(e.detail.value)
			this.userInfo.breedingType = this.breedingTypeIndex
			console.log('养殖类型已选择:', this.breedingTypeIndex, this.breedingTypes[this.breedingTypeIndex]);
		},
		
		// 角色选择
		onRoleChange(e) {
			this.roleIndex = parseInt(e.detail.value)
			this.userInfo.role = this.roleIndex
			console.log('角色已选择:', this.roleIndex, this.roles[this.roleIndex]);
		},
		
		// 保存基本信息
		async handleSaveInfo() {
			console.log('=== 开始保存基本信息 ===');
			console.log('当前 userInfo:', JSON.stringify(this.userInfo, null, 2));
			console.log('breedingTypeIndex:', this.breedingTypeIndex);
			console.log('roleIndex:', this.roleIndex);
			
			// 验证昵称
			if (!this.userInfo.nikeName || this.userInfo.nikeName.trim() === '') {
				uni.showToast({
					title: '请输入昵称',
					icon: 'none'
				})
				return
			}
			
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
					title: '请选择岗位角色',
					icon: 'none'
				})
				return
			}
			
			// 确保 breedingType 和 role 是 Integer 类型
			const updateData = {
				nikeName: this.userInfo.nikeName,
				icon: this.userInfo.icon || null,
				breedingType: parseInt(this.breedingTypeIndex),
				role: parseInt(this.roleIndex)
			};
			
			console.log('准备发送的数据:', JSON.stringify(updateData, null, 2));
			
			try {
				await request.post('/user/updateInfo', updateData)
				
				console.log('保存成功，更新本地数据');
				
				uni.showToast({
					title: '保存成功',
					icon: 'success'
				})
				
				// 更新本地 userInfo
				this.userInfo.breedingType = updateData.breedingType;
				this.userInfo.role = updateData.role;
				
				// 更新本地存储的用户信息
				const storedUserInfo = uni.getStorageSync('userInfo') || {}
				storedUserInfo.nikeName = updateData.nikeName
				storedUserInfo.icon = updateData.icon
				storedUserInfo.breedingType = updateData.breedingType
				storedUserInfo.role = updateData.role
				uni.setStorageSync('userInfo', storedUserInfo)
				
				console.log('本地存储已更新');
				
			} catch (err) {
				console.error('保存失败:', err)
				uni.showToast({
					title: err.msg || '保存失败',
					icon: 'none'
				})
			}
		},
		
		// 发送验证码
		handleSendVerifyCode() {
			console.log('点击获取验证码');
			
			// 防止重复点击
			if (this.countdown > 0) {
				return
			}
			
			this.sendVerifyCode()
		},
		
		// 发送修改手机号验证码
		async sendVerifyCode() {
			if (!this.newPhone) {
				uni.showToast({
					title: '请先输入新手机号',
					icon: 'none'
				})
				return
			}
			
			// 手机号格式验证
			const phoneReg = /^1[3-9]\d{9}$/
			if (!phoneReg.test(this.newPhone)) {
				uni.showToast({
					title: '请输入正确的手机号',
					icon: 'none'
				})
				return
			}
			
			// 不能与原手机号相同
			if (this.newPhone === this.userInfo.phone) {
				uni.showToast({
					title: '新手机号不能与原手机号相同',
					icon: 'none'
				})
				return
			}
			
			try {
				// 发送修改手机号验证码
				await request.post('/user/sendChangePhoneCode', {
					phone: this.newPhone
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
		
		// 修改手机号
		async handleChangePhone() {
			// 验证新手机号
			if (!this.newPhone) {
				uni.showToast({
					title: '请输入新手机号',
					icon: 'none'
				})
				return
			}
			
			// 手机号格式验证
			const phoneReg = /^1[3-9]\d{9}$/
			if (!phoneReg.test(this.newPhone)) {
				uni.showToast({
					title: '请输入正确的手机号',
					icon: 'none'
				})
				return
			}
			
			// 验证码
			if (!this.verifyCode) {
				uni.showToast({
					title: '请输入验证码',
					icon: 'none'
				})
				return
			}
			
			try {
				await request.post('/user/changePhone', {
					newPhone: this.newPhone,
					verifyCode: this.verifyCode
				})
				
				uni.showToast({
					title: '手机号修改成功，请重新登录',
					icon: 'success',
					duration: 2000
				})
				
				// 清除登录信息
				setTimeout(() => {
					uni.removeStorageSync('token')
					uni.removeStorageSync('userInfo')
					uni.reLaunch({
						url: '/pages/login/login?phone=' + this.newPhone
					})
				}, 2000)
				
			} catch (err) {
				console.error('修改手机号失败:', err)
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
	padding: 0;
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

.input {
	width: 100%;
	height: 70rpx;
	box-sizing: border-box;
	background-color: #1c1c26;
	border-radius: 12rpx;
	padding: 0 25rpx;
	color: #fff;
	font-size: 28rpx;
	border: 2rpx solid rgba(106, 90, 205, 0.3);
	transition: border-color 0.3s;
}

.input:disabled {
	color: #666;
	background-color: #0a0a14;
}

.input:focus {
	border-color: #6A5ACD;
	outline: none;
}

/* 头像上传样式 */
.avatar-item {
	display: flex;
	align-items: center;
}

.avatar-upload {
	width: 120rpx;
	height: 120rpx;
	margin-left: 30rpx;
	cursor: pointer;
}

.avatar-img {
	width: 100%;
	height: 100%;
	border-radius: 50%;
	border: 2rpx solid #6A5ACD;
}

.avatar-placeholder {
	width: 100%;
	height: 100%;
	border-radius: 50%;
	border: 2rpx dashed #6A5ACD;
	background-color: #1c1c26;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	color: #6A5ACD;
}

.avatar-placeholder .iconfont {
	font-size: 48rpx;
	margin-bottom: 8rpx;
}

.avatar-text {
	font-size: 20rpx;
}

/* 选择器样式 */
.picker-wrapper {
	width: 100%;
}

.picker-input {
	width: 100%;
	height: 70rpx;
	box-sizing: border-box;
	background-color: #1c1c26;
	border-radius: 12rpx;
	padding: 0 25rpx;
	border: 2rpx solid rgba(106, 90, 205, 0.3);
	display: flex;
	align-items: center;
	justify-content: space-between;
	transition: border-color 0.3s;
}

.picker-input text {
	color: #fff;
	font-size: 28rpx;
}

.picker-input .placeholder {
	color: #999;
}

.picker-input .iconfont {
	color: #6A5ACD;
	font-size: 24rpx;
}

/* 验证码行 */
.verify-item .label {
	margin-bottom: 15rpx;
}

.verify-row {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.verify-input {
	flex: 1;
}

.verify-btn {
	flex-shrink: 0;
	background: #6A5ACD;
	color: #FFFFFF;
	border-radius: 8rpx;
	padding: 0 30rpx;
	height: 70rpx;
	line-height: 70rpx;
	font-size: 24rpx;
	text-align: center;
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
