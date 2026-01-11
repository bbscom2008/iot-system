package com.example.demo.mapper;

import com.example.demo.dto.SensorListDTO;
import com.example.demo.entity.Sensor;
import com.example.demo.util.JsonUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SensorMapper {

    /**
     * 根据设备ID查询所有传感器
     */
    List<Sensor> findByDeviceId(Long deviceId);

    /**
     * 根据设备编号查询所有传感器
     */
    List<Sensor> findByDeviceNum(String deviceNum);

    /**
     * 根据ID查询传感器
     */
    Sensor findById(Long id);

    /**
     * 新增传感器
     */
    int insert(Sensor sensor);

    /**
     * 更新传感器值
     */
    int updateValue(@Param("id") Long id, @Param("sensorValue") Double sensorValue);

    /**
     * 更新传感器信息
     */
    int update(Sensor sensor);

    /**
     * 删除传感器
     */
    int deleteById(Long id);

    /**
     * 删除设备的所有传感器
     */
    int deleteByDeviceId(Long deviceId);

    /**
     * 根据传感器类型查询最大设备编号
     */
    String findMaxDeviceNumByType(Integer sensorTypeId);

    /**
     * 查询所有传感器（用于定时任务批量记录数据）
     */
    List<Sensor> findAll();

    /**
     * 分页查询传感器列表（关联设备和用户信息）
     */
    List<SensorListDTO> findListWithDeviceAndUser(Map<String, Object> params);

    /**
     * 查询传感器总数（关联设备和用户信息）
     */
    Long countListWithDeviceAndUser(Map<String, Object> params);

    /**
     * 根据传感器编号查询传感器
     */
    Sensor findBySensorCode(@Param("parentId") Long parentId, @Param("sensorCode") String sensorCode);

    Sensor findSensorById(@Param("id") Long id);

    int updateValueByParentAndCode(@Param("parentId") Long parentId,
                                   @Param("sensorCode") String sensorCode,
                                   @Param("sensorValue") Double sensorValue);

    // 显式指定List的参数名为list，避免默认名歧义
    int batchUpdateValueByParentId(
            @Param("parentId") Long parentId,
            @Param("list") List<JsonUtils.KV<Double>> list
    );

}

