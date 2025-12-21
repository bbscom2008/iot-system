package com.example.demo.mapper;

import com.example.demo.entity.FrequencyMotor;
import com.example.demo.util.JsonUtils;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FrequencyMotorMapper {

    /**
     * 根据父设备ID查询所有变频电机
     */
    List<FrequencyMotor> findByParentId(Long parentId);

    /**
     * 根据设备编号查询所有变频电机
     */
    List<FrequencyMotor> findByDeviceNum(String deviceNum);

    /**
     * 查询所有变频电机（关联设备和用户信息）
     */
    List<FrequencyMotor> findAll(Map<String, Object> params);

    /**
     * 根据ID查询变频电机
     */
    FrequencyMotor findById(Long id);

    /**
     * 新增变频电机
     */
    int insert(FrequencyMotor frequencyMotor);

    /**
     * 更新变频电机配置
     */
    int update(FrequencyMotor frequencyMotor);

    /**
     * 删除变频电机
     */
    int deleteById(Long id);

    /**
     * 删除父设备的所有变频电机
     */
    void deleteByParentId(Long parentId);

    /**
     * 根据父设备ID和设备编号更新变频电机的当前值
     */
    int updateValueByParentAndCode(Long parentId, String code, Integer value);
    
    /**
     * 根据父设备ID批量更新变频电机的当前值
     * @param params 包含parentId和valuesMap的参数
     * @return 更新的行数
     */
    int batchUpdateValueByParentId(
         @Param("deviceId") Long deviceId, 
        @Param("list") List<JsonUtils.KV<Integer>> list);
}