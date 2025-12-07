-- 将 sensors 表中的 device_num 字段重命名为 sensor_code
ALTER TABLE sensors CHANGE COLUMN device_num sensor_code VARCHAR(255) DEFAULT NULL COMMENT '传感器编号';

-- 为 sensor_code 添加唯一索引，确保传感器编号不重复
ALTER TABLE sensors ADD UNIQUE INDEX uk_sensor_code (sensor_code);

