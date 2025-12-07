<template>
	<view class="main-container">
		<!-- 内容区域 -->
		<view class="content-container">
			<HomeView v-if="currentTab === 0" />
			<AlarmView v-else-if="currentTab === 1" />
			<MyMineView v-else-if="currentTab === 2" />
		</view>
		
		<!-- 自定义底部 TabBar -->
		<CustomTabBar :current="currentTab" @change="handleTabChange" />
	</view>
</template>

<script>
import HomeView from '@/components/tab-views/home.vue'
import AlarmView from '@/components/tab-views/alarm.vue'
import MyMineView from '@/components/tab-views/myMine.vue'
import CustomTabBar from '@/custom-tab-bar/index.vue'

export default {
	name: 'MainPage',
	components: {
		HomeView,
		AlarmView,
		MyMineView,
		CustomTabBar
	},
	data() {
		return {
			currentTab: 0
		}
	},
	onLoad(options) {
		// 如果有传入 tab 参数，则切换到对应 tab
		if (options.tab !== undefined) {
			this.currentTab = parseInt(options.tab)
		}
	},
	methods: {
		handleTabChange(index) {
			this.currentTab = index
			// 更新导航栏标题
			const titles = ['首页', '报警', '我的']
			uni.setNavigationBarTitle({
				title: titles[index]
			})
		}
	}
}
</script>

<style scoped>
.main-container {
	min-height: 100vh;
	background-color: var(--primary-bg);
	position: relative;
}

.content-container {
	min-height: 100vh;
	padding-bottom: 80px; /* 为 TabBar 留出空间 */
}
</style>

