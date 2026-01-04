/**
 * Vuex Device 模块
 * 管理设备列表、设备统计等信息
 */

const state = {
  // 设备列表
  deviceList: [],
  
  // 设备统计信息
  deviceStats: {
    allDevice: 0,      // 总设备数
    lineDevice: 0,     // 在线设备数
    warningDevice: 0,  // 报警设备数
  },
  
  // 加载状态
  loading: false,
  
  // 错误信息
  error: null,
}

const mutations = {
  // 设置设备列表
  SET_DEVICE_LIST(state, deviceList) {
    state.deviceList = deviceList || []
  },

  // 设置设备统计
  SET_DEVICE_STATS(state, stats) {
    state.deviceStats = {
      ...state.deviceStats,
      ...stats
    }
  },

  // 设置加载状态
  SET_LOADING(state, loading) {
    state.loading = loading
  },

  // 设置错误信息
  SET_ERROR(state, error) {
    state.error = error
  },

  // 更新单个设备
  UPDATE_DEVICE(state, updatedDevice) {
    const index = state.deviceList.findIndex(d => d.id === updatedDevice.id)
    if (index !== -1) {
      state.deviceList.splice(index, 1, updatedDevice)
    }
  },

  // 清空设备列表
  CLEAR_DEVICE_LIST(state) {
    state.deviceList = []
  }
}

const actions = {
  // 获取设备列表
  async fetchDeviceList({ commit }, params = {}) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      // 注意：这里需要在 home.vue 中调用时传入 request 对象
      // 或者在此处导入 request
      // 这里暂时返回 Promise，由 home.vue 中处理 API 调用
      return {
        success: true
      }
    } catch (error) {
      commit('SET_ERROR', error.message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 设置设备列表数据
  setDeviceList({ commit }, deviceList) {
    commit('SET_DEVICE_LIST', deviceList)
  },

  // 获取设备统计
  async fetchDeviceStats({ commit }, params = {}) {
    try {
      // 由 home.vue 处理 API 调用，这里仅负责 mutation
      return {
        success: true
      }
    } catch (error) {
      commit('SET_ERROR', error.message)
      throw error
    }
  },

  // 设置设备统计数据
  setDeviceStats({ commit }, stats) {
    commit('SET_DEVICE_STATS', stats)
  },

  // 更新单个设备
  updateDevice({ commit }, device) {
    commit('UPDATE_DEVICE', device)
  },

  // 清空设备列表
  clearDeviceList({ commit }) {
    commit('CLEAR_DEVICE_LIST')
  }
}

const getters = {
  // 获取在线设备列表
  onlineDevices(state) {
    return state.deviceList.filter(device => device.deviceLineState === 1)
  },

  // 获取离线设备列表
  offlineDevices(state) {
    return state.deviceList.filter(device => device.deviceLineState !== 1)
  },

  // 获取报警设备列表
  warningDevices(state) {
    return state.deviceList.filter(device => device.warningStatus === true)
  },

  // 根据 ID 获取设备
  getDeviceById(state) {
    return (deviceId) => state.deviceList.find(device => device.id === deviceId)
  },

  // 根据设备编号获取设备
  getDeviceByNum(state) {
    return (deviceNum) => state.deviceList.find(device => device.deviceNum === deviceNum)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
