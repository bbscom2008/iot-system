package com.example.demo.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.dto.MotorFanListDTO;
import com.example.demo.entity.MotorFan;
import com.example.demo.util.JsonUtils;

@Mapper
public interface MotorFanMapper {

    /**
     * 根据父设备ID查询所有风扇
     */
    List<MotorFan> findByParentId(Long deviceId);

    /**
     * 根据风机编码查询所有风扇
     */
    List<MotorFan> findByFanCode(String deviceNum);

    /**
     * 查询所有风扇（关联设备和用户信息）
     */
    List<MotorFanListDTO> findAll(Map<String, Object> params);

    /**
     * 根据ID查询风扇
     */
    MotorFan findById(Long id);

    /**
     * 新增风扇
     */
    int insert(MotorFan motorFan);

    /**
     * 更新风扇运行状态
     */
    int updateRunningStatus(Long id, Integer isRunning);

    int updateRunningStatusByParentAndCode(@Param("deviceId") Long deviceId, @Param("deviceNum") String fanCode, @Param("isRunning") Integer isRunning);

    /**
     * 批量更新风机运行状态
     */
    int batchUpdateRunningStatusByParentId(
        @Param("deviceId") Long deviceId, 
        @Param("motorList") List<JsonUtils.KV<Integer>> motorList);

    /**
     * 更新风机配置
     */
    int update(MotorFan motorFan);

    /**
     * 删除风扇
     */
    int deleteById(Long id);

    /**
     * 删除父设备的所有风扇
     */
    int deleteByParentId(Long deviceId);
}
