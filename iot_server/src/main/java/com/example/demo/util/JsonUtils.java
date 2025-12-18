package com.example.demo.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KV<T> {
        public String key;
        public T value;
    }

    public static List<KV<Double>> convertJsonSensors(JsonNode node) {
        ArrayList<KV<Double>> list = new ArrayList<KV<Double>>();
        for (int i = 1; i <= 4; i++) {
            String key = "ts" + i;
            JsonNode v = node.get(key);
            if (v != null && v.isNumber()) {
                list.add(new KV<Double>(key, v.asDouble()));
            }
        }
        return list;
    }

    /**
     * 根据 mqtt 数居，解析出 所有的 motor
     * @param node
     * @return
     */
    public static List<KV<Integer>> convertJsonMotors(JsonNode node) {
        ArrayList<KV<Integer>> list = new ArrayList<KV<Integer>>();
        for (int i = 1; i <= 10; i++) {
            String key = "mt" + i;
            JsonNode v = node.get(key);
            if (v != null) {
                Integer run = null;
                if (v.isBoolean()) {
                    run = v.booleanValue() ? 1 : 0;
                } else if (v.isNumber()) {
                    run = v.intValue() != 0 ? 1 : 0;
                } else if (v.isTextual()) {
                    String s = v.asText().trim();
                    if ("true".equalsIgnoreCase(s) || "1".equals(s)) run = 1;
                    else if ("false".equalsIgnoreCase(s) || "0".equals(s)) run = 0;
                }
                if (run != null) {
                    list.add(new KV<Integer>(key, run));
                }
            }
        }
        return list;
    }



}
