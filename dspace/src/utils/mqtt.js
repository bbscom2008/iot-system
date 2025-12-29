/**
 * MQTT 客户端服务
 * 实现连接、订阅、发布、断开等核心功能
 * 支持 H5 和微信小程序环境
 */

import mqtt from './mqtt.min.js';
import { getMqttConfig, MQTT_TOPICS } from './mqtt-config';

class MqttClient {
  constructor() {
    this.client = null;
    this.config = getMqttConfig();
    this.isConnected = false;
    this.isConnecting = false;
    this.subscriptions = new Map(); // 存储订阅的主题和回调
    this.pendingSubscriptions = []; // 存储在未连接时的待处理订阅
    this.messageHandlers = new Map(); // 消息处理器
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 10;
  }

  /**
   * 连接到 MQTT Broker
   * @returns {Promise}
   */
  async connect() {
    return new Promise((resolve, reject) => {
      if (this.isConnected) {
        console.warn('[MQTT] Already connected');
        resolve();
        return;
      }

      if (this.isConnecting) {
        console.warn('[MQTT] Connection in progress');
        reject(new Error('Connection in progress'));
        return;
      }

      this.isConnecting = true;

      try {
        const options = {
          clientId: this.config.clientId,
          username: this.config.username,
          password: this.config.password,
          clean: this.config.clean,
          keepalive: this.config.keepalive,
          reconnectPeriod: this.config.reconnectPeriod,
          connectTimeout: this.config.connectTimeout,
          // 协议版本设置
          protocolVersion: 4,
          // rejectUnauthorized: false, // 开发环境可以设置为 false
        };

        console.log('[MQTT] Connecting to:', this.config.brokerUrl);

        this.client = mqtt.connect(this.config.brokerUrl, options);

        // 连接成功
        this.client.on('connect', () => {
          this.isConnected = true;
          this.isConnecting = false;
          this.reconnectAttempts = 0;
          console.log('[MQTT] Connected successfully');

          // 重新订阅之前的主题
          this.resubscribeTopics();

          // 处理在连接前缓存的订阅
          this.processPendingSubscriptions();

          // 触发连接事件
          this.emit('connected');
          resolve();
        });

        // 连接失败
        this.client.on('error', (error) => {
          this.isConnecting = false;
          console.error('[MQTT] Connection error:', error);
          this.emit('error', error);
          reject(error);
        });

        // 连接关闭
        this.client.on('close', () => {
          this.isConnected = false;
          console.log('[MQTT] Connection closed');
          this.emit('disconnected');
        });

        // 重连
        this.client.on('reconnect', () => {
          this.reconnectAttempts++;
          console.log('[MQTT] Reconnecting...', this.reconnectAttempts);
          this.emit('reconnecting', this.reconnectAttempts);
        });

        // 接收消息
        this.client.on('message', (topic, message) => {
          this.handleMessage(topic, message);
        });

        // 订阅失败
        this.client.on('offline', () => {
          console.warn('[MQTT] Client went offline');
          this.isConnected = false;
          this.emit('offline');
        });
      } catch (error) {
        this.isConnecting = false;
        console.error('[MQTT] Connection failed:', error);
        reject(error);
      }
    });
  }

  /**
   * 订阅主题
   * @param {string|Array} topics - 主题或主题数组
   * @param {Object} options - 订阅选项，包含 qos 等
   * @param {Function} callback - 消息回调函数
   * @returns {Promise}
   */
  async subscribe(topics, options = {}, callback) {
    // 如果未连接：将订阅请求缓存，连接后处理并在实际订阅后 resolve
    if (!this.isConnected) {
      return new Promise((resolve, reject) => {
        this.pendingSubscriptions.push({ topics, options, callback, resolve, reject });
        console.log('[MQTT] Not connected — subscription cached for topics:', topics);
      });
    }

    return new Promise((resolve, reject) => {
      const topicArray = Array.isArray(topics) ? topics : [topics];
      const opts = {
        qos: options.qos || this.config.qos,
        ...options,
      };

      try {
        this.client.subscribe(topicArray, opts, (error, granted) => {
          if (error) {
            console.error('[MQTT] Subscribe error:', error);
            reject(error);
          } else {
            console.log('[MQTT] Subscribed to:', granted);

            // 存储订阅信息和回调
            topicArray.forEach((topic) => {
              this.subscriptions.set(topic, {
                qos: opts.qos,
                callback,
              });
            });

            this.emit('subscribed', topicArray);
            resolve(granted);
          }
        });
      } catch (error) {
        console.error('[MQTT] Subscribe failed:', error);
        reject(error);
      }
    });
  }

  /**
   * 处理在未连接期间缓存的订阅请求
   * @private
   */
  processPendingSubscriptions() {
    if (!this.client || !this.isConnected) return;
    if (this.pendingSubscriptions.length === 0) return;

    const pending = this.pendingSubscriptions.slice();
    this.pendingSubscriptions = [];

    pending.forEach((entry) => {
      const { topics, options = {}, callback, resolve, reject } = entry;
      const topicArray = Array.isArray(topics) ? topics : [topics];
      const opts = { qos: options.qos || this.config.qos, ...options };

      try {
        this.client.subscribe(topicArray, opts, (error, granted) => {
          if (error) {
            console.error('[MQTT] Pending subscribe error:', error);
            if (reject) reject(error);
          } else {
            console.log('[MQTT] Pending subscribed to:', granted);
            topicArray.forEach((topic) => {
              this.subscriptions.set(topic, { qos: opts.qos, callback });
            });
            this.emit('subscribed', topicArray);
            if (resolve) resolve(granted);
          }
        });
      } catch (error) {
        console.error('[MQTT] Pending subscribe failed:', error);
        if (reject) reject(error);
      }
    });
  }

