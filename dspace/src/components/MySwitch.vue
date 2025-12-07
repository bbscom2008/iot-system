<template>
  <view class="switch-toggle" :class="{ active: isActive }" @tap="handleToggle">
    <view class="switch-handle"></view>
  </view>
</template>

<script>
export default {
  name: "MySwitch",
  props: {
    value: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    isActive() {
      return this.value;
    },
  },
  methods: {
    handleToggle() {
      this.$emit("input", !this.value);
    },
  },
};
</script>

<style scoped>
.switch-toggle {
  width: 100rpx;
  height: 50rpx;
  background-color: var(--text-muted);
  border-radius: 25rpx;
  position: relative;
  transition: background-color 0.3s;
}

.switch-toggle.active {
  background-color: var(--accent-color);
}

.switch-handle {
  width: 40rpx;
  height: 40rpx;
  background-color: var(--text-primary);
  border-radius: 50%;
  position: absolute;
  top: 5rpx;
  left: 5rpx;
  transition: transform 0.3s;
}

.switch-toggle.active .switch-handle {
  transform: translateX(50rpx);
}
</style>

