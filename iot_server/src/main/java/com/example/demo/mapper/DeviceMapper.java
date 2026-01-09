package com.example.demo.mapper;

import com.example.demo.dto.DeviceListDTO;
import com.example.demo.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DeviceMapper {

    /**
     * 查询设备列表
     */
    List<Device> findList(Map<String, Object> params);

    /**
     * 查询设备列表（包含用户信息）
     */
    List<DeviceListDTO> findListWithUser(Map<String, Object> params);

    /**
     * 查询设备总数
     */
    Long countDevice(Map<String, Object> params);

    /**
     * 查询设备总数（包含用户信息筛选）
     */
    Long countDeviceWithUser(Map<String, Object> params);

    /**
     * 根据ID查询设备
     */
    Device findById(@Param("id") Long id);

    /**
     * 根据设备编号查询
     */
    Device findByDeviceNum(@Param("deviceNum") String deviceNum);

    /**
     * 绑定设备
     */
    int insert(Device device);

    /**
     * 解绑设备
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新设备信息
     */
    int update(Device device);

    /**
     * 统计用户所有设备数
     */
    Long countAllByUserId(@Param("userId") Long userId);

    /**
     * 统计用户在线设备数
     */
    Long countOnlineByUserId(@Param("userId") Long userId);

    /**
     * 统计用户报警设备数
     */
    Long countWarningByUserId(@Param("userId") Long userId);

    /**
     * 统计所有设备数（web端使用）
     */
    Long countAll();

    /**
     * 统计所有在线设备数（web端使用）
     */
    Long countOnline();

    /**
     * 统计所有报警设备数（web端使用）
     */
    Long countWarning();

    int updateDeviceOnlineState(@Param("deviceNum") String deviceNum, @Param("state") Integer state);

    /**
     * 更新设备状态,
     * @param deviceNum
     * @param onlineState
     * @param alarmState
     * @return
     */
    int updateDeviceState(@Param("deviceNum") String deviceNum, @Param("onlineState") Integer onlineState, @Param("warningStatus") Integer warningStatus);
}

