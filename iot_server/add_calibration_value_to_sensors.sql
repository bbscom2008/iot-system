-- 为 sensors 表添加校正值字段
ALTER TABLE sensors ADD COLUMN adjust_value DOUBLE(6,2) DEFAULT 0.00 COMMENT '校正值（范围：-1024.00 到 1024.00）' AFTER sensor_value;