  /**
   * 取消订阅
   * @param {string|Array} topics - 主题或主题数组
   * @returns {Promise}
   */
  async unsubscribe(topics) {
    return new Promise((resolve, reject) => {
      if (!this.isConnected) {
        reject(new Error('Not connected'));
        return;
      }

      const topicArray = Array.isArray(topics) ? topics : [topics];

      try {
        this.client.unsubscribe(topicArray, (error) => {
          if (error) {
            console.error('[MQTT] Unsubscribe error:', error);
            reject(error);
          } else {
            console.log('[MQTT] Unsubscribed from:', topicArray);

            // 移除订阅信息
            topicArray.forEach((topic) => {
              this.subscriptions.delete(topic);
            });

            this.emit('unsubscribed', topicArray);
            resolve();
          }
        });
      } catch (error) {
        console.error('[MQTT] Unsubscribe failed:', error);
        reject(error);
      }
    });
  }

  /**
   * 发布消息
   * @param {string} topic - 主题
   * @param {string|Object} message - 消息内容
   * @param {Object} options - 发布选项
   * @returns {Promise}
   */
  async publish(topic, message, options = {}) {
    return new Promise((resolve, reject) => {
      if (!this.isConnected) {
        reject(new Error('Not connected'));
        return;
      }

      const payload = typeof message === 'string' ? message : JSON.stringify(message);
      const opts = {
        qos: options.qos || this.config.qos,
        retain: options.retain || false,
        ...options,
      };

      try {
        this.client.publish(topic, payload, opts, (error) => {
          if (error) {
            console.error('[MQTT] Publish error:', error);
            reject(error);
          } else {
            console.log('[MQTT] Published to:', topic);
            this.emit('published', { topic, message: payload });
            resolve();
          }
        });
      } catch (error) {
        console.error('[MQTT] Publish failed:', error);
        reject(error);
      }
    });
  }

  /**
   * 处理接收到的消息
   * @private
   * @param {string} topic - 主题
   * @param {Buffer} message - 消息内容
   */
  handleMessage(topic, message) {
    try {
      const messageStr = message.toString();
      let parsedMessage;

      // 尝试解析为 JSON
      try {
        parsedMessage = JSON.parse(messageStr);
      } catch (e) {
        parsedMessage = messageStr;
      }

      console.log('[MQTT] Received message on', topic, ':', parsedMessage);

      // 调用特定主题的回调
      const subscription = this.subscriptions.get(topic);
      if (subscription && subscription.callback) {
        subscription.callback(parsedMessage);
      }

      // 调用通用消息处理器
      this.emit('message', { topic, message: parsedMessage });

      // 按主题模式匹配调用处理器
      const handlers = this.messageHandlers.get(topic) || [];
      handlers.forEach((handler) => {
        handler(parsedMessage);
      });
    } catch (error) {
      console.error('[MQTT] Error handling message:', error);
    }
  }

  /**
   * 重新订阅所有主题
   * @private
   */
  resubscribeTopics() {
    const topics = Array.from(this.subscriptions.keys());
    if (topics.length > 0) {
      console.log('[MQTT] Resubscribing to topics:', topics);
      const opts = { qos: this.config.qos };
      this.client.subscribe(topics, opts, (error) => {
        if (error) {
          console.error('[MQTT] Resubscribe error:', error);
        } else {
          console.log('[MQTT] Resubscribed successfully');
        }
      });
    }
  }

  /**
   * 断开连接
   * @returns {Promise}
   */
  async disconnect() {
    return new Promise((resolve) => {
      if (!this.client) {
        resolve();
        return;
      }

      this.client.end(true, () => {
        this.isConnected = false;
        this.client = null;
        this.subscriptions.clear();
        console.log('[MQTT] Disconnected');
        this.emit('disconnected');
        resolve();
      });
    });
  }

  /**
   * 注册事件监听器
   * @param {string} event - 事件名称
   * @param {Function} callback - 回调函数
   */
  on(event, callback) {
    if (!this.messageHandlers.has(event)) {
      this.messageHandlers.set(event, []);
    }
    this.messageHandlers.get(event).push(callback);
  }

  /**
   * 移除事件监听器
   * @param {string} event - 事件名称
   * @param {Function} callback - 回调函数
   */
  off(event, callback) {
    const handlers = this.messageHandlers.get(event);
    if (handlers) {
      const index = handlers.indexOf(callback);
      if (index > -1) {
        handlers.splice(index, 1);
      }
    }
  }

  /**
   * 触发事件
   * @private
   * @param {string} event - 事件名称
   * @param {*} data - 事件数据
   */
  emit(event, data) {
    const handlers = this.messageHandlers.get(event);
    if (handlers) {
      handlers.forEach((handler) => {
        try {
          handler(data);
        } catch (error) {
          console.error(`[MQTT] Event handler error for '${event}':`, error);
        }
      });
    }
  }

  /**
   * 获取连接状态
   * @returns {boolean}
   */
  getStatus() {
    return this.isConnected;
  }

  /**
   * 获取已订阅的主题列表
   * @returns {Array}
   */
  getSubscriptions() {
    return Array.from(this.subscriptions.keys());
  }
}

// 创建全局单例
const mqttClient = new MqttClient();

export default mqttClient;
