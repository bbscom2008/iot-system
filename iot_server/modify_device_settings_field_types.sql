-- 修改 devices 表字段类型
-- 将气体上下限改为 INT 类型
-- 将温度、湿度上下限改为 DECIMAL(5,2) 类型

-- 修改气体上限字段类型
ALTER TABLE `devices` 
MODIFY COLUMN `gas_upper_limit` INT NULL DEFAULT NULL COMMENT '气体上限（ppm）';

-- 修改气体下限字段类型
ALTER TABLE `devices` 
MODIFY COLUMN `gas_lower_limit` INT NULL DEFAULT NULL COMMENT '气体下限（ppm）';

-- 修改温度上限字段类型
ALTER TABLE `devices` 
MODIFY COLUMN `temp_upper_limit` DECIMAL(5,2) NULL DEFAULT NULL COMMENT '温度上限（°C）';

-- 修改温度下限字段类型
ALTER TABLE `devices` 
MODIFY COLUMN `temp_lower_limit` DECIMAL(5,2) NULL DEFAULT NULL COMMENT '温度下限（°C）';

-- 修改湿度上限字段类型
ALTER TABLE `devices` 
MODIFY COLUMN `humidity_upper_limit` DECIMAL(5,2) NULL DEFAULT NULL COMMENT '湿度上限（%）';

-- 修改湿度下限字段类型
ALTER TABLE `devices` 
MODIFY COLUMN `humidity_lower_limit` DECIMAL(5,2) NULL DEFAULT NULL COMMENT '湿度下限（%）';

