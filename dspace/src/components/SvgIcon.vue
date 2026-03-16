<template>
  <view class="svg-icon" :class="iconClass" :style="containerStyle">
    <!-- H5：内联 SVG，支持动态颜色 -->
    <!-- #ifdef H5 -->
    <view class="icon-inner" v-html="processedSvg"></view>
    <!-- #endif -->

    <!-- 小程序 / App：使用 data URI 加载带颜色的 SVG -->
    <!-- #ifndef H5 -->
    <image class="icon-inner" :src="svgDataUri" mode="aspectFit"></image>
    <!-- #endif -->
  </view>
</template>

<script>
import { svgIcons } from '@/utils/svgIcons.js';

export default {
  name: 'SvgIcon',
  props: {
    name: {
      type: String,
      required: true,
    },
    size: {
      type: [String, Number],
      default: '24',
    },
    color: {
      type: String,
      default: '#4ea3ff',
    },
  },
  computed: {
    iconClass() {
      return `svg-icon-${this.name}`;
    },
    // 使用 CSS 字符串避免小程序中 style 对象序列化问题
    containerStyle() {
      const s = parseInt(this.size, 10) || 24;
      return `width:${s}px;height:${s}px;display:inline-block;line-height:0;vertical-align:middle;`;
    },
    // 替换 SVG 中的 currentColor 为实际颜色
    processedSvg() {
      const raw = svgIcons[this.name];
      if (!raw) return '';
      return raw
        .replace(/stroke="currentColor"/g, `stroke="${this.color}"`)
        .replace(/fill="currentColor"/g, `fill="${this.color}"`);
    },
    // 小程序使用 data URI，将颜色直接编码进 SVG
    svgDataUri() {
      const svg = this.processedSvg;
      if (!svg) {
        // 降级：使用静态文件（颜色不可变）
        return `/static/svg/${this.name}.svg`;
      }
      return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
    },
  },
};
</script>

<style scoped>
.svg-icon {
  display: inline-block;
  line-height: 0;
  vertical-align: middle;
}

/* #ifdef H5 */
.icon-inner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 穿透 scoped，作用于 v-html 内的 SVG 元素 */
.icon-inner >>> svg {
  width: 100%;
  height: 100%;
  display: block;
}
/* #endif */

/* #ifndef H5 */
.icon-inner {
  display: block;
  width: 100%;
  height: 100%;
}
/* #endif */
</style>