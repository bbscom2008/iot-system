<template>
  <view 
    class="svg-icon" 
    :class="iconClass" 
    :style="iconStyleObject"
  >
    <!-- H5 环境：直接使用 svg 标签 -->
    <view v-if="isH5" class="icon-content" v-html="svgContent"></view>
    
    <!-- 微信小程序环境：使用 image 标签加载 svg 文件 -->
    <image 
      v-else 
      class="icon-content" 
      :src="getImagePath" 
      mode="aspectFit"
      :style="imageStyleObject"
    ></image>
  </view>
</template>

<script>
export default {
  name: "SvgIcon",
  props: {
    name: {
      type: String,
      required: true,
    },
    size: {
      type: [String, Number],
      default: "24",
    },
    color: {
      type: String,
      default: "#4ea3ff",
    },
    fill: {
      type: String,
      default: "none",
    },
  },
  data() {
    return {
      svgContent: '',
      // 环境判断
      isH5: process.env.VUE_APP_PLATFORM === 'h5' || ['h5', 'web'].includes(process.env.VUE_APP_PLATFORM)
    }
  },
  computed: {
    iconClass() {
      return `svg-icon-${this.name}`;
    },
    // 修复：将样式对象转换为小程序可识别的字符串格式
    iconStyleObject() {
      // 基础样式
      const baseStyle = {
        width: this.size + 'px',
        height: this.size + 'px'
      };
      
      // H5 环境添加 flex 显示
      if (this.isH5) {
        baseStyle.display = 'inline-flex';
        baseStyle.color = this.color;
        baseStyle.alignItems = 'center';
        baseStyle.justifyContent = 'center';
      } else {
        // 小程序使用 inline-block
        baseStyle.display = 'inline-block';
      }
      
      return baseStyle;
    },
    // 小程序图片样式
    imageStyleObject() {
      return {
        width: '100%',
        height: '100%',
        display: 'block'
      };
    },
    // 小程序图片路径
    getImagePath() {
      return `/static/svg/${this.name}.svg`;
    }
  },
  created() {
    // 只在 H5 环境下处理 svg 内容
    if (this.isH5) {
      this.loadSvgContent();
    }
  },
  methods: {
    // 加载 svg 内容（H5 环境）
    loadSvgContent() {
      // 动态导入 SVG 文件
      const modules = import.meta.glob('@/static/svg/*.svg', { 
        eager: true,
        as: 'raw' 
      });
      
      const svgPath = `/src/static/svg/${this.name}.svg`;
      
      for (const path in modules) {
        if (path.includes(this.name)) {
          let svgRaw = modules[path];
          
          // 处理颜色替换
          if (this.color !== '#4ea3ff') {
            // 替换 stroke 属性
            svgRaw = svgRaw.replace(/stroke="[^"]*"/g, `stroke="${this.color}"`);
            // 替换 fill 属性（如果不是 none）
            if (this.fill !== 'none') {
              svgRaw = svgRaw.replace(/fill="[^"]*"/g, `fill="${this.fill}"`);
            }
          }
          
          this.svgContent = svgRaw;
          break;
        }
      }
    }
  }
};
</script>

<style scoped>
/* 基础样式 */
.svg-icon {
  display: inline-block;
  line-height: 0; /* 防止多余行高影响 */
}

/* H5 特定样式 - 只作用于 H5 环境 */
/* #ifdef H5 */
.svg-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: v-bind(color); /* Vue 3 特有，如果是 Vue 2 需要用其他方式 */
}

.icon-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-content svg {
  width: 100%;
  height: 100%;
}
/* #endif */

/* 小程序特定样式 - 只作用于微信小程序 */
/* #ifdef MP-WEIXIN */
.icon-content {
  width: 100%;
  height: 100%;
  display: block;
}

/* 确保 image 正确显示 */
image.icon-content {
  will-change: transform; /* 优化渲染性能 */
}
/* #endif */
</style>