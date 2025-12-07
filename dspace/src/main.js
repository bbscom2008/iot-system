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
