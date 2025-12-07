<template>
	<view class="crop-container">
		<!-- 顶部标题栏 -->
		<view class="header">
			<view class="cancel-btn" @tap="handleCancel">取消</view>
			<view class="title">调整头像</view>
			<view class="confirm-btn" @tap="handleConfirm">确定</view>
		</view>
		
		<!-- 裁剪区域 -->
		<view class="crop-area">
			<!-- 可移动的图片 -->
			<view 
				class="image-wrapper"
				:style="imageWrapperStyle"
				@touchstart="handleTouchStart"
				@touchmove="handleTouchMove"
				@touchend="handleTouchEnd"
			>
				<image 
					:src="imageSrc" 
					class="crop-image"
					:style="imageStyle"
					mode="widthFix"
					@load="handleImageLoad"
				></image>
			</view>
			
			<!-- 裁剪框（正方形） -->
			<view class="crop-box" :style="cropBoxStyle">
				<view class="corner corner-tl"></view>
				<view class="corner corner-tr"></view>
				<view class="corner corner-bl"></view>
				<view class="corner corner-br"></view>
			</view>
			
			<!-- 遮罩层 -->
			<view class="mask-top" :style="maskTopStyle"></view>
			<view class="mask-bottom" :style="maskBottomStyle"></view>
		</view>
		
		<!-- 提示文字 -->
		<view class="tip">上下拖动图片调整裁剪区域</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			imageSrc: '',           // 图片源
			imageWidth: 0,          // 图片实际宽度
			imageHeight: 0,         // 图片实际高度
			cropBoxSize: 0,         // 裁剪框大小（正方形）
			imageTop: 0,            // 图片当前top位置
			touchStartY: 0,         // 触摸开始Y坐标
			startImageTop: 0,       // 触摸开始时图片的top
			containerHeight: 0,     // 容器高度
			screenWidth: 0,         // 屏幕宽度
		}
	},
	computed: {
		// 裁剪框样式
		cropBoxStyle() {
			return {
				width: this.cropBoxSize + 'px',
				height: this.cropBoxSize + 'px',
				left: '50%',
				top: '50%',
				marginLeft: -(this.cropBoxSize / 2) + 'px',
				marginTop: -(this.cropBoxSize / 2) + 'px',
			}
		},
		// 图片容器样式
		imageWrapperStyle() {
			return {
				width: this.cropBoxSize + 'px',
				top: this.imageTop + 'px',
			}
		},
		// 图片样式
		imageStyle() {
			return {
				width: this.cropBoxSize + 'px',
			}
		},
		// 上遮罩样式
		maskTopStyle() {
			const top = (this.containerHeight - this.cropBoxSize) / 2
			return {
				height: top + 'px'
			}
		},
		// 下遮罩样式
		maskBottomStyle() {
			const top = (this.containerHeight + this.cropBoxSize) / 2
			return {
				top: top + 'px',
				height: (this.containerHeight - top) + 'px'
			}
		}
	},
	onLoad(options) {
		// 获取传递的图片路径
		if (options.src) {
			this.imageSrc = decodeURIComponent(options.src)
			console.log('接收到的图片路径:', this.imageSrc)
		}
		
		// 获取屏幕信息
		const systemInfo = uni.getSystemInfoSync()
		this.screenWidth = systemInfo.windowWidth
		this.containerHeight = systemInfo.windowHeight - 100 // 减去头部和提示的高度
		
		// 设置裁剪框大小（屏幕宽度的60%）
		this.cropBoxSize = Math.floor(this.screenWidth * 0.6)
		
		console.log('裁剪框大小:', this.cropBoxSize)
	},
	methods: {
		// 图片加载完成
		handleImageLoad(e) {
			console.log('图片加载完成:', e)
			
			// 获取图片信息
			uni.getImageInfo({
				src: this.imageSrc,
				success: (res) => {
					console.log('图片信息:', res)
					this.imageWidth = res.width
					this.imageHeight = res.height
					
					// 计算图片显示高度（按宽度缩放）
					const displayHeight = (this.imageHeight / this.imageWidth) * this.cropBoxSize
					
					// 初始化图片位置（居中）
					this.imageTop = (this.containerHeight - displayHeight) / 2
					
					// 如果图片高度小于裁剪框，则居中显示
					if (displayHeight < this.cropBoxSize) {
						this.imageTop = (this.containerHeight - displayHeight) / 2
					}
					
					console.log('图片显示高度:', displayHeight)
					console.log('初始位置:', this.imageTop)
				},
				fail: (err) => {
					console.error('获取图片信息失败:', err)
					uni.showToast({
						title: '图片加载失败',
						icon: 'none'
					})
				}
			})
		},
		
		// 触摸开始
		handleTouchStart(e) {
			this.touchStartY = e.touches[0].clientY
			this.startImageTop = this.imageTop
			console.log('触摸开始:', this.touchStartY)
		},
		
		// 触摸移动
		handleTouchMove(e) {
			const touchY = e.touches[0].clientY
			const deltaY = touchY - this.touchStartY
			let newTop = this.startImageTop + deltaY
			
			// 计算图片显示高度
			const displayHeight = (this.imageHeight / this.imageWidth) * this.cropBoxSize
			const cropBoxTop = (this.containerHeight - this.cropBoxSize) / 2
			const cropBoxBottom = cropBoxTop + this.cropBoxSize
			
			// 限制移动范围，确保裁剪框内始终有图片
			const minTop = cropBoxBottom - displayHeight
			const maxTop = cropBoxTop
			
			if (newTop < minTop) {
				newTop = minTop
			}
			if (newTop > maxTop) {
				newTop = maxTop
			}
			
			this.imageTop = newTop
		},
		
		// 触摸结束
		handleTouchEnd() {
			console.log('触摸结束，最终位置:', this.imageTop)
		},
		
		// 取消
		handleCancel() {
			uni.navigateBack()
		},
		
		// 确定裁剪
		handleConfirm() {
			console.log('===== 开始裁剪 =====')
			console.log('图片信息:', {
				width: this.imageWidth,
				height: this.imageHeight,
				top: this.imageTop,
				src: this.imageSrc.substring(0, 100)
			})
			
			// 检查图片是否已加载
			if (!this.imageWidth || !this.imageHeight) {
				console.error('图片尺寸未获取，请稍等')
				uni.showToast({
					title: '图片加载中，请稍等',
					icon: 'none'
				})
				
				// 延迟重试
				setTimeout(() => {
					if (this.imageWidth && this.imageHeight) {
						this.handleConfirm()
					} else {
						uni.showToast({
							title: '图片加载失败，请重新选择',
							icon: 'none'
						})
					}
				}, 1000)
				return
			}
			
			uni.showLoading({
				title: '处理中...',
				mask: true
			})
			
			// 创建canvas进行裁剪
			try {
				this.cropImage()
			} catch (err) {
				console.error('裁剪过程出错:', err)
				uni.hideLoading()
				uni.showToast({
					title: '裁剪失败: ' + err.message,
					icon: 'none',
					duration: 3000
				})
			}
		},
		
		// 裁剪图片
		cropImage() {
			// 计算裁剪参数
			const displayHeight = (this.imageHeight / this.imageWidth) * this.cropBoxSize
			const cropBoxTop = (this.containerHeight - this.cropBoxSize) / 2
			
			// 图片在裁剪框中的偏移
			const offsetY = cropBoxTop - this.imageTop
			
			// 计算在原图中的裁剪区域
			const scale = this.imageWidth / this.cropBoxSize
			const cropX = 0
			const cropY = offsetY * scale
			const cropWidth = this.imageWidth
			const cropHeight = this.imageWidth // 正方形，所以高度=宽度
			
			console.log('裁剪参数:', {
				cropX, cropY, cropWidth, cropHeight,
				scale, offsetY, displayHeight
			})
			
			// #ifdef H5
			// H5环境：使用HTML5 Canvas API
			this.cropImageH5(cropX, cropY, cropWidth, cropHeight)
			// #endif
			
			// #ifndef H5
			// 小程序环境：使用uni.createCanvasContext
			this.cropImageMiniProgram(cropX, cropY, cropWidth, cropHeight)
			// #endif
		},
		
		// H5环境裁剪
		cropImageH5(cropX, cropY, cropWidth, cropHeight) {
			console.log('===== H5环境裁剪 =====')
			console.log('裁剪区域:', { cropX, cropY, cropWidth, cropHeight })
			
			const canvasSize = Math.min(this.imageWidth, 800)
			console.log('Canvas大小:', canvasSize)
			
			// 创建图片对象
			const img = new Image()
			img.crossOrigin = 'anonymous'
			
			img.onload = () => {
				console.log('图片加载成功，开始绘制canvas')
				
				try {
					// 创建canvas
					const canvas = document.createElement('canvas')
					canvas.width = canvasSize
					canvas.height = canvasSize
					const ctx = canvas.getContext('2d')
					
					console.log('Canvas创建成功，开始绘制')
					
					// 计算缩放比例
					const scale = canvasSize / cropWidth
					console.log('缩放比例:', scale)
					
					// 绘制裁剪后的图片
					ctx.drawImage(
						img,
						cropX, cropY, cropWidth, cropHeight,  // 源图裁剪区域
						0, 0, canvasSize, canvasSize          // 目标区域
					)
					
					console.log('绘制完成，开始转换Base64')
					
					// 转换为Base64
					const base64 = canvas.toDataURL('image/jpeg', 0.9)
					console.log('裁剪成功，Base64长度:', base64.length)
					console.log('Base64前50字符:', base64.substring(0, 50))
					
					uni.hideLoading()
					
					// 返回上一页并传递裁剪后的图片（Base64）
					const pages = getCurrentPages()
					console.log('当前页面栈:', pages.length)
					const prevPage = pages[pages.length - 2]
					
					if (prevPage && prevPage.handleCroppedImage) {
						console.log('调用上一页的handleCroppedImage方法')
						// H5环境直接传递Base64
						prevPage.handleCroppedImage(base64)
						
						console.log('准备返回上一页')
						uni.navigateBack()
					} else {
						console.error('未找到上一页或handleCroppedImage方法')
						uni.showToast({
							title: '返回失败，请手动返回',
							icon: 'none'
						})
					}
				} catch (err) {
					console.error('Canvas处理失败:', err)
					uni.hideLoading()
					uni.showToast({
						title: '裁剪失败: ' + err.message,
						icon: 'none',
						duration: 3000
					})
				}
			}
			
			img.onerror = (err) => {
				console.error('图片加载失败:', err)
				uni.hideLoading()
				uni.showToast({
					title: '图片加载失败',
					icon: 'none'
				})
			}
			
			console.log('开始加载图片:', this.imageSrc.substring(0, 100))
			// 加载图片
			img.src = this.imageSrc
		},
		
		// 小程序环境裁剪
		cropImageMiniProgram(cropX, cropY, cropWidth, cropHeight) {
			console.log('使用小程序方式裁剪')
			
			// 创建canvas上下文
			const ctx = uni.createCanvasContext('cropCanvas', this)
			
			// 设置canvas大小为正方形（使用原图尺寸以保证质量）
			const canvasSize = Math.min(this.imageWidth, 800)
			
			// 绘制裁剪后的图片
			ctx.drawImage(
				this.imageSrc,
				cropX, cropY, cropWidth, cropHeight,  // 源图裁剪区域
				0, 0, canvasSize, canvasSize          // 目标区域
			)
			
			// 绘制完成后导出
			ctx.draw(false, () => {
				setTimeout(() => {
					uni.canvasToTempFilePath({
						canvasId: 'cropCanvas',
						width: canvasSize,
						height: canvasSize,
						destWidth: canvasSize,
						destHeight: canvasSize,
						fileType: 'jpg',
						quality: 0.9,
						success: (res) => {
							console.log('裁剪成功:', res.tempFilePath)
							uni.hideLoading()
							
							// 返回上一页并传递裁剪后的图片
							const pages = getCurrentPages()
							const prevPage = pages[pages.length - 2]
							
							// 调用上一页的方法处理裁剪后的图片
							if (prevPage && prevPage.handleCroppedImage) {
								prevPage.handleCroppedImage(res.tempFilePath)
							}
							
							uni.navigateBack()
						},
						fail: (err) => {
							console.error('裁剪失败:', err)
							uni.hideLoading()
							uni.showToast({
								title: '裁剪失败，请重试',
								icon: 'none'
							})
						}
					}, this)
				}, 500)
			})
		}
	}
}
</script>

