package com.example.demo.mapper;

import com.example.demo.dto.MotorFanListDTO;
import com.example.demo.entity.MotorFan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface MotorFanMapper {

    /**
     * 根据父设备ID查询所有风扇
     */
    List<MotorFan> findByParentId(Long parentId);

    /**
     * 根据风机编码查询所有风扇
     */
    List<MotorFan> findByFanCode(String fanCode);

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

    int updateRunningStatusByParentAndCode(Long parentId, String fanCode, Integer isRunning);

    /**
     * 批量更新风机运行状态
     */
    int batchUpdateRunningStatusByParentId(Map<String, Object> params);

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
    int deleteByParentId(Long parentId);
}
