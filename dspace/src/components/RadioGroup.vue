<template>
  <view class="radio-group">
    <MyRadio
      v-for="option in options"
      :key="option.value"
      :value="option.value"
      :label="option.label"
      :checked="currentValue === option.value"
      @change="handleChange"
    />
  </view>
</template>

<script>
import MyRadio from "./Radio.vue";

export default {
  name: "RadioGroup",
  components: {
    MyRadio
  },
  props: {
    value: {
      type: [String, Number],
      required: true
    },
    options: {
      type: Array,
      required: true
    }
  },
  computed: {
    currentValue() {
      return this.value;
    }
  },
  methods: {
    handleChange(value) {
      console.log('-handleChange---', value);
      this.$emit('change', value);
    }
  }
};
</script>

<style scoped>
.radio-group {
  display: flex;
  flex-direction: row;
  gap: 15rpx;
}
</style>