<style scoped>
page {
	background-color: #000000;
}

.crop-container {
	width: 100%;
	height: 100vh;
	background-color: #000000;
	position: relative;
	overflow: hidden;
}

/* 头部 */
.header {
	height: 100rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 30rpx;
	background-color: rgba(0, 0, 0, 0.8);
	position: relative;
	z-index: 100;
}

.cancel-btn,
.confirm-btn {
	color: #6A5ACD;
	font-size: 32rpx;
	padding: 10rpx 20rpx;
	cursor: pointer;
}

.confirm-btn {
	font-weight: bold;
}

.title {
	color: #FFFFFF;
	font-size: 36rpx;
	font-weight: bold;
}

/* 裁剪区域 */
.crop-area {
	position: relative;
	width: 100%;
	height: calc(100vh - 200rpx);
	overflow: hidden;
}

/* 图片容器 */
.image-wrapper {
	position: absolute;
	left: 50%;
	transform: translateX(-50%);
	z-index: 1;
}

.crop-image {
	display: block;
	width: 100%;
}

/* 裁剪框 */
.crop-box {
	position: absolute;
	border: 2rpx solid #6A5ACD;
	box-shadow: 0 0 0 2000px rgba(0, 0, 0, 0.5);
	z-index: 10;
	pointer-events: none;
}

