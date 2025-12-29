/**
 * MQTT 配置文件
 * 支持开发环境和生产环境的配置
 */

// MQTT Broker 地址配置
const MQTT_CONFIG = {
  // 开发环境配置
  dev: {
    broker: 'ws://192.168.56.128:8083/mqtt', // WebSocket 连接地址（H5 和小程序支持）
    brokerWx: 'ws://192.168.56.128:8083/mqtt', // TCP 连接地址
    clientId: 'dspace-' + Math.random().toString(36).substr(2, 9), // 客户端 ID
    username: '', // 用户名
    password: '', // 密码
    keepalive: 60, // 心跳间隔（秒）
    reconnectPeriod: 3000, // 重连间隔（毫秒）
    connectTimeout: 30000, // 连接超时（毫秒）
    clean: true, // 是否清除会话
    qos: 1, // 默认 QoS 等级 (0, 1, 2)
  },
  // 生产环境配置
  prod: {
    broker: 'ws://192.168.56.128:8083/mqtt', // WebSocket 连接地址（H5 和小程序支持）
    brokerWx: 'ws://192.168.56.128:8083/mqtt', // TCP 连接地址
    clientId: 'dspace-' + Math.random().toString(36).substr(2, 9), // 客户端 ID
    username: '', // 用户名
    password: '', // 密码
    keepalive: 60, // 心跳间隔（秒）
    reconnectPeriod: 3000, // 重连间隔（毫秒）
    connectTimeout: 30000, // 连接超时（毫秒）
    clean: true, // 是否清除会话
    qos: 1, // 默认 QoS 等级 (0, 1, 2)
  },
};

/**
 * 获取当前环境的 MQTT 配置
 * @returns {Object} MQTT 配置对象
 */
export function getMqttConfig() {
  const isDev = process.env.NODE_ENV === 'development';
  const config = isDev ? MQTT_CONFIG.dev : MQTT_CONFIG.prod;
  
  return {
    ...config,
    // 根据平台选择合适的连接方式
    brokerUrl: getbrokerUrl(),
  };
}

/**
 * 根据平台选择合适的 Broker URL
 * @returns {string} Broker URL
 */
export function getbrokerUrl() {
  const isDev = process.env.NODE_ENV === 'development';
  const config = isDev ? MQTT_CONFIG.dev : MQTT_CONFIG.prod;
  
  // 判断是否为小程序环境
  if (typeof wx !== 'undefined') {
    // 微信小程序环境 - 使用 TCP 协议
    return config.brokerWx;
  } 
  
  // H5 环境 - 使用 WebSocket 协议
  return config.broker;
}

/**
 * MQTT 主题配置
 */
export const MQTT_TOPICS = {
  // 设备数据上报
  DEVICE_DATA: 'device/+/data', // 设备数据主题：device/{deviceId}/data
  DEVICE_STATUS: 'device/+/status', // 设备状态主题：device/{deviceId}/status
  
  // 设备控制
  DEVICE_CONTROL: 'device/+/control', // 控制指令主题：device/{deviceId}/control
  DEVICE_CONFIG: 'device/+/config', // 配置主题：device/{deviceId}/config
  
  // 系统主题
  SYSTEM: 'system/#', // 系统消息
  HEARTBEAT: 'system/heartbeat', // 心跳
  SYNC: 'system/sync', // 同步
};

/**
 * QoS 等级定义
 */
export const QOS_LEVELS = {
  AT_MOST_ONCE: 0, // 最多一次（不保证送达）
  AT_LEAST_ONCE: 1, // 至少一次（保证至少送达一次）
  EXACTLY_ONCE: 2, // 恰好一次（保证恰好送达一次）
};

export default getMqttConfig;
