/**
 * Vuex MQTT 模块
 * 管理 MQTT 连接状态、消息数据、设备状态等
 */

import mqttClient from "../../utils/mqtt"
import { MQTT_TOPICS } from "../../utils/mqtt-config"
import device from "./device"

let mqttListenersBound = false

const state = {
  // 连接状态
  isConnected: false,
  isConnecting: false,
  reconnectAttempts: 0,
  lastError: null,

  // 实时消息数据
  deviceDataMap: {}, // {deviceId: {sensorCode, temperature, humidity, ...}}
  deviceStatusMap: {}, // {deviceId: {status, timestamp}}

  // 设备列表
  devices: [],

  // 消息队列（用于离线消息缓存）
  messageQueue: [],

  // 最后更新时间
  lastUpdateTime: null,

  // 订阅的主题列表
  subscriptions: [],
}

const mutations = {
  SET_CONNECTED(state, isConnected) {
    state.isConnected = isConnected
  },

  SET_CONNECTING(state, isConnecting) {
    state.isConnecting = isConnecting
  },

  SET_RECONNECT_ATTEMPTS(state, attempts) {
    state.reconnectAttempts = attempts
  },

  SET_LAST_ERROR(state, error) {
    state.lastError = error
  },

  // 更新设备数据
  // UPDATE_DEVICE_DATA(state, { deviceId, data }) {

  //   if (!state.deviceDataMap[deviceId]) {
  //     state.deviceDataMap[deviceId] = {};
  //   }
  //   state.deviceDataMap[deviceId] = {
  //     ...state.deviceDataMap[deviceId],
  //     ...data,
  //     timestamp: new Date().getTime(),
  //   };
  //   state.lastUpdateTime = new Date().getTime();
  // },

  // 更新设备状态
  UPDATE_DEVICE_STATUS(state, { deviceId, status }) {
    if (!state.deviceStatusMap[deviceId]) {
      state.deviceStatusMap[deviceId] = {}
    }
    state.deviceStatusMap[deviceId] = {
      ...state.deviceStatusMap[deviceId],
      ...status,
      timestamp: new Date().getTime(),
    }
    state.lastUpdateTime = new Date().getTime()
  },

  // 设置设备列表
  SET_DEVICES(state, devices) {
    state.devices = devices
  },

  // 添加设备
  ADD_DEVICE(state, device) {
    const exists = state.devices.find((d) => d.id === device.id)
    if (!exists) {
      state.devices.push(device)
    }
  },

  // 移除设备
  REMOVE_DEVICE(state, deviceId) {
    state.devices = state.devices.filter((d) => d.id !== deviceId)
    delete state.deviceDataMap[deviceId]
    delete state.deviceStatusMap[deviceId]
  },

  // 消息队列操作
  ADD_TO_QUEUE(state, message) {
    state.messageQueue.push({
      ...message,
      timestamp: new Date().getTime(),
    })
  },

  CLEAR_QUEUE(state) {
    state.messageQueue = []
  },

  REMOVE_FROM_QUEUE(state, index) {
    state.messageQueue.splice(index, 1)
  },

  // 订阅管理
  SET_SUBSCRIPTIONS(state, subscriptions) {
    state.subscriptions = subscriptions
  },

  CLEAR_ALL_DATA(state) {
    state.deviceDataMap = {}
    state.deviceStatusMap = {}
    state.messageQueue = []
    state.lastError = null
  },
}

