package com.example.demo.service;

import com.example.demo.entity.Device;
import com.example.demo.entity.DeviceWarning;
import com.example.demo.entity.FrequencyMotor;
import com.example.demo.entity.MotorFan;
import com.example.demo.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.demo.entity.Sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService implements MqttCallback {

    /**
     * 温控仪数据上报 topic
     * 如 device/report/123123123
     */
    public static final String DEVICE_REPORT = "device/report/";

    /**
     * 服务器查询温控仪数据
     *
     * @param deviceNum
     * @return
     */
    public static String QUERY_DEVICE_STATUS(String deviceNum) {
        return "device/query-status/" + deviceNum;
    }

    /**
     * 服务器向温控仪发送控制指令 topic
     * 如 ： device/ctrl/123123123
     */
    public static String DEVICE_CTRL(String deviceNum) {
        return "device/ctrl/" + deviceNum;
    }

    /**
     * 服务器向温控仪下发工厂设置 topic
     * 如：server/setting/{STM32ID}/factoryset
     */
    public static String FACTORY_SET(String deviceNum) {
        return "server/setting/" + deviceNum + "/factoryset";
    }

    ;

    /**
     * 服务器向 前端 发送更新通知的 topic
     * 如 wxapi/d002
     */
    public static final String WX_CTRL = "wxapi/";

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.topic}")
    private String topic;

    @Value("${mqtt.clean-session:true}")
    private boolean cleanSession;

    @Value("${mqtt.connection-timeout:30}")
    private int connectionTimeout;

    @Value("${mqtt.keepalive-interval:60}")
    private int keepAliveInterval;

    private MqttClient client;
    private final ObjectMapper objectMapper;
    private final DeviceService deviceService;
    private final SensorService sensorService;
    private final MotorFanService motorFanService;
    private final FrequencyMotorService frequencyMotorService;
    private final DeviceWarningService deviceWarningService;
    private final MotorControlRuleEngineService motorControlRuleEngineService;

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            client.setCallback(this);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(cleanSession);
            options.setConnectionTimeout(connectionTimeout);
            options.setKeepAliveInterval(keepAliveInterval);
            if (StringUtils.hasText(username)) {
                options.setUserName(username);
            }
            if (StringUtils.hasText(password)) {
                options.setPassword(password.toCharArray());
            }
            client.connect(options);
            client.subscribe(topic, 1);
            log.info("MQTT subscribed: {}", topic);
        } catch (MqttException e) {
            log.error("MQTT error", e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            log.error("MQTT disconnect error", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        System.out.println("=====mqttmessage=====");
        System.out.println("topic : " + topic);
        System.out.println(payload);

        try {
            JsonNode node = parsePayloadNode(payload);

            // 常规设备上报 device/report/{STM32ID}
            if (isDeviceReportTopic(topic)) {
                handleDeviceReport(topic, node);
                return;
            }

            // 变频详情设置上报 device/report/{STM32ID}/{imtx}
            if (isFrequencyMotorDetailTopic(topic)) {
                handleFrequencyMotorDetailReport(topic, node);
                return;
            }

            // 风机详情设置上报 device/report/{STM32ID}/{mtx}
            if (isMotorFanDetailTopic(topic)) {
                handleMotorFanDetailReport(topic, node);
                return;
            }

            // 报警上报 device/report/{STM32ID}/alarm
            if (isDeviceAlarmTopic(topic)) {
                handleDeviceAlarmReport(topic, node);
                return;
            }

            // 工厂设置上报 device/report/{STM32ID}/factoryset
            if (isFactorySetTopic(topic)) {
                handleFactorySetReport(topic, node);
                return;
            }

        } catch (Exception e) {
            log.error("MQTT payload parse error", e);
        }
    }

    /**
     * 兼容解析设备上报：
     * 1) 标准 JSON；
     * 2) 非标准对象文本（如 key 未加双引号，按行 key:value）。
     */
    private JsonNode parsePayloadNode(String payload) throws JsonProcessingException {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException ex) {
            JsonNode looseNode = parseLooseObjectPayload(payload);
            if (looseNode != null) {
                return looseNode;
            }
            throw ex;
        }
    }

    /**
     * 解析类似：
     * {
     * wm:1
     * tcps:4
     * }
     */
    private JsonNode parseLooseObjectPayload(String payload) {
        if (!StringUtils.hasText(payload)) {
            return null;
        }
        String text = payload.trim();
        if (!text.startsWith("{") || !text.endsWith("}")) {
            return null;
        }

        String body = text.substring(1, text.length() - 1);
        String[] lines = body.split("\\r?\\n");
        ObjectNode root = objectMapper.createObjectNode();

        for (String line : lines) {
            if (!StringUtils.hasText(line)) {
                continue;
            }
            String item = line.trim();
            if (item.endsWith(",")) {
                item = item.substring(0, item.length() - 1).trim();
            }
            if (!StringUtils.hasText(item)) {
                continue;
            }

            int colonIndex = item.indexOf(':');
            if (colonIndex <= 0 || colonIndex >= item.length() - 1) {
                continue;
            }

            String key = item.substring(0, colonIndex).trim();
            String valueText = item.substring(colonIndex + 1).trim();

            if (!StringUtils.hasText(key)) {
                continue;
            }
            if (key.startsWith("\"") && key.endsWith("\"") && key.length() >= 2) {
                key = key.substring(1, key.length() - 1);
            }

            if (!StringUtils.hasText(valueText)) {
                root.set(key, NullNode.instance);
                continue;
            }

            JsonNode valueNode = parseLooseValue(valueText);
            root.set(key, valueNode);
        }

        return root.size() == 0 ? null : root;
    }

    private JsonNode parseLooseValue(String valueText) {
        String value = valueText.trim();
        if (value.endsWith(",")) {
            value = value.substring(0, value.length() - 1).trim();
        }

        if (!StringUtils.hasText(value) || "null".equalsIgnoreCase(value)) {
            return NullNode.instance;
        }

        // 字符串（单双引号）
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            String str = value.substring(1, value.length() - 1);
            return objectMapper.getNodeFactory().textNode(str);
        }

        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return objectMapper.getNodeFactory().booleanNode(Boolean.parseBoolean(value));
        }

        try {
            if (value.contains(".") || value.contains("e") || value.contains("E")) {
                return objectMapper.getNodeFactory().numberNode(Double.parseDouble(value));
            }
            return objectMapper.getNodeFactory().numberNode(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
        }

        // 尝试让 Jackson 处理数组/对象等复杂值
        try {
            return objectMapper.readTree(value);
        } catch (Exception ignored) {
        }

        // 最后回退为文本
        return objectMapper.getNodeFactory().textNode(value);
    }

    private boolean isMotorFanDetailTopic(String topic) {
        if (!StringUtils.hasText(topic)) {
            return false;
        }
        String[] parts = topic.split("/");
        return parts.length == 4
                && "device".equals(parts[0])
                && "report".equals(parts[1])
                && StringUtils.hasText(parts[2])
                && StringUtils.hasText(parts[3])
                && parts[3].toLowerCase().matches("^mt\\d+$");
    }

    private boolean isFrequencyMotorDetailTopic(String topic) {
        if (!StringUtils.hasText(topic)) {
            return false;
        }
        String[] parts = topic.split("/");
        return parts.length == 4
                && "device".equals(parts[0])
                && "report".equals(parts[1])
                && StringUtils.hasText(parts[2])
                && StringUtils.hasText(parts[3])
                && parts[3].toLowerCase().matches("^imt\\d+$");
    }

    private boolean isDeviceAlarmTopic(String topic) {
        if (!StringUtils.hasText(topic)) {
            return false;
        }
        String[] parts = topic.split("/");
        return parts.length == 4
                && "device".equals(parts[0])
                && "report".equals(parts[1])
                && StringUtils.hasText(parts[2])
                && "alarm".equalsIgnoreCase(parts[3]);
    }

    private boolean isFactorySetTopic(String topic) {
        if (!StringUtils.hasText(topic)) {
            return false;
        }
        String[] parts = topic.split("/");
        return parts.length == 4
                && "device".equals(parts[0])
                && "report".equals(parts[1])
                && StringUtils.hasText(parts[2])
                && "factoryset".equalsIgnoreCase(parts[3]);
    }

    /**
     * 判断是否是设备常规上报 topic，如 device/report/{STM32ID}
     * 
     * @param topic
     * @return
     */
    private boolean isDeviceReportTopic(String topic) {
        if (!StringUtils.hasText(topic)) {
            return false;
        }
        String[] parts = topic.split("/");
        return parts.length == 3
                && "device".equals(parts[0])
                && "report".equals(parts[1])
                && StringUtils.hasText(parts[2]);
    }

    private void handleDeviceReport(String topic, JsonNode node) {
        JsonNode idNode = node.get("STM32ID");
        String deviceNum = null;
        if (idNode != null && idNode.isTextual()) {
            deviceNum = idNode.asText();
        }
        // 兼容：如果载荷缺少 STM32ID，则从 topic device/report/{STM32ID} 解析
        if (!StringUtils.hasText(deviceNum) && topic != null && topic.startsWith(DEVICE_REPORT)) {
            deviceNum = topic.substring(DEVICE_REPORT.length());
        }

        if (!StringUtils.hasText(deviceNum)) {
            log.warn("设备上报忽略，缺少设备编号: topic={}, payload={}", topic, node.toString());
            return;
        }

        Device device = deviceService.findByDeviceNum(deviceNum);
        if (device == null) {
            log.warn("设备上报忽略，设备不存在: deviceNum={}, topic={}", deviceNum, topic);
            return;
        }

        String imei = node.hasNonNull("IMEI") ? node.get("IMEI").asText() : null;
        String iccid = node.hasNonNull("ICCID") ? node.get("ICCID").asText() : null;
        Integer signal = getInt(node, "signal");
        Integer power = getInt(node, "power");
        deviceService.updateDevice(deviceNum, imei, iccid, 1, signal, power);

        // 传感器的父ID，即当前设备的ID
        Long parentId = device.getId();

        // 批量更新传感器值，如果没有对应的传感器，就创建一个新的传感器
        List<JsonUtils.KV<Double>> sensorValues = JsonUtils.convertJsonSensors(node);
        sensorService.batchUpdateValueByParentId(parentId, sensorValues);

        // 批量更新电机运行状态
        List<JsonUtils.KV<Integer>> motorValues = JsonUtils.convertJsonMotors(node);
        motorFanService.batchUpdateRunningStatusByParentId(parentId, motorValues);

        // 批量更新变频电机的值
        // 处理 变频电机
        List<JsonUtils.KV<Integer>> freqMotorValues = JsonUtils.convertJsonIMotor(node);
        // 如果有需要更新的值，调用批量更新方法
        frequencyMotorService.batchUpdateValueByParentId(parentId, freqMotorValues);

        // 应用电机控制规则 - 基于自动模式和控制模式管理电机状态
        // 注释 下面这行，现在 服务器只接收和发送数据，不对数据进行逻辑处理
        // processMotorControlRules(device.getId(), device.getDeviceNum());

        // 数据已经更新，发消息给前端更新数据
        notifyToUpdate(deviceNum, topic);
    }

    private void handleDeviceAlarmReport(String topic, JsonNode node) {
        try {
            String[] parts = topic.split("/");
            String stm32Id = parts[2];
            if (!StringUtils.hasText(stm32Id)) {
                JsonNode idNode = node.get("STM32ID");
                if (idNode != null && idNode.isTextual()) {
                    stm32Id = idNode.asText();
                }
            }

            if (!StringUtils.hasText(stm32Id)) {
                log.warn("报警上报忽略，缺少设备编号: topic={}", topic);
                return;
            }

            Device device = deviceService.findByDeviceNum(stm32Id);
            if (device == null) {
                log.warn("报警上报忽略，设备不存在: stm32Id={}, topic={}", stm32Id, topic);
                return;
            }

            DeviceWarning warning = new DeviceWarning();
            warning.setDeviceId(device.getId());
            warning.setDeviceNum(device.getDeviceNum());
            warning.setUserId(device.getUserId());

            warning.setTa1(getInt(node, "ta1"));
            warning.setTs1(getScaledDecimal(node, "ts1"));
            warning.setTa2(getInt(node, "ta2"));
            warning.setTs2(getScaledDecimal(node, "ts2"));
            warning.setTa3(getInt(node, "ta3"));
            warning.setTs3(getScaledDecimal(node, "ts3"));
            warning.setTa4(getInt(node, "ta4"));
            warning.setTs4(getScaledDecimal(node, "ts4"));
            warning.setHa(getInt(node, "ha"));
            warning.setHv(getScaledDecimal(node, "hv"));
            warning.setNa(getInt(node, "na"));
            warning.setNv(getDouble(node, "nv"));

            deviceWarningService.saveWarning(warning);
            notifyToUpdate(stm32Id, topic);

            log.info("报警上报已保存: stm32Id={}, deviceId={}", stm32Id, device.getId());
        } catch (Exception e) {
            log.error("处理报警上报失败: topic={}", topic, e);
        }
    }

    /**
     * 工厂设置上报 device/report/{STM32ID}/factoryset
     */
    private void handleFactorySetReport(String topic, JsonNode node) {
        try {
            String[] parts = topic.split("/");
            String deviceNum = parts[2];

            if (!StringUtils.hasText(deviceNum)) {
                log.warn("工厂设置上报忽略，缺少设备编号: topic={}", topic);
                return;
            }

            Device device = deviceService.findByDeviceNum(deviceNum);
            if (device == null) {
                log.warn("工厂设置上报忽略，设备不存在: deviceNum={}, topic={}", deviceNum, topic);
                return;
            }

            Device update = new Device();
            update.setTempUpperLimit(getScaledDecimal(node, "taul"));
            update.setTempLowerLimit(getScaledDecimal(node, "tadl"));
            update.setHumidityUpperLimit(getScaledDecimal(node, "haul"));
            update.setHumidityLowerLimit(getScaledDecimal(node, "hadl"));
            update.setGasUpperLimit(getInt(node, "naul"));
            update.setGasLowerLimit(getInt(node, "nadl"));
            update.setLevelTime(getInt(node, "lt"));

            update.setTof1(getInt(node, "tof1"));
            update.setTof2(getInt(node, "tof2"));
            update.setTof3(getInt(node, "tof3"));
            update.setTof4(getInt(node, "tof4"));

            update.setHr(getInt(node, "hr"));
            update.setNr(getInt(node, "nr"));
            update.setTb(getInt(node, "tb"));
            update.setHb(getInt(node, "hb"));
            update.setNb(getInt(node, "nb"));

            deviceService.updateDeviceSettings(device.getId(), device.getUserId(), "web", update);
            notifyToUpdate(deviceNum, topic);

            log.info("工厂设置上报已更新: deviceNum={}, deviceId={}", deviceNum, device.getId());
        } catch (Exception e) {
            log.error("处理工厂设置上报失败: topic={}", topic, e);
        }
    }

    /**
     * 变频详情设置上报 device/report/{STM32ID}/{imtx}
     * 
     * @param topic
     * @param node
     */
    private void handleFrequencyMotorDetailReport(String topic, JsonNode node) {
        try {
            String[] parts = topic.split("/");
            String stm32Id = parts[2];
            String imtx = parts[3];

            Device device = deviceService.findByDeviceNum(stm32Id);
            if (device == null) {
                log.warn("变频详情上报忽略，设备不存在: stm32Id={}, topic={}", stm32Id, topic);
                return;
            }

            List<FrequencyMotor> motors = frequencyMotorService.findByParentId(device.getId());
            FrequencyMotor frequencyMotor = null;
            if (motors != null) {
                for (FrequencyMotor item : motors) {
                    if (item != null && StringUtils.hasText(item.getDeviceNum())
                            && item.getDeviceNum().equalsIgnoreCase(imtx)) {
                        frequencyMotor = item;
                        break;
                    }
                }
            }

            if (frequencyMotor == null) {
                log.warn("变频详情上报忽略，变频器不存在: stm32Id={}, imtx={}", stm32Id, imtx);
                return;
            }

            FrequencyMotor update = new FrequencyMotor();
            update.setId(frequencyMotor.getId());

            update.setFcm(getInt(node, "fcm"));

            // fcm : 变频模式选择：0手动，1自动温控，2自动湿控，3自动氨气

            switch (update.getFcm()) {
                case 0:
                    // 手动模式
                    update.setMs(getDouble(node, "ms"));
                    update.setMrtm(getInt(node, "mrtm"));
                    update.setMrts(getInt(node, "mrts"));
                    update.setMptm(getInt(node, "mptm"));
                    update.setMpts(getInt(node, "mpts"));
                    break;
                case 1:
                    // 自动温控
                    update.setAtps(getInt(node, "atps"));
                    update.setAtls(getDouble(node, "atls"));
                    update.setAtul(getScaledDecimal(node, "atul"));
                    update.setAtdl(getScaledDecimal(node, "atdl"));
                    update.setAswt(getScaledDecimal(node, "aswt"));
                    update.setAtrtm(getInt(node, "atrtm"));
                    update.setAtrts(getInt(node, "atrts"));
                    update.setAtptm(getInt(node, "atptm"));
                    update.setAtpts(getInt(node, "atpts"));
                    break;
                case 2:
                    // 自动湿控
                    update.setAhls(getDouble(node, "ahls"));
                    update.setAhul(getScaledDecimal(node, "ahul"));
                    update.setAhdl(getScaledDecimal(node, "ahdl"));
                    update.setAhrtm(getInt(node, "ahrtm"));
                    update.setAhrts(getInt(node, "ahrts"));
                    update.setAhptm(getInt(node, "ahptm"));
                    update.setAhpts(getInt(node, "ahpts"));
                    break;
                case 3:
                    // 自动氨气
                    update.setAnls(getDouble(node, "anls"));
                    update.setAnul(getDouble(node, "anul"));
                    update.setAndl(getDouble(node, "andl"));
                    update.setAnrtm(getInt(node, "anrtm"));
                    update.setAnrts(getInt(node, "anrts"));
                    update.setAnptm(getInt(node, "anptm"));
                    update.setAnpts(getInt(node, "anpts"));
                    break;
                default:
                    break;
            }

            frequencyMotorService.update(update);
            notifyToUpdate(stm32Id, topic);
            log.info("变频详情上报已更新: stm32Id={}, imtx={}, frequencyMotorId={}", stm32Id, imtx, frequencyMotor.getId());
        } catch (Exception e) {
            log.error("处理变频详情上报失败: topic={}", topic, e);
        }
    }

    /**
     * 风机详情设置上报 device/report/{STM32ID}/{mtx}
     */
    private void handleMotorFanDetailReport(String topic, JsonNode node) {
        try {
            String[] parts = topic.split("/");
            String stm32Id = parts[2];
            String motorNum = parts[3];

            Device device = deviceService.findByDeviceNum(stm32Id);
            if (device == null) {
                log.warn("风机详情上报忽略，设备不存在: stm32Id={}, topic={}", stm32Id, topic);
                return;
            }

            MotorFan motorFan = motorFanService.findByDeviceIdAndMotorNum(device.getId(), motorNum);
            if (motorFan == null) {
                log.warn("风机详情上报忽略，风机不存在: stm32Id={}, motorNum={}", stm32Id, motorNum);
                return;
            }

            MotorFan update = new MotorFan();
            update.setId(motorFan.getId());
            update.setWm(getInt(node, "wm"));

            switch (update.getWm()) {
                case 0: // 温控
                    update.setTcps(getInt(node, "tcps"));
                    update.setTcat(getScaledDecimal(node, "tcat"));
                    update.setTcot(getScaledDecimal(node, "tcot"));
                    update.setTcltrm(getInt(node, "tcltrm"));
                    update.setTcltrs(getInt(node, "tcltrs"));
                    update.setTcltpm(getInt(node, "tcltpm"));
                    update.setTcltps(getInt(node, "tcltps"));
                    update.setTctcm(getInt(node, "tctcm"));
                    break;
                case 1: // 循环
                    update.setCcps(getInt(node, "ccps"));
                    update.setCctu(getScaledDecimal(node, "cctu"));
                    update.setCctd(getScaledDecimal(node, "cctd"));
                    update.setCcrm(getInt(node, "ccrm"));
                    update.setCcrs(getInt(node, "ccrs"));
                    update.setCcpm(getInt(node, "ccpm"));
                    update.setCcpss(getInt(node, "ccpss"));
                    update.setCccm(getInt(node, "cccm"));
                    break;
                case 2: // 湿控
                    update.setHchu(getScaledDecimal(node, "hchu"));
                    update.setHchd(getScaledDecimal(node, "hchd"));
                    update.setHcrm(getInt(node, "hcrm"));
                    update.setHcrs(getInt(node, "hcrs"));
                    update.setHcpm(getInt(node, "hcpm"));
                    update.setHcps(getInt(node, "hcps"));
                    update.setHchcm(getInt(node, "hchcm"));
                    break;
                case 3: // 氨气
                    update.setNcnu(getInt(node, "ncnu"));
                    update.setNcnd(getInt(node, "ncnd"));
                    update.setNcrm(getInt(node, "ncrm"));
                    update.setNcrs(getInt(node, "ncrs"));
                    update.setNcpm(getInt(node, "ncpm"));
                    update.setNcps(getInt(node, "ncps"));
                    break;
                case 4: // 定时
                    update.setTict1nf(getInt(node, "tict1nf"));
                    update.setTict1nh(getInt(node, "tict1nh"));
                    update.setTict1nm(getInt(node, "tict1nm"));
                    update.setTict1fh(getInt(node, "tict1fh"));
                    update.setTict1fm(getInt(node, "tict1fm"));

                    update.setTict2nf(getInt(node, "tict2nf"));
                    update.setTict2nh(getInt(node, "tict2nh"));
                    update.setTict2nm(getInt(node, "tict2nm"));
                    update.setTict2fh(getInt(node, "tict2fh"));
                    update.setTict2fm(getInt(node, "tict2fm"));

                    update.setTict3nf(getInt(node, "tict3nf"));
                    update.setTict3nh(getInt(node, "tict3nh"));
                    update.setTict3nm(getInt(node, "tict3nm"));
                    update.setTict3fh(getInt(node, "tict3fh"));
                    update.setTict3fm(getInt(node, "tict3fm"));

                    update.setTicps(getInt(node, "ticps"));
                    update.setTicat(getScaledDecimal(node, "ticat"));
                    update.setTicot(getScaledDecimal(node, "ticot"));
                    update.setTictitm(getInt(node, "tictitm"));
                    break;
                default:
                    break;
            }

            motorFanService.update(update);
            notifyToUpdate(stm32Id, topic);
            log.info("风机详情上报已更新: stm32Id={}, motorNum={}, motorFanId={}", stm32Id, motorNum, motorFan.getId());
        } catch (Exception e) {
            log.error("处理风机详情上报失败: topic={}", topic, e);
        }
    }

    private Integer getInt(JsonNode node, String key) {
        JsonNode value = node.get(key);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isInt() || value.isLong()) {
            return value.asInt();
        }
        if (value.isNumber()) {
            return (int) Math.round(value.asDouble());
        }
        if (value.isTextual() && StringUtils.hasText(value.asText())) {
            try {
                return Integer.parseInt(value.asText().trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Integer firstInt(JsonNode node, String... keys) {
        for (String key : keys) {
            Integer value = getInt(node, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Double getDouble(JsonNode node, String key) {
        JsonNode value = node.get(key);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asDouble();
        }
        if (value.isTextual() && StringUtils.hasText(value.asText())) {
            try {
                return Double.parseDouble(value.asText().trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Double getScaledDecimal(JsonNode node, String key) {
        Double value = getDouble(node, key);
        if (value == null) {
            return null;
        }
        return value / 10D;
    }

    /**
     * 通知前端 更新页面
     *
     * @param deviceNum
     */
    public void notifyToUpdate(String deviceNum, String topic) {
        try {
            log.info("发送更新通知: deviceNum={}, topic={}", deviceNum, topic);

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("topic", topic);
            messageMap.put("payload", "UPDATE_DEVICES");
            // qos 1 确保消息到达
            MqttMessage mqttMessage = new MqttMessage(objectMapper.writeValueAsBytes(messageMap));
            mqttMessage.setQos(1);
            client.publish(MqttService.WX_CTRL + deviceNum, mqttMessage);
        } catch (MqttException | JsonProcessingException e) {
            log.warn("notifyToUpdate 出错了");
            throw new RuntimeException(e);
        }
    }

    /**
     * 通知前端 更新页面
     *
     * @param deviceNum
     */
    public void queryDeviceStatus(String deviceNum) {
        // try {
        // Map<String, Object> messageMap = new HashMap<>();
        // messageMap.put("topic", MqttService.QUERY_DEVICE_STATUS(deviceNum));
        // messageMap.put("payload", "QUERY_DEVICE_STATUS");
        // // qos 1 确保消息到达
        // MqttMessage mqttMessage = new
        // MqttMessage(objectMapper.writeValueAsBytes(messageMap));
        // mqttMessage.setQos(1);
        // client.publish(MqttService.QUERY_DEVICE_STATUS(deviceNum), mqttMessage);

        this.publishString(MqttService.QUERY_DEVICE_STATUS(deviceNum), "QUERY_DEVICE_STATUS");

        // } catch (MqttException e) {
        // throw new RuntimeException(e);
        // }
    }

    
    /**
     * 处理设备所有电机的控制规则
     * 从数据库读取传感器和电机配置，应用控制规则
     *
     * @param deviceId  设备ID
     * @param deviceNum
     */
    @SuppressWarnings("unused")
    private void processMotorControlRules(Long deviceId, String deviceNum) {
        try {
            // 获取该设备的所有电机
            List<MotorFan> motors = motorFanService.findByParentId(deviceId);

            if (motors == null || motors.isEmpty()) {
                return;
            }

            // 为每个电机处理控制规则
            // for在循环前，一次性获取所有传感器
            List<Sensor> sensors = sensorService.findByDeviceId(deviceId);

            // 创建 sensorId -> sensor 的映射，便于快速查找
            Map<Long, Sensor> sensorMap = new HashMap<>();
            if (sensors != null) {
                for (Sensor sensor : sensors) {
                    sensorMap.put(sensor.getId(), sensor);
                }
            }

            // 为每个电机处理控制规则
            for (MotorFan motor : motors) {
                try {
                    Double currentSensorValue = getTempValueBySelection(sensorMap, motor.getTcps());
                    // 应用控制规则
                    motorControlRuleEngineService.processMotorControl(motor, currentSensorValue, deviceNum);

                } catch (Exception e) {
                    log.error("处理电机控制规则错误: motorId={}", motor.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("处理电机控制规则错误: deviceId={}", deviceId, e);
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    /**
     * 发送 MQTT 消息到指定主题
     *
     * @param topic   消息主题
     * @param payload 消息内容（对象会被序列化为 JSON）
     * @param qos     服务质量等级 (0, 1, 2)，默认为 1
     * @return 发送是否成功
     */
    public boolean publishMessage(String topic, Object payload, int qos) {
        try {
            if (!client.isConnected()) {
                log.warn("MQTT client is not connected, attempting to reconnect");
                client.connect();
            }

            byte[] payloadBytes;
            if (payload instanceof String) {
                payloadBytes = ((String) payload).getBytes(StandardCharsets.UTF_8);
            } else {
                payloadBytes = objectMapper.writeValueAsBytes(payload);
            }

            MqttMessage message = new MqttMessage(payloadBytes);
            message.setQos(Math.min(qos, 2)); // QoS 范围：0-2
            message.setRetained(false);

            client.publish(topic, message);
            log.info("MQTT message published: topic={}, qos={}, payloadSize={} bytes",
                    topic, qos, payloadBytes.length);
            return true;

        } catch (MqttException e) {
            log.error("Failed to publish MQTT message to topic: {}", topic, e);
            return false;
        } catch (Exception e) {
            log.error("Error serializing MQTT payload for topic: {}", topic, e);
            return false;
        }
    }

    /**
     * 发送 MQTT 消息到指定主题 (QoS 默认为 1)
     *
     * @param topic   消息主题
     * @param payload 消息内容
     * @return 发送是否成功
     */
    public boolean publishMessage(String topic, Object payload) {
        return publishMessage(topic, payload, 1);
    }

    /**
     * 发送 MQTT 消息到指定主题 (消息为字符串，QoS 默认为 1)
     *
     * @param topic   消息主题
     * @param message 消息内容（字符串）
     * @return 发送是否成功
     */
    public boolean publishString(String topic, String message) {
        return publishMessage(topic, message, 1);
    }

    /**
     * 下发工厂设置到设备
     */
    public boolean publishFactorySetting(Device device) {
        if (device == null || !StringUtils.hasText(device.getDeviceNum())) {
            return false;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("taul", toProtocolScaledValue(device.getTempUpperLimit(), 0));
        payload.put("tadl", toProtocolScaledValue(device.getTempLowerLimit(), 0));
        payload.put("haul", toProtocolScaledValue(device.getHumidityUpperLimit(), 0));
        payload.put("hadl", toProtocolScaledValue(device.getHumidityLowerLimit(), 0));
        payload.put("naul", intOrDefault(device.getGasUpperLimit(), 0));
        payload.put("nadl", intOrDefault(device.getGasLowerLimit(), 0));

        // 当前数据库无补偿字段，按协议下发默认 0
        payload.put("tcv1", 0);
        payload.put("tcv2", 0);
        payload.put("tcv3", 0);
        payload.put("tcv4", 0);
        payload.put("hcv", 0);
        payload.put("ncv", 0);

        payload.put("tof1", intOrDefault(device.getTof1(), 0));
        payload.put("tof2", intOrDefault(device.getTof2(), 0));
        payload.put("tof3", intOrDefault(device.getTof3(), 0));
        payload.put("tof4", intOrDefault(device.getTof4(), 0));
        payload.put("lt", intOrDefault(device.getLevelTime(), 0));

        payload.put("hr", intOrDefault(device.getHr(), 100));
        payload.put("nr", intOrDefault(device.getNr(), 100));
        payload.put("tb", intOrDefault(device.getTb(), 10));
        payload.put("hb", intOrDefault(device.getHb(), 1));
        payload.put("nb", intOrDefault(device.getNb(), 1));

        String topic = FACTORY_SET(device.getDeviceNum());
        boolean published = publishMessage(topic, payload, 1);
        if (published) {
            log.info("工厂设置已下发: topic={}, deviceNum={}", topic, device.getDeviceNum());
        } else {
            log.warn("工厂设置下发失败: topic={}, deviceNum={}", topic, device.getDeviceNum());
        }
        return published;
    }

    private Integer toProtocolScaledValue(Double value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.TEN)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int intOrDefault(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Double getTempValueBySelection(Map<Long, Sensor> sensorMap, Integer selection) {
        if (sensorMap == null || sensorMap.isEmpty() || selection == null) {
            return null;
        }

        Map<String, Double> tempMap = new HashMap<>();
        for (Sensor sensor : sensorMap.values()) {
            if (sensor.getSensorCode() != null && sensor.getSensorValue() != null) {
                tempMap.put(sensor.getSensorCode(), sensor.getSensorValue());
            }
        }

        switch (selection) {
            case 0:
                return tempMap.get("ts1");
            case 1:
                return tempMap.get("ts2");
            case 2:
                return tempMap.get("ts3");
            case 3:
                return tempMap.get("ts4");
            case 4:
                return avg(tempMap.get("ts1"), tempMap.get("ts2"));
            case 5:
                return avg(tempMap.get("ts3"), tempMap.get("ts4"));
            case 6:
            case 7:
                return avg(tempMap.get("ts1"), tempMap.get("ts2"), tempMap.get("ts3"), tempMap.get("ts4"));
            default:
                return null;
        }
    }

    private Double avg(Double... values) {
        double sum = 0D;
        int count = 0;
        for (Double value : values) {
            if (value != null) {
                sum += value;
                count++;
            }
        }
        return count == 0 ? null : sum / count;
    }

    /**
     * 检查 MQTT 连接状态
     *
     * @return 如果连接则返回 true，否则返回 false
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }
}
