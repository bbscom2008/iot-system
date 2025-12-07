-- 创建传感器数据记录表
-- 用于存储每10秒采集一次的传感器历史数据

CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    sensor_id BIGINT NOT NULL COMMENT '传感器ID，关联sensor表的id',
    device_id BIGINT NOT NULL COMMENT '设备ID，关联device表的id',
    device_num VARCHAR(100) COMMENT '设备编号',
    sensor_num VARCHAR(100) COMMENT '传感器编号',
    sensor_type_id INT COMMENT '传感器类型ID：5-温度 6-湿度 7-气体',
    sensor_name VARCHAR(100) COMMENT '传感器名称',
    sensor_value DECIMAL(10, 2) COMMENT '传感器值',
    unit VARCHAR(20) COMMENT '单位：°C, %, ppm等',
    record_time DATETIME NOT NULL COMMENT '记录时间',
    
    INDEX idx_sensor_id (sensor_id),
    INDEX idx_device_id (device_id),
    INDEX idx_record_time (record_time),
    INDEX idx_device_record (device_id, record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传感器数据记录表';

