import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'

import createPersistedState from 'vuex-persistedstate';

import user from './modules/user'
import deviceDetail from './modules/deviceDetail'
import mqtt from './modules/mqtt'
import device from './modules/device'

Vue.use(Vuex)

const STORAGE_KEY = 'vuex'

function createStorage() {
  if (typeof uni !== 'undefined' && typeof uni.getStorageSync === 'function') {
    return {
      getItem(key) {
        const value = uni.getStorageSync(key)
        return value || null
      },
      setItem(key, value) {
        uni.setStorageSync(key, value)
      },
      removeItem(key) {
        uni.removeStorageSync(key)
      }
    }
  }

  if (typeof window !== 'undefined' && window.localStorage) {
    return window.localStorage
  }

  return {
    getItem() {
      return null
    },
    setItem() {},
    removeItem() {}
  }
}

const persistedStorage = createStorage()

const modules = {
  user,
  deviceDetail,
  mqtt,
  device
}

const store = new Vuex.Store({
  modules,
  getters,
  plugins: [
    createPersistedState({
      key: STORAGE_KEY,
      storage: persistedStorage,
      // 持久化指定的模块路径
      // 例如：['user.userInfo', 'app.theme'] 表示只持久化 user 模块下的 userInfo 和 app 模块下的 theme
      paths: ['user', 'deviceDetail', 'device'], // 持久化 user、app 和 device 模块（MQTT 状态不持久化）
    })
  ]
})

export default store
