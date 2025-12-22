/**
 * MQTT 消息处理和解析器
 * 负责消息的解析、验证、转换和业务逻辑处理
 * 对标 iot_server 中的消息处理方式
 */

/**
 * 设备数据消息解析器
 * 对应后端的 MqttMessageData 数据模型
 */
export class DeviceDataParser {
  /**
   * 解析设备数据消息
   * @param {Object|string} message - 原始消息
   * @returns {Object} 解析后的数据
   */
  static parse(message) {
    try {
      const data = typeof message === 'string' ? JSON.parse(message) : message;

      return {
        // 传感器编码（设备唯一标识）
        sensorCode: data.sensorCode || data.deviceId || '',
        
        // 温度相关
        temperature: this.parseNumber(data.temperature),
        targetTemperature: this.parseNumber(data.targetTemperature || data.tempTarget),
        minTemperature: this.parseNumber(data.minTemperature || data.tempMin),
        maxTemperature: this.parseNumber(data.maxTemperature || data.tempMax),
        
        // 湿度相关
        humidity: this.parseNumber(data.humidity),
        targetHumidity: this.parseNumber(data.targetHumidity || data.humidityTarget),
        minHumidity: this.parseNumber(data.minHumidity || data.humidityMin),
        maxHumidity: this.parseNumber(data.maxHumidity || data.humidityMax),
        
        // 气压
        pressure: this.parseNumber(data.pressure),
        
        // 设备状态
        status: data.status || 0,
        isOnline: this.parseBoolean(data.isOnline !== undefined ? data.isOnline : true),
        
        // 控制状态（各个设备特定的控制位）
        controls: {
          // 示例：通风机状态
          fanEnabled: this.parseBoolean(data.fanEnabled || data.fan),
          // 加热器状态
          heaterEnabled: this.parseBoolean(data.heaterEnabled || data.heater),
          // 加湿器状态
          humidifierEnabled: this.parseBoolean(data.humidifierEnabled || data.humidifier),
          // 除湿器状态
          dehumidifierEnabled: this.parseBoolean(data.dehumidifierEnabled || data.dehumidifier),
          // 其他状态标志
          ...data.controls,
        },
        
        // 设备类型
        deviceType: data.deviceType || 'sensor',
        
        // 电池/信号强度
        signal: this.parseNumber(data.signal),
        battery: this.parseNumber(data.battery),
        
        // 时间戳
        timestamp: data.timestamp || Date.now(),
        
        // 原始数据（保留完整信息）
        rawData: data,
      };
    } catch (error) {
      console.error('[MessageParser] Error parsing device data:', error);
      throw error;
    }
  }

  /**
   * 验证设备数据的有效性
   * @param {Object} data - 解析后的数据
   * @returns {boolean}
   */
  static validate(data) {
    // 至少要有传感器编码
    if (!data.sensorCode) {
      console.warn('[MessageParser] Missing sensorCode');
      return false;
    }

    // 至少要有一个温湿度数据
    const hasValidData = 
      data.temperature !== undefined ||
      data.humidity !== undefined ||
      data.pressure !== undefined;

    if (!hasValidData) {
      console.warn('[MessageParser] No valid sensor data');
      return false;
    }

    return true;
  }

  /**
   * 转换为数据库格式
   * @param {Object} data - 解析后的数据
   * @returns {Object} 数据库格式的数据
   */
  static toDbFormat(data) {
    return {
      sensorCode: data.sensorCode,
      temperature: data.temperature,
      targetTemperature: data.targetTemperature,
      minTemperature: data.minTemperature,
      maxTemperature: data.maxTemperature,
      humidity: data.humidity,
      targetHumidity: data.targetHumidity,
      minHumidity: data.minHumidity,
      maxHumidity: data.maxHumidity,
      pressure: data.pressure,
      status: data.status,
      controls: JSON.stringify(data.controls), // JSON 序列化
      signal: data.signal,
      battery: data.battery,
      timestamp: new Date(data.timestamp),
    };
  }

  /**
   * 工具方法：解析数字
   */
  static parseNumber(value, defaultValue = null) {
    if (value === null || value === undefined) {
      return defaultValue;
    }
    const num = Number(value);
    return isNaN(num) ? defaultValue : num;
  }

  /**
   * 工具方法：解析布尔值
   */
  static parseBoolean(value) {
    if (typeof value === 'boolean') {
      return value;
    }
    if (typeof value === 'number') {
      return value !== 0;
    }
    if (typeof value === 'string') {
      return ['true', '1', 'on', 'yes'].includes(value.toLowerCase());
    }
    return Boolean(value);
  }
}

/**
 * 设备状态消息解析器
 */
export class DeviceStatusParser {
  /**
   * 解析设备状态消息
   * @param {Object|string} message - 原始消息
   * @returns {Object} 解析后的状态
   */
  static parse(message) {
    try {
      const data = typeof message === 'string' ? JSON.parse(message) : message;

      return {
        // 设备识别
        sensorCode: data.sensorCode || data.deviceId || '',
        
        // 连接状态
        isOnline: this.parseBoolean(data.isOnline !== undefined ? data.isOnline : true),
        lastSeen: data.lastSeen || Date.now(),
        
        // 信号强度
        signal: this.parseNumber(data.signal),
        
        // 电池电量
        battery: this.parseNumber(data.battery),
        
        // 版本信息
        firmwareVersion: data.firmwareVersion || '',
        hardwareVersion: data.hardwareVersion || '',
        
        // 运行状态
        uptime: this.parseNumber(data.uptime), // 运行时长（秒）
        errors: data.errors || [], // 错误代码列表
        
        // 内存/存储
        memoryUsage: this.parseNumber(data.memoryUsage), // %
        storageUsage: this.parseNumber(data.storageUsage), // %
        
        // 工作模式
        workMode: data.workMode || 'normal', // normal, eco, powerful
        
        // 时间戳
        timestamp: data.timestamp || Date.now(),
        
        // 原始数据
        rawData: data,
      };
    } catch (error) {
      console.error('[MessageParser] Error parsing device status:', error);
      throw error;
    }
  }

