package com.example.demo.mapper;

import com.example.demo.entity.DeviceWarning;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DeviceWarningMapper {

    /**
     * 新增报警记录
     */
    int insert(DeviceWarning warning);

    /**
     * 查询报警列表
     */
    List<DeviceWarning> findList(Map<String, Object> params);

    /**
     * 查询报警总数
     */
    Long countWarning(Map<String, Object> params);

    /**
     * 清理设备报警（原“标记已读”）
     */
    int deleteByDeviceNumAndUserId(@Param("deviceNum") String deviceNum, @Param("userId") Long userId);
}

