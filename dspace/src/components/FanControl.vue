<template>
  <view class="fan-control">
    <view class="fan-icon-container" :class="{ 'active': isActive }">
      <SvgIcon name="fan" :size="24" :color="isActive ? '#00ff00' : '#6A5ACD'" />
    </view>
    <text class="fan-label" :style="{ color: isActive ? '#00ff00' : '#6A5ACD' }">{{ label }}</text>
  </view>
</template>

<script>
import SvgIcon from './SvgIcon.vue'

export default {
  name: 'FanControl',
  components: { SvgIcon },
  props: {
    // 风机标签文字
    label: {
      type: String,
      default: '风机'
    },
    // 是否启动
    isActive: {
      type: Boolean,
      default: false
    },
    // 设备ID
    deviceId: {
      type: [String, Number],
      required: true
    }
  }
}
</script>

<style scoped>
  .fan-control {
    display: flex;
    align-items: center;
    flex-direction: row;
    gap: 12rpx;
    height: 100%;
    /* padding: 15rpx; */
    /* height: 80rpx; */
    background-color: var(--card-bg);
    border-radius: 12rpx;
    cursor: pointer;
    transition: all 0.3s ease;
  }
  
  .fan-control:hover {
    background-color: rgba(106, 90, 205, 0.1);
  }
  
  .fan-icon-container {
    width: 40rpx;
    height: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    flex-shrink: 0;
  }
  
  .fan-icon-container.active {
    animation: rotate 2s linear infinite;
  }
  
  .fan-label {
    color: var(--text-primary);
    font-size: 26rpx;
  }

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
