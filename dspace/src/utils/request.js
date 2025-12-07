// 网络请求工具
// 根据环境变量设置不同的 BASE_URL
const BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'http://47.95.6.103:7900' // 生产环境
  : 'http://127.0.0.1:8080'   // 开发环境

console.log('当前环境:', process.env.NODE_ENV, 'BASE_URL:', BASE_URL)


let requestCount = 0
let isNavigatingToLogin = false // 防止重复跳转登录页

/**
 * 清除用户数据并跳转到登录页
 */
function clearUserDataAndLogin() {
	// 防止重复跳转
	if (isNavigatingToLogin) {
		return
	}
	isNavigatingToLogin = true
	
	// 清除本地存储数据
	uni.removeStorageSync('token')
	uni.removeStorageSync('userInfo')
	uni.removeStorageSync('vuex') // 清除 vuex 持久化数据
	
	// 提示用户
	uni.showToast({
		title: '登录已过期，请重新登录',
		icon: 'none',
		duration: 2000
	})
	
	// 延迟跳转，让用户看到提示
	setTimeout(() => {
		uni.reLaunch({
			url: '/pages/login/login',
			success: () => {
				// 重置标志位
				isNavigatingToLogin = false
			},
			fail: () => {
				// 如果跳转失败，也重置标志位
				isNavigatingToLogin = false
			}
		})
	}, 2000)
}

/**
 * 网络请求封装
 * @param {Object} options 请求配置
 * @returns {Promise}
 */
export const request = (options) => {
	const header = {
		...options.header
	}
	
	console.log('=options.url===', options.url);
	
	// 不需要token的接口白名单
	const noTokenUrls = [
		'user/login', 
		'user/register', 
		'user/sendVerifyCode', 
		'user/sendRegisterCode', 
		'user/sendLoginCode', 
		'user/loginByCode',
		'user/sendResetPasswordCode',
		'user/changePasswordByPhone'
	];
	const needToken = !noTokenUrls.includes(options.url);
	
	if (needToken) {
		// 获得 token 
		const token = uni.getStorageSync('token')
		// 如果没有，跳转登录
		if (!token) {
			uni.navigateTo({
				url: '/pages/login/login'
			})
		}
		header.Authorization = uni.getStorageSync('token')
	}
	
	requestCount++
	uni.showLoading({
		title: '加载中',
		mask: true
	})
	
	return new Promise((resolve, reject) => {
		uni.request({
			...options,
			url: BASE_URL + options.url,
			header,
			success: (res) => {
				console.log("===success=== res ====", res);
				
				// 检查 HTTP 状态码
				if (res.statusCode === 401) {
					clearUserDataAndLogin()
					// 拒绝这个请求
					reject({
						code: 401,
						msg: '登录已过期'
					})
					return
				}
				
				// 检查响应体的 code 字段（重要：处理业务错误）
				if (res.data && res.data.code !== 200) {
					console.log("业务错误:", res.data);
					
					// 检查是否是 token 失效相关的错误码
					const errorCode = res.data.code;
					const errorMsg = res.data.msg || '请求失败';
					
					// 处理 token 无效的情况（可能是 401 或其他自定义错误码）
					if (errorCode === 401 || 
						errorMsg.includes('token') && (errorMsg.includes('无效') || errorMsg.includes('失效') || errorMsg.includes('过期'))) {
						clearUserDataAndLogin()
						reject({
							code: errorCode,
							msg: '登录已过期'
						})
						return
					}
					
					// 其他业务错误，reject 并传递错误信息
					reject({
						code: errorCode,
						msg: errorMsg
					})
					return
				}
				
				// 成功的响应
				resolve(res.data.data)
			},
			fail: (err) => {
				console.log("===fail=== err ====", err);
				// 网络错误或其他错误，直接 reject
				reject(err)
			},
			complete: () => {
				requestCount--
				if (requestCount === 0) {
					uni.hideLoading()
				}
			}
		})
	})
}

/**
 * GET 请求
 */
export const get = (url, data = {}, type = '') => {
	return request({
		url,
		method: 'GET',
		data
	})
}

/**
 * POST 请求
 */
export const post = (url, data = {}, type = '') => {
	return request({
		url,
		method: 'POST',
		data,
		header: {
			'Content-Type': 'application/json'
		}
	})
}

/**
 * PUT 请求
 */
export const put = (url, data = {}, type = '') => {
	return request({
		url,
		method: 'PUT',
		data,
		header: {
			'Content-Type': 'application/json'
		}
	})
}

/**
 * DELETE 请求
 */
export const del = (url, data = {}, type = '') => {
	return request({
		url,
		method: 'DELETE',
		data
	})
}

export default {
	get,
	post,
	put,
	del,
	request
}

