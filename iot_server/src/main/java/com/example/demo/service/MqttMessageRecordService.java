package com.example.demo.service;

import com.example.demo.entity.MqttMessageRecord;
import com.example.demo.mapper.MqttMessageRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MqttMessageRecordService {
    private final MqttMessageRecordMapper mapper;

    public void save(String payload) {
        MqttMessageRecord record = new MqttMessageRecord();
        record.setPayload(payload);
        mapper.insert(record);
    }
}

