<template>
	<view class="custom-tabbar">
		<view 
			class="tab-item" 
			:class="{ active: current === index }"
			v-for="(item, index) in tabList" 
			:key="index"
			@tap="switchTab(index)"
		>
			<view class="tab-icon">
				<SvgIcon 
					:name="item.icon" 
					:color="current === index ? selectedColor : color"
				/>
			</view>
			<view class="tab-text" :style="{ color: current === index ? selectedColor : color }">
				{{ item.text }}
			</view>
		</view>
	</view>
</template>

<script>
import SvgIcon from '@/components/SvgIcon.vue'

export default {
	name: 'CustomTabBar',
	components: {
		SvgIcon
	},
	props: {
		current: {
			type: Number,
			default: 0
		}
	},
	data() {
		return {
			color: '#CCCCCC',
			selectedColor: '#6A5ACD',
			tabList: [
				{
					text: '首页',
					icon: 'home'
				},
				{
					text: '报警',
					icon: 'alarm'
				},
				{
					text: '我的',
					icon: 'user'
				}
			]
		}
	},
	methods: {
		switchTab(index) {
			// 触发父组件的 change 事件
			this.$emit('change', index)
		}
	}
}
</script>

<style scoped>
.custom-tabbar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 60px;
	background-color: #04041A;
	border-top: 1px solid #333366;
	display: flex;
	align-items: center;
	justify-content: space-around;
	padding: 0 20px;
	z-index: 1000;
}

.tab-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	flex: 1;
	padding: 8px 0;
	cursor: pointer;
}

.tab-icon {
	margin-bottom: 4px;
}

.tab-text {
	font-size: 12px;
	transition: color 0.3s;
}

.tab-item.active .tab-text {
	font-weight: 500;
}
</style>
