
import request from '../../utils/request.js'

const state = {
  currentFrequencyMotor: null, // 当前选中的变频器信息
  // currentSensor: null, // 当前选中的传感器信息
  currentSensorId: null, // 当前选中的传感器ID
  currentMotorFan: null, // 当前选中的风机信息
  deviceInfo: null, // 设备信息
  currDevice: null,
}

const mutations = {
  SET_CURR_DEVICE(state, device) {
    state.currDevice = device
  },
  // 设置当前变频器
  SET_CURRENT_FREQUENCY_MOTOR(state, motor) {
    state.currentFrequencyMotor = motor
  },

  // 设置设备信息
  SET_DEVICE_INFO(state, deviceInfo) {
    state.deviceInfo = deviceInfo
  },

  // 更新设备字段
  UPDATE_DEVICE_FIELD(state, { field, value }) {
    if (state.deviceInfo) {
      state.deviceInfo[field] = value
    }
  },

  // 设置当前传感器
  SET_CURRENT_SENSOR(state, sensor) {
    // state.deviceInfo.sensors.find(
    //     (sensor) => sensor.id === state.currentSensorId
    // );
  },
  SET_CURRENT_SENSOR_ID(state, currSensorId) {
    state.currentSensorId = currSensorId
  },

  // 设置当前风机
  SET_CURRENT_MOTOR_FAN(state, motorFan) {
    state.currentMotorFan = motorFan
  },

  // 更新风机字段
  UPDATE_MOTOR_FAN_FIELD(state, { field, value }) {
    if (state.currentMotorFan) {
      state.currentMotorFan[field] = value
    }
  },

}

const actions = {
  // 获取设备详情并保存到仓库
  async fetchDeviceInfo({ rootState,commit, state }) {
    console.log('-----fetchDeviceInfo -----');
    
    if(!state.currDevice || rootState.device.currUpdateDeviceNum != state.currDevice.deviceNum){
      return 
    }

    try {
      const deviceId = state.currDevice.id
      const res = await request.get(`/device/detail/${deviceId}`)
      const deviceInfo = res || {}

      // 确保数组字段存在
      if (!deviceInfo.sensors) deviceInfo.sensors = []
      if (!deviceInfo.motorFans) deviceInfo.motorFans = []
      if (!deviceInfo.frequencyMotors) deviceInfo.frequencyMotors = []

      commit('SET_DEVICE_INFO', deviceInfo)
      return { success: true, deviceInfo }
    } catch (err) {
      throw err
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