/* 裁剪框四角 */
.corner {
	position: absolute;
	width: 40rpx;
	height: 40rpx;
	border: 4rpx solid #6A5ACD;
}

.corner-tl {
	top: -4rpx;
	left: -4rpx;
	border-right: none;
	border-bottom: none;
}

.corner-tr {
	top: -4rpx;
	right: -4rpx;
	border-left: none;
	border-bottom: none;
}

.corner-bl {
	bottom: -4rpx;
	left: -4rpx;
	border-right: none;
	border-top: none;
}

.corner-br {
	bottom: -4rpx;
	right: -4rpx;
	border-left: none;
	border-top: none;
}

/* 遮罩层 */
.mask-top,
.mask-bottom {
	position: absolute;
	left: 0;
	right: 0;
	background-color: rgba(0, 0, 0, 0.7);
	z-index: 9;
	pointer-events: none;
}

.mask-top {
	top: 0;
}

/* 提示文字 */
.tip {
	position: absolute;
	bottom: 60rpx;
	left: 0;
	right: 0;
	text-align: center;
	color: rgba(255, 255, 255, 0.8);
	font-size: 28rpx;
	z-index: 100;
}

/* Canvas（隐藏） */
canvas {
	position: fixed;
	left: -9999px;
	top: -9999px;
}
</style>

<!-- Canvas元素（用于裁剪） -->
<canvas canvas-id="cropCanvas" style="position: fixed; left: -9999px; top: -9999px;"></canvas>

