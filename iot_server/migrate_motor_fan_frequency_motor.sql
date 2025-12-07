-- 修改 motor_fan 表结构
-- 将 device_id 改为 parent_id，备注：父设备的ID
-- 将 device_num 改为 fan_code，备注：风机编码

USE `iot_system`;

-- 修改 motor_fan 表
-- 1. 重命名 device_id 为 parent_id
ALTER TABLE `motor_fan` 
CHANGE COLUMN `device_id` `parent_id` BIGINT NOT NULL COMMENT '父设备的ID';

-- 2. 重命名 device_num 为 fan_code
ALTER TABLE `motor_fan` 
CHANGE COLUMN `device_num` `fan_code` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '风机编码';

-- 3. 更新索引名称
ALTER TABLE `motor_fan` 
DROP INDEX `idx_device_id`,
ADD INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE;

ALTER TABLE `motor_fan` 
DROP INDEX `idx_device_num`,
ADD INDEX `idx_fan_code`(`fan_code` ASC) USING BTREE;

-- 修改 frequency_motor 表结构
-- 将 device_id 改为 parent_id，备注：父设备的ID
-- device_num 修改备注：变频器设备编码（不改字段名）

-- 1. 重命名 device_id 为 parent_id
ALTER TABLE `frequency_motor` 
CHANGE COLUMN `device_id` `parent_id` BIGINT NOT NULL COMMENT '父设备的ID';

-- 2. 修改 device_num 的备注
ALTER TABLE `frequency_motor` 
MODIFY COLUMN `device_num` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '变频器设备编码';

-- 3. 更新索引名称
ALTER TABLE `frequency_motor` 
DROP INDEX `idx_device_id`,
ADD INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE;

