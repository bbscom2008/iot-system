package com.example.demo.util;

import com.example.demo.dto.*;
import com.example.demo.entity.Device;
import com.example.demo.entity.FrequencyMotor;
import com.example.demo.entity.MotorFan;
import com.example.demo.entity.Sensor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO 转换工具类
 */
@Component
public class DtoConverter {
    
    /**
     * 通用实体转DTO方法
     * 
     * @param source 源实体对象
     * @param dtoClass DTO类型
     * @return DTO对象
     */
    public <T> T toDTO(Object source, Class<T> dtoClass) {
        if (source == null) return null;
        try {
            Constructor<T> constructor = dtoClass.getDeclaredConstructor();
            T dto = constructor.newInstance();
            BeanUtils.copyProperties(source, dto);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert entity to DTO: " + dtoClass.getName(), e);
        }
    }
    
    /**
     * 通用DTO转实体方法
     * 
     * @param source 源DTO对象
     * @param entityClass 实体类型
     * @return 实体对象
     */
    public <T> T toEntity(Object source, Class<T> entityClass) {
        if (source == null) return null;
        try {
            Constructor<T> constructor = entityClass.getDeclaredConstructor();
            T entity = constructor.newInstance();
            BeanUtils.copyProperties(source, entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DTO to entity: " + entityClass.getName(), e);
        }
    }
    
    /**
     * 通用列表转DTO列表方法
     * 
     * @param sources 源实体列表
     * @param dtoClass DTO类型
     * @return DTO列表
     */
    public <E, T> List<T> toDTOList(List<E> sources, Class<T> dtoClass) {
        if (sources == null) return null;
        return sources.stream()
                .map(source -> toDTO(source, dtoClass))
                .collect(Collectors.toList());
    }
    
    /**
     * Sensor 转 SensorDTO
     */
    public SensorDTO toSensorDTO(Sensor sensor) {
        return toDTO(sensor, SensorDTO.class);
    }
    
    /**
     * Sensor 列表转 SensorDTO 列表
     */
    public List<SensorDTO> toSensorDTOList(List<Sensor> sensors) {
        return toDTOList(sensors, SensorDTO.class);
    }
    
    /**
     * MotorFan 转 MotorFanDTO
     */
    public MotorFanDTO toMotorFanDTO(MotorFan motorFan) {
        return toDTO(motorFan, MotorFanDTO.class);
    }
    
    /**
     * MotorFan 列表转 MotorFanDTO 列表
     */
    public List<MotorFanDTO> toMotorFanDTOList(List<MotorFan> motorFans) {
        return toDTOList(motorFans, MotorFanDTO.class);
    }
    
    /**
     * FrequencyMotor 转 FrequencyMotorDTO
     */
    public FrequencyMotorDTO toFrequencyMotorDTO(FrequencyMotor frequencyMotor) {
        return toDTO(frequencyMotor, FrequencyMotorDTO.class);
    }
    
    /**
     * FrequencyMotor 列表转 FrequencyMotorDTO 列表
     */
    public List<FrequencyMotorDTO> toFrequencyMotorDTOList(List<FrequencyMotor> frequencyMotors) {
        return toDTOList(frequencyMotors, FrequencyMotorDTO.class);
    }
    
    /**
     * Device 转 DeviceListDTO
     */
    public DeviceListDTO toDeviceListDTO(Device device, List<Sensor> sensors) {
        if (device == null) return null;
        DeviceListDTO dto = toDTO(device, DeviceListDTO.class);
        if (dto != null) {
            dto.setSensors(toDTOList(sensors, SensorDTO.class));
        }
        return dto;
    }
    
    /**
     * Device 转 DeviceDetailDTO
     */
    public DeviceDetailDTO toDeviceDetailDTO(Device device, List<Sensor> sensors, 
                                            List<MotorFan> motorFans, 
                                            List<FrequencyMotor> frequencyMotors) {
        if (device == null) return null;
        DeviceDetailDTO dto = toDTO(device, DeviceDetailDTO.class);
        if (dto != null) {
            dto.setSensors(toDTOList(sensors, SensorDTO.class));
            dto.setMotorFans(toDTOList(motorFans, MotorFanDTO.class));
            dto.setFrequencyMotors(toDTOList(frequencyMotors, FrequencyMotorDTO.class));
        }
        return dto;
    }
}

