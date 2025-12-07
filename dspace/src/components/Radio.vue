<template>
  <view class="radio-item" @tap="handleClick">
    <view class="radio-circle" :class="{ active: isChecked }"></view>
    <text>{{ label }}</text>
  </view>
</template>

<script>
export default {
  name: "Radio",
  props: {
    value: {
      type: [String, Number],
      required: true
    },
    label: {
      type: String,
      required: true
    },
    checked: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    isChecked() {
      return this.checked;
    }
  },
  methods: {
    handleClick() {
      this.$emit('change', this.value);
    }
  }
};
</script>

<style scoped>
.radio-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.radio-circle {
  width: 30rpx;
  height: 30rpx;
  border: 2rpx solid var(--text-primary);
  border-radius: 50%;
  position: relative;
}

.radio-circle.active {
  border-color: var(--accent-color);
}

.radio-circle.active::after {
  content: '';
  width: 16rpx;
  height: 16rpx;
  background-color: var(--accent-color);
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.radio-item text {
  color: var(--text-primary);
  font-size: 28rpx;
}
</style>

