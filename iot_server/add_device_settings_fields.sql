-- 为 devices 表添加设备设置相关字段
-- 用于适配 deviceSettings 页面

ALTER TABLE `devices` 
ADD COLUMN `level_time` INT NOT NULL DEFAULT 0 COMMENT '阶梯时间（秒），大于等于0的整数' AFTER `last_offline_time`,
ADD COLUMN `gas_upper_limit` INT NULL DEFAULT 0 COMMENT '气体上限（ppm）' AFTER `level_time`,
ADD COLUMN `gas_lower_limit` INT NULL DEFAULT 0 COMMENT '气体下限（ppm）' AFTER `gas_upper_limit`,
ADD COLUMN `temp_upper_limit` DECIMAL(5,2) NULL DEFAULT 0 COMMENT '温度上限（°C）' AFTER `gas_lower_limit`,
ADD COLUMN `temp_lower_limit` DECIMAL(5,2) NULL DEFAULT 0 COMMENT '温度下限（°C）' AFTER `temp_upper_limit`,
ADD COLUMN `humidity_upper_limit` DECIMAL(5,2) NULL DEFAULT 0 COMMENT '湿度上限（%）' AFTER `temp_lower_limit`,
ADD COLUMN `humidity_lower_limit` DECIMAL(5,2) NULL DEFAULT 0 COMMENT '湿度下限（%）' AFTER `humidity_upper_limit`,
ADD COLUMN `master_switch` TINYINT NOT NULL DEFAULT 1 COMMENT '总开关：0-关闭 1-开启' AFTER `humidity_lower_limit`,
ADD COLUMN `power_off_switch` TINYINT NOT NULL DEFAULT 1 COMMENT '断电开关：0-关闭 1-开启' AFTER `master_switch`,
ADD COLUMN `temp_switch` TINYINT NOT NULL DEFAULT 1 COMMENT '温度开关：0-关闭 1-开启' AFTER `power_off_switch`,
ADD COLUMN `humidity_switch` TINYINT NOT NULL DEFAULT 1 COMMENT '湿度开关：0-关闭 1-开启' AFTER `temp_switch`,
ADD COLUMN `gas_switch` TINYINT NOT NULL DEFAULT 1 COMMENT '气体开关：0-关闭 1-开启' AFTER `humidity_switch`,
ADD COLUMN `dialing_method` TINYINT NOT NULL DEFAULT 1 COMMENT '拨打方式：1-同时拨打 2-依次拨打' AFTER `gas_switch`,
ADD COLUMN `alarm_method` TINYINT NOT NULL DEFAULT 3 COMMENT '报警方式：1-打电话 2-发短信 3-打电话和发短信' AFTER `dialing_method`,
ADD COLUMN `alarm_phones` VARCHAR(256) NULL DEFAULT NULL COMMENT '报警电话 JSON字符串，最多10个电话' AFTER `alarm_method`;

