package com.example.demo.mapper;

import com.example.demo.entity.MqttMessageData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MqttMessageDataMapper {
    int insert(MqttMessageData data);
}

