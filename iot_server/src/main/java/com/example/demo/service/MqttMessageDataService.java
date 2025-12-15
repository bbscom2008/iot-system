package com.example.demo.service;

import com.example.demo.entity.MqttMessageData;
import com.example.demo.mapper.MqttMessageDataMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MqttMessageDataService {
    private final MqttMessageDataMapper mapper;

    public void save(String deviceNum, JsonNode node) {
        MqttMessageData d = new MqttMessageData();
        d.setDeviceNum(deviceNum);
        setDouble(d::setTs1, node.get("ts1"));
        setDouble(d::setTs2, node.get("ts2"));
        setDouble(d::setTs3, node.get("ts3"));
        setDouble(d::setTs4, node.get("ts4"));
        setInt(d::setMt1, node.get("mt1"));
        setInt(d::setMt2, node.get("mt2"));
        setInt(d::setMt3, node.get("mt3"));
        setInt(d::setMt4, node.get("mt4"));
        setInt(d::setMt5, node.get("mt5"));
        setInt(d::setMt6, node.get("mt6"));
        setInt(d::setMt7, node.get("mt7"));
        setInt(d::setMt8, node.get("mt8"));
        setInt(d::setMt9, node.get("mt9"));
        setInt(d::setMt10, node.get("mt10"));
        setInt(d::setImt1, node.get("imt1"));
        setInt(d::setImt2, node.get("imt2"));
        mapper.insert(d);
    }

    private void setDouble(java.util.function.Consumer<Double> setter, JsonNode v) {
        if (v != null && v.isNumber()) setter.accept(v.asDouble());
    }
    private void setInt(java.util.function.Consumer<Integer> setter, JsonNode v) {
        if (v == null) return;
        if (v.isBoolean()) setter.accept(v.booleanValue() ? 1 : 0);
        else if (v.isNumber()) setter.accept(v.intValue());
        else if (v.isTextual()) {
            String s = v.asText().trim();
            if ("true".equalsIgnoreCase(s)) setter.accept(1);
            else if ("false".equalsIgnoreCase(s)) setter.accept(0);
            else {
                try { setter.accept(Integer.parseInt(s)); } catch (Exception ignored) {}
            }
        }
    }
}

