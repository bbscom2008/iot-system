import Vue from 'vue'
import App from './App'
import './uni.promisify.adaptor'

import store from './store'
import MySwitch from './components/MySwitch.vue'

Vue.config.productionTip = false

// 全局注册 MySwitch 组件
Vue.component('MySwitch', MySwitch)

App.mpType = 'app'

const app = new Vue({
  ...App,
  store
})
app.$mount()

// ======================== MQTT 初始化 ========================
/**
 * 应用启动时初始化 MQTT 连接
 * - 在生产环境下自动连接
 * - 处理连接失败和重连
 * - 支持 H5 和微信小程序环境
 */
function initializeMqtt() {
  try {
    // 延迟初始化，等待应用完全加载
    setTimeout(() => {
      console.log('[App] Initializing MQTT connection...')
      
      // 分发 Vuex action 来初始化 MQTT
      store.dispatch('mqtt/initMqtt')
        .then(() => {
          console.log('[App] MQTT initialized successfully')
          // 初始化成功后可以进行其他操作
          // 例如：更新 UI、加载设备列表等
        })
        .catch((error) => {
          console.error('[App] MQTT initialization failed:', error)
          // 可以在这里处理初始化失败的情况
          // 例如：显示错误提示、重试等
          
          // 自动重试：3 秒后重新尝试连接
          setTimeout(() => {
            console.log('[App] Retrying MQTT connection...')
            initializeMqtt()
          }, 3000)
        })
    }, 500)
  } catch (error) {
    console.error('[App] Failed to initialize MQTT:', error)
  }
}

// 仅在生产环境或开发时需要 MQTT 时初始化
// 可以根据需要调整这个条件
const shouldInitMqtt = true // 设置为 true 表示启用 MQTT
if (shouldInitMqtt) {
  initializeMqtt()
}

// 应用进入前台时重连
if (typeof uni !== 'undefined') {
  uni.onAppShow(() => {
    console.log('[App] App shown')
    if (!store.getters['mqtt/isConnected'] && !store.getters['mqtt/isConnecting']) {
      console.log('[App] Reconnecting MQTT...')
      store.dispatch('mqtt/initMqtt').catch((error) => {
        console.warn('[App] Reconnect failed:', error)
      })
    }
  })

  // 应用进入后台时断开连接（可选）
  // uni.onAppHide(() => {
  //   console.log('[App] App hidden')
  //   store.dispatch('mqtt/disconnectMqtt').catch((error) => {
  //     console.warn('[App] Disconnect failed:', error)
  //   })
  // })
}
