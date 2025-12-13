package com.example.demo.mapper;

import com.example.demo.entity.MqttMessageRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MqttMessageRecordMapper {
    int insert(MqttMessageRecord record);
}

