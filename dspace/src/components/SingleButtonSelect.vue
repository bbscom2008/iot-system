<template>
  <view class="single-button-select" :class="variantClass">
    <view
      v-for="option in options"
      :key="option.value"
      class="select-item"
      :class="{ active: isActive(option.value) }"
      @tap="onSelect(option.value)"
    >
      {{ option.label }}
    </view>
  </view>
</template>

<script>
export default {
  name: "SingleButtonSelect",
  props: {
    value: {
      type: [String, Number],
      required: true,
    },
    options: {
      type: Array,
      required: true,
      validator(val) {
        return Array.isArray(val) && val.length >= 2;
      },
    },
    variant: {
      type: String,
      default: "segmented", // segmented | chip
    },
    itemMinWidth: {
      type: String,
      default: "150rpx",
    },
    fontSize: {
      type: String,
      default: "26rpx",
    },
  },
  computed: {
    variantClass() {
      return this.variant === "chip" ? "chip" : "segmented";
    },
  },
  methods: {
    normalizeValue(val) {
      // 兼容小程序模板里 number 可能被当成 string 的情况
      if (val === null || val === undefined) return val;
      const str = String(val);
      if (/^-?\d+(\.\d+)?$/.test(str)) {
        return Number(str);
      }
      return val;
    },
    isActive(val) {
      return String(this.normalizeValue(this.value)) === String(this.normalizeValue(val));
    },
    onSelect(val) {
      const normalized = this.normalizeValue(val);
      this.$emit("input", normalized);
      this.$emit("change", normalized);
    },
  },
};
</script>

<style scoped>
.single-button-select {
  display: flex;
  gap: 0;
}

.select-item {
  min-width: 150rpx;
  width: 150rpx;
  height: 60rpx;
  line-height: 60rpx;
  text-align: center;
  color: #fff;
  font-size: 26rpx;
  /* background: #1a1a3a; */
  border: 2rpx solid var(--accent-color);
}

.select-item:first-child {
  border-radius: 30rpx 0 0 30rpx;
}

.select-item:last-child {
  border-radius: 0 30rpx 30rpx 0;
}

.select-item.active {
  background: var(--accent-color);
  border-color: var(--accent-color);
  font-weight: bold;
}

.single-button-select.chip {
  flex-wrap: wrap;
}

.single-button-select.chip .select-item {
  width: auto;
  min-width: 120rpx;
  font-size: 24rpx;
  padding: 0 18rpx;
}

.single-button-select.segmented {
  flex-wrap: nowrap;
}

.single-button-select.segmented .select-item {
  width: 150rpx;
  /* background: #1a1a3a; */
}
</style>