const actions = {
  /**
   * 初始化 MQTT 连接
   */
  async initMqtt({ commit, dispatch, state }) {
    try {
      if (state.isConnected || state.isConnecting) {
        console.log("initMqtt isConnected || isConnecting, skipping init")
        return true
      }

      commit("SET_CONNECTING", true)
      commit("SET_LAST_ERROR", null)

      // 注册事件监听（仅绑定一次，避免重复回调）
      if (!mqttListenersBound) {
        mqttClient.on("connected", () => {
          commit("SET_CONNECTED", true)
          commit("SET_CONNECTING", false)
          commit("SET_RECONNECT_ATTEMPTS", 0)
          console.log("[Store] MQTT connected")
          // 连接成功后处理队列中的消息
          dispatch("processMessageQueue")
        })

        mqttClient.on("error", (error) => {
          commit("SET_LAST_ERROR", error.message)
          console.error("[Store] MQTT error:", error)
        })

        mqttClient.on("disconnected", () => {
          commit("SET_CONNECTED", false)
        })

        mqttClient.on("reconnecting", (attempts) => {
          commit("SET_RECONNECT_ATTEMPTS", attempts)
        })

        mqttClient.on("offline", () => {
          commit("SET_CONNECTED", false)
        })

        // 订阅消息事件
        mqttClient.on("message", ({ topic, message }) => {
          console.log("[Store] Received message:", topic, message)
          dispatch("handleMessage", { topic, message })
        })

        mqttListenersBound = true
      }

      // 连接到 MQTT
      await mqttClient.connect()

      // 订阅默认主题
      await dispatch("subscribeDefaultTopics")

      return true
    } catch (error) {
      commit("SET_CONNECTING", false)
      commit("SET_LAST_ERROR", error.message)
      console.error("[Store] Failed to initialize MQTT:", error)
      throw error
    }
  },

  /**
   * 订阅默认主题
   */
  async subscribeDefaultTopics({ commit }) {
    try {
      const topics = [
        MQTT_TOPICS.SYSTEM, // system/#
      ]

      await mqttClient.subscribe(topics, { qos: 1 })
      commit("SET_SUBSCRIPTIONS", mqttClient.getSubscriptions())
      console.log("[Store] Subscribed to default topics")
    } catch (error) {
      console.error("[Store] Failed to subscribe default topics:", error)
      throw error
    }
  },

  /**
   * 处理接收到的消息
   */
  async handleMessage({ commit, dispatch }, { topic, message }) {
    // 匹配设备数据主题：wxapi/{deviceNum}

    console.log("=====收到 mqtt 消息=======")
    console.log("Topic:", topic) //  wxapi/{deviceNum}   wxapi/464B21320F3936313536374D
    console.log("Message:", message) // { payload: "UPDATE_DEVICES", topic: "device/report/464B21320F3936313536374D" }

    if (topic.startsWith("wxapi/")) {
      const deviceNum = topic.split("/")[1]

      /**
       * 硬件设备发送数据到服务器的 topic
          常规设备上报 device/report/{STM32ID}
          变频详情设置上报 device/report/{STM32ID}/{imtx}  x是 1-2
          风机详情设置上报 device/report/{STM32ID}/{mtx}   x是 1-10
          报警上报 device/report/{STM32ID}/alarm  如： device/report/464B21320F3936313536374D/alarm
      */
      const deviceTopic = message.topic
      const parts = deviceTopic.split("/")
      if (
        parts.length === 3 &&
        parts[0] === "device" &&
        parts[1] === "report" &&
        parts[2] === deviceNum
      ) {
        // 常规设备上报 device/report/{STM32ID}
        console.log("处理常规更新: deviceNum=", deviceNum)
        uni.$emit("device/update", { deviceNum, message })
      } else if (
        parts.length === 4 &&
        parts[0] === "device" &&
        parts[1] === "report" &&
        parts[2] === deviceNum &&
        parts[3].startsWith("imt")
      ) {
        // 变频详情设置上报 device/report/{STM32ID}/{imtx}
        console.log("处理变频详情更新: deviceNum=", deviceNum)
        uni.$emit("device/frequencyMotorUpdate", { deviceNum, message })
      } else if (
        parts.length === 4 &&
        parts[0] === "device" &&
        parts[1] === "report" &&
        parts[2] === deviceNum &&
        parts[3].startsWith("mt")
      ) {
        // 风机详情设置上报 device/report/{STM32ID}/{mtx}
        console.log("处理风机详情设置上报: deviceNum=", deviceNum)
        uni.$emit("device/motorFanUpdate", { deviceNum, message })
      } else if (
        parts.length === 4 &&
        parts[0] === "device" &&
        parts[1] === "report" &&
        parts[2] === deviceNum &&
        parts[3] === "alarm"
      ) {
        // 报警上报 device/report/{STM32ID}/alarm  如： device/report/464B21320F3936313536374D/alarm
        console.log("处理报警更新: deviceNum=", deviceNum)
        uni.$emit("device/alarm", { deviceNum, message })
      }

      // 更新设备首页信息
      // dispatch(
      //   "device/updateDeviceHome",
      //   {
      //     id: deviceNum,
      //     message: message,
      //   },
      //   { root: true }
      // )

      // try {
      //   const data = typeof message === 'string' ? JSON.parse(message) : message;
      //   commit('UPDATE_DEVICE_DATA', {
      //     deviceId,
      //     data: {
      //       sensorCode: data.sensorCode || deviceId,
      //       temperature: data.temperature,
      //       humidity: data.humidity,
      //       pressure: data.pressure,
      //       status: data.status,
      //       // 其他设备数据字段
      //       ...data,
      //     },
      //   });
      // } catch (error) {
      //   console.error('[Store] Error parsing device data:', error);
      // }
    }

    // 匹配设备状态主题：device/{deviceId}/status
    // if (topic.startsWith('device/') && topic.endsWith('/status')) {
    //   const deviceId = topic.split('/')[1];
    //   try {
    //     const status = typeof message === 'string' ? JSON.parse(message) : message;
    //     commit('UPDATE_DEVICE_STATUS', {
    //       deviceId,
    //       status: {
    //         online: status.online,
    //         signal: status.signal,
    //         lastSeen: status.lastSeen,
    //         ...status,
    //       },
    //     });
    //   } catch (error) {
    //     console.error('[Store] Error parsing device status:', error);
    //   }
    // }

    // 系统消息
    if (topic.startsWith("system/")) {
      console.log("[Store] System message:", message)
      // 可以在这里处理系统消息，如同步指令等
    }
  },

  /**
   * 发布消息到设备
   */
  async publishMessage({ commit }, { deviceId, action, payload }) {
    try {
      const topic = `device/${deviceId}/control`
      const message = {
        action,
        payload,
        timestamp: new Date().getTime(),
      }

      if (!state.isConnected) {
        // 离线时加入队列
        commit("ADD_TO_QUEUE", {
          topic,
          message,
        })
        return { queued: true }
      }

      await mqttClient.publish(topic, message, { qos: 1 })
      return { sent: true }
    } catch (error) {
      console.error("[Store] Failed to publish message:", error)
      throw error
    }
  },

  /**
   * 处理消息队列（重连后）
   */
  async processMessageQueue({ state, commit }) {
    if (state.messageQueue.length === 0) {
      return
    }

    console.log(
      "[Store] Processing message queue, count:",
      state.messageQueue.length
    )
    const queue = [...state.messageQueue]

    for (let i = 0; i < queue.length; i++) {
      try {
        const { topic, message } = queue[i]
        await mqttClient.publish(topic, message, { qos: 1 })
        commit("REMOVE_FROM_QUEUE", 0)
      } catch (error) {
        console.error("[Store] Error processing queue message:", error)
        break // 停止处理，等待下次重连
      }
    }
  },

  /**
   * 订阅特定设备的数据
   */
  async subscribeDevice({ commit }, deviceId) {
    try {
      const idArray = Array.isArray(deviceId) ? deviceId : [deviceId]
      const topics = idArray.map((id) => {
        // 小程序不正直接订阅设备消息，只订阅 wxapi 主题，由后端转发
        return `wxapi/${id}`
      })
      // 如果 subscriptions 中没有该主题，则订阅
      const currentSubscriptions = state.subscriptions
      const newTopics = topics.filter(
        (topic) => !currentSubscriptions.includes(topic)
      )
      if (newTopics.length > 0) {
        await mqttClient.subscribe(newTopics, { qos: 2 })
        commit("SET_SUBSCRIPTIONS", mqttClient.getSubscriptions())
      }
    } catch (error) {
      console.error("[Store] Failed to subscribe device:", error)
      throw error
    }
  },

  /**
   * 取消订阅特定设备
   */
  async unsubscribeDevice({ commit }, deviceId) {
    try {
      const idArray = Array.isArray(deviceId) ? deviceId : [deviceId]
      const topics = idArray.map((id) => {
        return `device/${id}`
      })
      await mqttClient.unsubscribe(topics)
      commit("SET_SUBSCRIPTIONS", mqttClient.getSubscriptions())
    } catch (error) {
      console.error("[Store] Failed to unsubscribe device:", error)
      throw error
    }
  },

  /**
   * 断开 MQTT 连接
   */
  async disconnectMqtt({ commit }) {
    try {
      await mqttClient.disconnect()
      commit("SET_CONNECTED", false)
      commit("SET_CONNECTING", false)
      commit("CLEAR_ALL_DATA")
      console.log("[Store] MQTT disconnected")
    } catch (error) {
      console.error("[Store] Failed to disconnect MQTT:", error)
      throw error
    }
  },

  /**
   * 重新连接
   */
  async reconnect({ dispatch }) {
    try {
      await dispatch("disconnectMqtt")
      await dispatch("initMqtt")
    } catch (error) {
      console.error("[Store] Failed to reconnect:", error)
      throw error
    }
  },
}

const getters = {
  isConnected: (state) => state.isConnected,
  isConnecting: (state) => state.isConnecting,
  reconnectAttempts: (state) => state.reconnectAttempts,
  lastError: (state) => state.lastError,

  // 获取特定设备的最新数据
  getDeviceData: (state) => (deviceId) => state.deviceDataMap[deviceId] || {},

  // 获取特定设备的状态
  getDeviceStatus: (state) => (deviceId) =>
    state.deviceStatusMap[deviceId] || {},

  // 获取所有设备数据
  getAllDeviceData: (state) => state.deviceDataMap,
  getAllDeviceStatus: (state) => state.deviceStatusMap,

  // 获取设备列表
  getDevices: (state) => state.devices,
  getDeviceCount: (state) => state.devices.length,

  // 获取离线消息队列
  getMessageQueue: (state) => state.messageQueue,
  getQueueSize: (state) => state.messageQueue.length,

  // 获取最后更新时间
  getLastUpdateTime: (state) => state.lastUpdateTime,

  // 获取订阅列表
  getSubscriptions: (state) => state.subscriptions,
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
}
