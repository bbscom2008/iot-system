import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'

import createPersistedState from 'vuex-persistedstate';

import user from './modules/user'
import app from './modules/app'
import mqtt from './modules/mqtt'
import device from './modules/device'

Vue.use(Vuex)

const modules = {
  user,
  app,
  mqtt,
  device
}

const store = new Vuex.Store({
  modules,
  getters,
  plugins: [
    createPersistedState({
      // 持久化指定的模块路径
      // 例如：['user.userInfo', 'app.theme'] 表示只持久化 user 模块下的 userInfo 和 app 模块下的 theme
      paths: ['user', 'app', 'device'], // 持久化 user、app 和 device 模块（MQTT 状态不持久化）
    })
  ]
})

export default store
