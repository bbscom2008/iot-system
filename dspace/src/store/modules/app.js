
const state = {
  currentFrequencyMotor: null, // 当前选中的变频器信息
  currentSensor: null, // 当前选中的传感器信息
  currentMotorFan: null, // 当前选中的风机信息
  deviceInfo: null, // 设备信息
}

const mutations = {
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
    state.currentSensor = sensor
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
  
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
