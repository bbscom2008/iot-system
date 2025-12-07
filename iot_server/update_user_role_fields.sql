-- 修改 user_role 表字段名称
-- dict_label -> label
-- dict_value -> role_id
-- dict_sort -> sort

-- 1. 重命名字段
ALTER TABLE `user_role` 
CHANGE COLUMN `dict_label` `label` VARCHAR(50) NOT NULL COMMENT '角色名称',
CHANGE COLUMN `dict_value` `role_id` VARCHAR(10) NOT NULL COMMENT '角色ID',
CHANGE COLUMN `dict_sort` `sort` INT NOT NULL DEFAULT 0 COMMENT '排序';

-- 2. 删除旧的复合唯一索引
ALTER TABLE `user_role` 
DROP INDEX `uk_dict_value_platform`;

-- 3. 添加新的复合唯一索引
ALTER TABLE `user_role` 
ADD UNIQUE INDEX `uk_role_id_platform` (`role_id`, `platform`);
