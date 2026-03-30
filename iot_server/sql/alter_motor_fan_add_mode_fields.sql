-- motor_fan 新增控制模式字段
ALTER TABLE `motor_fan`
  ADD COLUMN `tctcm` TINYINT(4) NULL DEFAULT 0 COMMENT '温控模式：0降温1升温' AFTER `pause_time`,
  ADD COLUMN `cccm` TINYINT(4) NULL DEFAULT 0 COMMENT '循环模式：0降温1升温2时间' AFTER `tctcm`,
  ADD COLUMN `hchcm` TINYINT(4) NULL DEFAULT 0 COMMENT '湿控模式：0除湿1加湿' AFTER `humidity_lower`,
  ADD COLUMN `tictitm` TINYINT(4) NULL DEFAULT 0 COMMENT '定时温控：0降温1升温' AFTER `timer3_stop_temp`;
