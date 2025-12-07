-- 为motor_fan表添加控制模式相关字段
USE `iot_system`;

-- 添加基本控制字段
ALTER TABLE `motor_fan`
ADD COLUMN `control_mode` TINYINT NOT NULL DEFAULT 1 COMMENT '控制模式：1-温控 2-循环 3-湿控 4-气体 5-定时' AFTER `is_running`,
ADD COLUMN `auto_mode` TINYINT NOT NULL DEFAULT 1 COMMENT '自动模式：1-自动 2-开 3-关' AFTER `control_mode`,
ADD COLUMN `probe_sensor_id` BIGINT NULL COMMENT '探头传感器ID' AFTER `auto_mode`;

-- 添加温控/循环共用字段
ALTER TABLE `motor_fan`
ADD COLUMN `temp_upper` DECIMAL(5,2) NULL DEFAULT 38.00 COMMENT '温度上限/启动温度（°C）' AFTER `probe_sensor_id`,
ADD COLUMN `temp_lower` DECIMAL(5,2) NULL DEFAULT 30.00 COMMENT '温度下限/停止温度（°C）' AFTER `temp_upper`,
ADD COLUMN `run_time` INT NULL DEFAULT 5 COMMENT '运行时间/低温运行（秒）' AFTER `temp_lower`,
ADD COLUMN `pause_time` INT NULL DEFAULT 5 COMMENT '暂停时间/低温暂停（秒）' AFTER `run_time`;

-- 添加湿控字段
ALTER TABLE `motor_fan`
ADD COLUMN `humidity_upper` DECIMAL(5,2) NULL DEFAULT 75.00 COMMENT '湿度上限（%）' AFTER `pause_time`,
ADD COLUMN `humidity_lower` DECIMAL(5,2) NULL DEFAULT 45.00 COMMENT '湿度下限（%）' AFTER `humidity_upper`;

-- 添加气体字段
ALTER TABLE `motor_fan`
ADD COLUMN `gas_upper` INT NULL DEFAULT 3000 COMMENT '气体上限（ppm）' AFTER `humidity_lower`,
ADD COLUMN `gas_lower` INT NULL DEFAULT 1000 COMMENT '气体下限（ppm）' AFTER `gas_upper`;

-- 添加定时1字段
ALTER TABLE `motor_fan`
ADD COLUMN `timer1_enabled` TINYINT NULL DEFAULT 0 COMMENT '定时1开关：0-关闭 1-开启' AFTER `gas_lower`,
ADD COLUMN `timer1_start_time` VARCHAR(5) NULL COMMENT '定时1开启时间（HH:mm）' AFTER `timer1_enabled`,
ADD COLUMN `timer1_end_time` VARCHAR(5) NULL COMMENT '定时1关闭时间（HH:mm）' AFTER `timer1_start_time`,
ADD COLUMN `timer1_probe_sensor_id` BIGINT NULL COMMENT '定时1探头传感器ID' AFTER `timer1_end_time`,
ADD COLUMN `timer1_start_temp` DECIMAL(5,2) NULL DEFAULT 30.00 COMMENT '定时1启动温度（°C）' AFTER `timer1_probe_sensor_id`,
ADD COLUMN `timer1_stop_temp` DECIMAL(5,2) NULL DEFAULT 38.00 COMMENT '定时1停止温度（°C）' AFTER `timer1_start_temp`;

-- 添加定时2字段
ALTER TABLE `motor_fan`
ADD COLUMN `timer2_enabled` TINYINT NULL DEFAULT 0 COMMENT '定时2开关：0-关闭 1-开启' AFTER `timer1_stop_temp`,
ADD COLUMN `timer2_start_time` VARCHAR(5) NULL COMMENT '定时2开启时间（HH:mm）' AFTER `timer2_enabled`,
ADD COLUMN `timer2_end_time` VARCHAR(5) NULL COMMENT '定时2关闭时间（HH:mm）' AFTER `timer2_start_time`,
ADD COLUMN `timer2_probe_sensor_id` BIGINT NULL COMMENT '定时2探头传感器ID' AFTER `timer2_end_time`,
ADD COLUMN `timer2_start_temp` DECIMAL(5,2) NULL DEFAULT 30.00 COMMENT '定时2启动温度（°C）' AFTER `timer2_probe_sensor_id`,
ADD COLUMN `timer2_stop_temp` DECIMAL(5,2) NULL DEFAULT 38.00 COMMENT '定时2停止温度（°C）' AFTER `timer2_start_temp`;

-- 添加定时3字段
ALTER TABLE `motor_fan`
ADD COLUMN `timer3_enabled` TINYINT NULL DEFAULT 0 COMMENT '定时3开关：0-关闭 1-开启' AFTER `timer2_stop_temp`,
ADD COLUMN `timer3_start_time` VARCHAR(5) NULL COMMENT '定时3开启时间（HH:mm）' AFTER `timer3_enabled`,
ADD COLUMN `timer3_end_time` VARCHAR(5) NULL COMMENT '定时3关闭时间（HH:mm）' AFTER `timer3_start_time`,
ADD COLUMN `timer3_probe_sensor_id` BIGINT NULL COMMENT '定时3探头传感器ID' AFTER `timer3_end_time`,
ADD COLUMN `timer3_start_temp` DECIMAL(5,2) NULL DEFAULT 30.00 COMMENT '定时3启动温度（°C）' AFTER `timer3_probe_sensor_id`,
ADD COLUMN `timer3_stop_temp` DECIMAL(5,2) NULL DEFAULT 38.00 COMMENT '定时3停止温度（°C）' AFTER `timer3_start_temp`;