  /**
   * 验证设备状态的有效性
   */
  static validate(data) {
    return !!data.sensorCode;
  }

  /**
   * 工具方法
   */
  static parseNumber(value, defaultValue = null) {
    if (value === null || value === undefined) {
      return defaultValue;
    }
    const num = Number(value);
    return isNaN(num) ? defaultValue : num;
  }

  static parseBoolean(value) {
    if (typeof value === 'boolean') {
      return value;
    }
    if (typeof value === 'number') {
      return value !== 0;
    }
    if (typeof value === 'string') {
      return ['true', '1', 'on', 'yes'].includes(value.toLowerCase());
    }
    return Boolean(value);
  }
}

/**
 * 设备控制指令生成器
 * 用于生成发送给设备的控制指令
 */
export class DeviceControlCommand {
  /**
   * 创建温度设置指令
   */
  static createTemperatureControl(deviceId, targetTemp) {
    return {
      action: 'set_temperature',
      deviceId,
      payload: {
        targetTemperature: Number(targetTemp),
        timestamp: Date.now(),
      },
    };
  }

  /**
   * 创建湿度设置指令
   */
  static createHumidityControl(deviceId, targetHumidity) {
    return {
      action: 'set_humidity',
      deviceId,
      payload: {
        targetHumidity: Number(targetHumidity),
        timestamp: Date.now(),
      },
    };
  }

  /**
   * 创建开关控制指令
   */
  static createSwitchControl(deviceId, controlName, enabled) {
    return {
      action: 'switch_control',
      deviceId,
      payload: {
        control: controlName, // 'fan', 'heater', 'humidifier', etc.
        enabled: Boolean(enabled),
        timestamp: Date.now(),
      },
    };
  }

  /**
   * 创建设备重启指令
   */
  static createRebootCommand(deviceId) {
    return {
      action: 'reboot',
      deviceId,
      payload: {
        timestamp: Date.now(),
      },
    };
  }

  /**
   * 创建配置更新指令
   */
  static createConfigUpdate(deviceId, config) {
    return {
      action: 'update_config',
      deviceId,
      payload: {
        config,
        timestamp: Date.now(),
      },
    };
  }

  /**
   * 创建心跳/ping 指令
   */
  static createPingCommand(deviceId) {
    return {
      action: 'ping',
      deviceId,
      payload: {
        timestamp: Date.now(),
      },
    };
  }
}

/**
 * 系统消息处理器
 */
export class SystemMessageHandler {
  /**
   * 处理系统同步消息
   */
  static handleSync(message) {
    console.log('[SystemMessage] Sync:', message);
    // 触发应用重新同步所有设备数据
    return {
      type: 'sync_request',
      data: message,
    };
  }

  /**
   * 处理系统配置消息
   */
  static handleConfig(message) {
    console.log('[SystemMessage] Config:', message);
    return {
      type: 'config_update',
      data: message,
    };
  }

  /**
   * 处理系统心跳
   */
  static handleHeartbeat(message) {
    console.log('[SystemMessage] Heartbeat received');
    return {
      type: 'heartbeat',
      data: message,
      receivedAt: Date.now(),
    };
  }
}

/**
 * 消息处理的主要工厂类
 */
export class MessageProcessorFactory {
  /**
   * 处理设备数据消息
   */
  static processDeviceData(rawMessage) {
    const data = DeviceDataParser.parse(rawMessage);
    
    if (!DeviceDataParser.validate(data)) {
      throw new Error('Invalid device data');
    }

    return {
      type: 'device_data',
      data,
      dbFormat: DeviceDataParser.toDbFormat(data),
      timestamp: Date.now(),
    };
  }

  /**
   * 处理设备状态消息
   */
  static processDeviceStatus(rawMessage) {
    const status = DeviceStatusParser.parse(rawMessage);
    
    if (!DeviceStatusParser.validate(status)) {
      throw new Error('Invalid device status');
    }

    return {
      type: 'device_status',
      data: status,
      timestamp: Date.now(),
    };
  }

  /**
   * 根据主题智能处理消息
   */
  static processMessage(topic, rawMessage) {
    try {
      if (topic.includes('/data')) {
        return this.processDeviceData(rawMessage);
      } else if (topic.includes('/status')) {
        return this.processDeviceStatus(rawMessage);
      } else if (topic.includes('system/')) {
        return {
          type: 'system_message',
          topic,
          data: typeof rawMessage === 'string' ? JSON.parse(rawMessage) : rawMessage,
          timestamp: Date.now(),
        };
      }

      return {
        type: 'unknown',
        topic,
        data: rawMessage,
        timestamp: Date.now(),
      };
    } catch (error) {
      console.error('[MessageProcessor] Error processing message:', error);
      throw error;
    }
  }
}

export default MessageProcessorFactory;
