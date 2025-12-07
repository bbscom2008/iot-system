package com.example.demo.mapper;

import com.example.demo.entity.SensorData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SensorDataMapper {

    /**
     * 批量插入传感器数据
     */
    int batchInsert(List<SensorData> sensorDataList);

    /**
     * 根据传感器ID查询历史数据
     */
    List<SensorData> findBySensorId(@Param("sensorId") Long sensorId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 根据设备ID查询所有传感器的历史数据
     */
    List<SensorData> findByDeviceId(@Param("deviceId") Long deviceId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的数据（用于数据清理）
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 获取指定传感器的最新记录
     */
    SensorData getLatestBySensorId(@Param("sensorId") Long sensorId);

    /**
     * 获取指定设备的所有传感器最新记录
     */
    List<SensorData> getLatestByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 删除设备的所有传感器历史数据
     */
    int deleteByDeviceId(@Param("deviceId") Long deviceId);
}

