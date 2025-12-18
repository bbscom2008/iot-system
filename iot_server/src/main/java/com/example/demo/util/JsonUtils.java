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


}
