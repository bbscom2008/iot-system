<script>
	import request from '@/utils/request'
	
	export default {
		onLaunch: function() {
			console.log('App Launch')
			// 获取 logs
			const logs = uni.getStorageSync('logs') || []
			logs.unshift(Date.now())
			uni.setStorageSync('logs', logs)
			
			// 登录
			// uni.login({
			// 	success: (res) => {
			// 		console.log('login success', res)
			// 	}
			// })
			
			// 将 request 挂载到 globalData，方便在组件中使用
			this.globalData.$request = request
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		},
		globalData: {
			timer: 125,
			$request: request, // 将 request 挂载到 globalData
			routerConfig: {
				url: 'https://app.nongmuiot.com/baomingxiang-app/',
				tokenName: 'Authorization',
				tokenValue: '',
				version: 'v.1.0.30'
			},
			openRouterConfig: {
				tokenName: 'Authorization',
				enterpriseKey: '$2A$10$QGWBUEIM/LWJXUIVUOYQ5U3XZ7VNNCNUJ0W/N10Z39BBMBDTJD6BK',
				openToken: ''
			}
		}
	}
</script>

<style>
	/* 字体图标 */
	@font-face {
		font-family: iconfont;
		src: url('https://at.alicdn.com/t/font_2867763_pmlyvnygmin.woff2?t=1653702062972') format('woff2');
	}

	.iconfont {
		-webkit-font-smoothing: antialiased;
		-moz-osx-font-smoothing: grayscale;
		font-family: iconfont !important;
		font-size: 16px;
		font-style: normal;
	}

	/* 图标样式 */
	.icon-dianliang1::before {
		content: '\e673';
	}

	.icon-dianliang::before {
		content: '\e613';
	}

	.icon-tixian::before {
		content: '\e616';
	}

	.icon-lishijilu_huaban::before {
		content: '\e61c';
	}

	.icon-wendu::before {
		content: '\e62e';
	}

	.icon-icon_gangwei::before {
		content: '\e607';
	}

	.icon-yangzhi::before {
		content: '\e61e';
	}

	.icon-wangjimima::before {
		content: '\e65c';
	}

	.icon-qingchu::before {
		content: '\e62b';
	}

	.icon-banbenxinxi::before {
		content: '\e653';
	}

	.icon-dingwei::before {
		content: '\ec32';
	}

	.icon-yanzhengma::before {
		content: '\e6bf';
	}

	.icon-shaixuan1::before {
		content: '\e692';
	}

	.icon-xiugaiziliao::before {
		content: '\e618';
	}

	.icon-qingdan::before {
		content: '\e61a';
	}

	.icon-quxiao1::before {
		content: '\e67f';
	}

	.icon-saoyisao1::before {
		content: '\e604';
	}

	.icon-zhuangtaixinxi::before {
		content: '\e605';
	}

	.icon-saoyisao::before {
		content: '\e8b5';
	}

	.icon-export::before {
		content: '\e668';
	}

	.icon-weiduxiaoxi-05::before {
		content: '\e634';
	}

	.icon-sousuo::before {
		content: '\e600';
	}

	.icon-alarm-full::before {
		content: '\e872';
	}

	.icon-baojingjilu::before {
		content: '\e619';
	}

	.icon-shanchu::before {
		content: '\e615';
	}

	.icon-wode::before {
		content: '\e606';
	}

	.icon-lixian::before {
		content: '\e797';
	}

	.icon-zaixian::before {
		content: '\e7a3';
	}

	.icon-weixin::before {
		content: '\e601';
	}

	.icon-bianji::before {
		content: '\e602';
	}

	.icon-triangle-left::before {
		content: '\e67e';
	}

	.icon-shoujihao::before {
		content: '\e6c1';
	}

	.icon-xiugaishoujihao::before {
		content: '\e696';
	}

	.icon-80dianliang::before {
		content: '\e639';
	}

	.icon-quxiao::before {
		content: '\e625';
	}

	.icon-yiduxinxi::before {
		content: '\e638';
	}

	.icon-zuohua::before {
		content: '\e64b';
	}

	.icon-shouye1::before {
		content: '\e65d';
	}

	.icon-tianjia::before {
		content: '\e622';
	}

	.icon-duihao::before {
		content: '\e633';
	}

	.icon-daochu::before {
		content: '\e61b';
	}

	.icon-fenxiang::before {
		content: '\e7c5';
	}

	.icon-jinrujiantouxiao::before {
		content: '\e8d4';
	}

	.icon-mima::before {
		content: '\e640';
	}

	.icon-shuaxin2::before {
		content: '\e62d';
	}

	.icon-jibenxinxi::before {
		content: '\e637';
	}

	.icon-icon_tianjia::before {
		content: '\eb89';
	}

	.icon-shaixuan::before {
		content: '\e671';
	}

	.icon-shouye::before {
		content: '\e620';
	}

	.icon-alarm::before {
		content: '\e871';
	}

	.icon-lixian-xian::before {
		content: '\e796';
	}

	.icon-zaixian-xian::before {
		content: '\e7a2';
	}

	.icon-xinhao1::before {
		content: '\e612';
	}

	.icon-xinhao::before {
		content: '\e61f';
	}

	.icon-da80dianliang::before {
		content: '\e617';
	}

	.icon-dianliang-di::before {
		content: '\e672';
	}

	.icon-dianchi-zhongdianliang::before {
		content: '\e7a6';
	}

	.icon-shuaxin::before {
		content: '\ec08';
	}

	.icon-jia::before {
		content: '\e603';
	}

	.icon-jianshao::before {
		content: '\e8c5';
	}

	/* 通用容器样式 */
	.container {
		align-items: center;
		box-sizing: border-box;
		display: flex;
		flex-direction: column;
		height: 100%;
		justify-content: space-between;
		padding: 200rpx 0;
	}

	/* 通用元素重置 */
	image,
	navigator,
	page,
	swiper,
	swiper-item,
	text,
	view {
		box-sizing: border-box;
		margin: 0;
		padding: 0;
	}
	
	/* 页面背景色 */
	page {
		background-color: #04041A;
	}
	
	/* 全局颜色变量 */
	:root {
		--primary-bg: #04041A;
		--secondary-bg: #0A0A2E;
		--card-bg: #1A1A3A;
		--accent-color: #6A5ACD;
		--accent-cyan: #00FFFF;
		--text-primary: #E0E0E0;
		--text-secondary: #CCCCCC;
		--text-muted: #999999;
		--success-color: #72da72;
		--green-color: green;
		--warning-color: #FFA500;
		--error-color: #FF4444;
		--border-color: #333366;
	}

	/* 全局 uni-button 样式重写 */
	uni-button {
		line-height: 1.5;
	}
</style>
