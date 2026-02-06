package com.example.demo.service;

import com.example.demo.dto.MotorControlMessage;
import com.example.demo.entity.Device;
import com.example.demo.entity.MotorFan;
import com.example.demo.entity.MotorFanTimerTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.example.demo.entity.MotorFanTimerTask.TIME_FORMATTER;

/**
 * 电机控制规则引擎服务
 * 根据配置和传感器数据管理电机状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotorControlRuleEngineService {

    private final MotorFanService motorFanService;
    private final SensorService sensorService;
    private final MotorControlProducerService motorControlProducerService;

    private final ObjectMapper objectMapper;

    private final DeviceService deviceService;

    // 延迟获取 MqttService 以打破启动时的循环依赖
//    private final ObjectProvider<MqttService> mqttServiceProvider;

    @Lazy
    @Autowired
    private MqttService mqttService;


    // 跟踪为电机调度的延时切换，键格式：motorNum:parentDeviceNum
    private final ConcurrentMap<String, Long> scheduledUntil = new ConcurrentHashMap<>();

    /**
     * 根据配置和传感器数据处理电机控制
     *
     * @param motorFan           电机
     * @param currentSensorValue 当前传感器数值 温度
     * @param deviceNum
     */
    public void processMotorControl(MotorFan motorFan, Double currentSensorValue, String deviceNum) {
        try {
            if (motorFan == null) {
                log.warn("未找到电机: motorId");
                return;
            }

            if(deviceNum == null){
                Long deviceId = motorFan.getDeviceId();
                Device device = deviceService.findByDeviceId(deviceId);
                deviceNum = device.getDeviceNum();
            }

            log.warn("电机: motorId={}， model={}", motorFan.getDeviceId(), motorFan.getAutoMode());

            // 第一步：检查自动模式
            // 1 = 自动模式, 2 = 始终开启, 3 = 始终关闭
            if (motorFan.getAutoMode() == 2) {
                removeScheduleKey(motorFan.getDeviceNum(), deviceNum);
                // 如果当前状态不是运行，则发送开启命令
                if (motorFan.getIsRunning() != 1) {
                    // 清除定时任务 标记
                    updateMotorFanState(motorFan.getDeviceNum(), 1, deviceNum);
                }
                return;
            } else if (motorFan.getAutoMode() == 3) {
                removeScheduleKey(motorFan.getDeviceNum(), deviceNum);
                // 如果当前状态不是停止，则发送关闭命令
                if (motorFan.getIsRunning() != 0) {
                    updateMotorFanState(motorFan.getDeviceNum(), 0, deviceNum);
                }
                return;
            } else if (motorFan.getAutoMode() != 1) {
                log.warn("无效的自动模式: motorId={}, autoMode={}", motorFan.getDeviceId(), motorFan.getAutoMode());
                return;
            }

            // 第二步：按控制模式处理（仅当autoMode = 1时）
            Integer controlMode = motorFan.getControlMode();

            if (controlMode == null) {
                log.warn("未设置控制模式: motorId={}", motorFan.getDeviceId());
                return;
            }

            Integer newState = 0;

            switch (controlMode) {
                case 1: // 温控
                    newState = processTemperatureControl(motorFan, currentSensorValue, deviceNum);
                    break;
                case 2: // 循环
                    newState = processCycleControl(motorFan);
                    break;
                case 3:
                    //  目前还没有 湿度传感器
                    log.warn("目前还没有温度控制模式: motorId={}，controlMode={}", motorFan.getDeviceId(), controlMode);
                    break;
                case 4:
                    // 目前还没有 气体传感器
                    log.warn("目前还没有气体控制模式: motorId={}, controlMode={}", motorFan.getDeviceId(), controlMode);
                    break;
                case 5:
                    // 定时
                    newState = processTimerControl(motorFan,deviceNum);
                    break;
                default:
                    log.warn("未知的控制模式: motorId={}, controlMode={}", motorFan.getDeviceId(), controlMode);
                    return;
            }

            // 如果状态改变，发送控制消息
            if (newState != null && !newState.equals(motorFan.getIsRunning())) {
                updateMotorFanState(motorFan.getDeviceNum(), newState, deviceNum);
            }

        } catch (Exception e) {
            if (motorFan != null) {
                log.error("处理电机控制错误: motorId={}", motorFan.getDeviceId(), e);
            }
        }
    }

    /**
     * 处理温度控制模式
     *
     * @param motorFan    电机配置
     * @param currentTemp 当前温度 可以为空
     * @return 新电机状态 (0 = 停止, 1 = 运行)
     */
    private Integer processTemperatureControl(MotorFan motorFan, Double currentTemp, String deviceNum) {
        if (currentTemp == null) {
            // 根据 motorFan 绑定的温度传感器，去查询 当前温度值
            Long sensorId = motorFan.getProbeSensorId();
            if (sensorId != null) {
                currentTemp = sensorService.getSensorValueById(sensorId);
                log.info("获取传感器温度: sensorId={}, temperature={}", sensorId, currentTemp);
            }else{
                log.info("当前电机没有设置 温度传感器: motorFanId={}, sensorId={}", motorFan.getDeviceId(), sensorId);
                return motorFan.getIsRunning();
            }
        }

        Double upper = motorFan.getTempUpper();
        Double lower = motorFan.getTempLower();

        if (upper == null || lower == null) {
            log.warn("温度限制未设置: motorId={}", motorFan.getId());
            return motorFan.getIsRunning();
        }

        // 如果当前温度 >= 上限，则开启
        if (currentTemp >= upper) {
            removeScheduleKey(motorFan.getDeviceNum(), deviceNum);
            return 1;
        }
        // 如果当前温度 <= 下限，则关闭
        else if (currentTemp <= lower) {
            removeScheduleKey(motorFan.getDeviceNum(), deviceNum);
            return 0;
        }
        // 否则，保持当前状态，并按照运行/暂停时间进行循环调度
        else {
            Integer runTime = motorFan.getRunTime(); // 运行多少 秒
            Integer pauseTime = motorFan.getPauseTime(); // 暂停多少秒 

            if (runTime == null || pauseTime == null || runTime <= 0 || pauseTime <= 0) {
                log.warn("循环时间未设置或无效: motorId={}", motorFan.getId());
                return motorFan.getIsRunning();
            }

            String key = motorFan.getDeviceNum() + ":" + deviceNum;
            long now = System.currentTimeMillis();

            // 如果当前正在运行，排期在 runTime 秒后停止；如果当前停止，排期在 pauseTime 秒后启动
            if (motorFan.getIsRunning() != null && motorFan.getIsRunning() == 1) {
                long scheduledTs = scheduledUntil.getOrDefault(key, 0L);
                if (scheduledTs > now) {
                    // 已经有排程，跳过重复排队
                    return 1;
                }

                int delayMs = runTime * 1000;
                scheduledUntil.put(key, now + delayMs);
                sendMotorControlMessage(motorFan, 0, delayMs, deviceNum);
                return 1;
            } else {
                long scheduledTs = scheduledUntil.getOrDefault(key, 0L);
                if (scheduledTs > now) {
                    return 0;
                }

                int delayMs = pauseTime * 1000;
                scheduledUntil.put(key, now + delayMs);
                sendMotorControlMessage(motorFan, 1, delayMs, deviceNum);
                return 0;
            }
        }
    }

    /**
     * 处理循环控制模式（运行X秒，然后暂停Y秒）
     *
     * @param motorFan 电机配置
     * @return 新电机状态
     */
    private Integer processCycleControl(MotorFan motorFan) {
        // 注意：循环控制需要跟踪时间状态
        // 这是一个简化的实现 - 你可能需要存储状态
        Integer runTime = motorFan.getRunTime();
        Integer pauseTime = motorFan.getPauseTime();

        if (runTime == null || pauseTime == null) {
            log.warn("循环时间未设置: motorId={}", motorFan.getId());
            return motorFan.getIsRunning();
        }

        // 保持当前状态 - 实际循环管理应由定时器完成
        return motorFan.getIsRunning();
    }

    /**
     * 处理湿度控制模式
     *
     * @param motorFan        电机配置
     * @param currentHumidity 当前湿度值
     * @return 新电机状态
     */
    private Integer processHumidityControl(MotorFan motorFan, Double currentHumidity) {
        if (currentHumidity == null) {
            return motorFan.getIsRunning();
        }

        Double upper = motorFan.getHumidityUpper();
        Double lower = motorFan.getHumidityLower();

        if (upper == null || lower == null) {
            log.warn("湿度限制未设置: motorId={}", motorFan.getId());
            return motorFan.getIsRunning();
        }

        // 如果湿度 >= 上限，则开启
        if (currentHumidity >= upper) {
            return 1;
        }
        // 如果湿度 <= 下限，则关闭
        else if (currentHumidity <= lower) {
            return 0;
        }
        // 否则，保持当前状态
        else {
            return motorFan.getIsRunning();
        }
    }

    /**
     * 处理气体控制模式
     *
     * @param motorFan   电机配置
     * @param currentGas 当前气体值（ppm）
     * @return 新电机状态
     */
    private Integer processGasControl(MotorFan motorFan, Double currentGas) {
        if (currentGas == null) {
            return motorFan.getIsRunning();
        }

        Integer upper = motorFan.getGasUpper();
        Integer lower = motorFan.getGasLower();

        if (upper == null || lower == null) {
            log.warn("气体限制未设置: motorId={}", motorFan.getId());
            return motorFan.getIsRunning();
        }

        // 如果气体 >= 上限，则开启
        if (currentGas >= upper) {
            return 1;
        }
        // 如果气体 <= 下限，则关闭
        else if (currentGas <= lower) {
            return 0;
        }
        // 否则，保持当前状态
        else {
            return motorFan.getIsRunning();
        }
    }

    /**
     * 处理定时控制模式
     * 检查多个定时器并确定最终状态
     *
     * @param motorFan  电机配置
     * @param deviceNum
     * @return 新电机状态 null 异步处理了，  1 或 0 ，立刻更新
     */
    private Integer processTimerControl(MotorFan motorFan, String deviceNum) {
        LocalTime currentTime = LocalTime.now();
        int resultState = 0;

        // 获得所有的定时任务
        List<MotorFanTimerTask> taskList = getAllEnableTask(motorFan);
        // 对 taskList 按开始时间 排序
        Collections.sort(taskList);

        // 找到要处理的时间点
        LocalTime now = LocalTime.now();
//        LocalTime now = LocalTime.parse("0:00", TIME_FORMATTER);
        MotorFanTimerTask processTimeTask = null;

        for (MotorFanTimerTask task : taskList) {
            String startTime = task.getStartTime();
            String endTime = task.getEndTime();
            if (now.isBefore(LocalTime.parse(startTime, TIME_FORMATTER))
                    || isTimeInRange(now, startTime, endTime)) {
                processTimeTask = task;
                break;
            }
        }
        if (processTimeTask == null) {
            return null;
        }

        // 时间还没到，发送定时任务，到时候再执行
        LocalTime processStartTime = LocalTime.parse(processTimeTask.getStartTime(), TIME_FORMATTER);
        if (now.isBefore(processStartTime)) {
            // 现在到指定时间还有多少秒
            long secondsDiff = now.until(processStartTime, ChronoUnit.SECONDS);
            System.out.println("到开始时间还有: " + secondsDiff + "秒");
        } else {
            // 当前时间在指定时间段内,


        }


        return resultState;
    }

    /**
     * 获得所有定时任务
     * @param motorFan
     * @return
     */
    private List<MotorFanTimerTask> getAllEnableTask(MotorFan motorFan) {

        List<MotorFanTimerTask> taskList = new ArrayList<>();
        // 总共 3 个传感器
        if (motorFan.getTimer1Enabled() == 1) {
            taskList.add(new MotorFanTimerTask(motorFan.getTimer1Enabled(), motorFan.getTimer1StartTime(), motorFan.getTimer1EndTime(),
             motorFan.getTimer1ProbeSensorId(), motorFan.getTimer1StartTemp(), motorFan.getTimer1StopTemp()));
        }
        if (motorFan.getTimer2Enabled() == 1) {
            taskList.add(new MotorFanTimerTask(motorFan.getTimer2Enabled(), motorFan.getTimer2StartTime(), motorFan.getTimer2EndTime(),
                    motorFan.getTimer2ProbeSensorId(), motorFan.getTimer2StartTemp(), motorFan.getTimer2StopTemp()));
        }
        if (motorFan.getTimer3Enabled() == 1) {
            taskList.add(new MotorFanTimerTask(motorFan.getTimer3Enabled(), motorFan.getTimer3StartTime(), motorFan.getTimer3EndTime(),
                    motorFan.getTimer3ProbeSensorId(), motorFan.getTimer3StartTemp(), motorFan.getTimer3StopTemp()));
        }

        return taskList;
    }

    /**
     * 检查当前时间是否在指定范围内
     *
     * @param currentTime 当前时间
     * @param startTime   开始时间（HH:mm格式）
     * @param endTime     结束时间（HH:mm格式）
     * @return 如果在范围内返回true
     */
    private boolean isTimeInRange(LocalTime currentTime, String startTime, String endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }

        try {
            LocalTime start = LocalTime.parse(startTime, TIME_FORMATTER);
            LocalTime end = LocalTime.parse(endTime, TIME_FORMATTER);

            if (start.isBefore(end)) {
                // 正常情况：开始 < 结束 (例如 08:00 到 17:00)
                return !currentTime.isBefore(start) && currentTime.isBefore(end);
            } else {
                // 跨越午夜情况：开始 > 结束 (例如 22:00 到 06:00)
                return !currentTime.isBefore(start) || currentTime.isBefore(end);
            }
        } catch (Exception e) {
            log.warn("无效的时间格式: startTime={}, endTime={}", startTime, endTime);
            return false;
        }
    }

    /**
     * 发送mqtt消息更新 motorFan 的状态
     *
     * @param motorNum  如   mt1  mt2 mt3
     * @param state     新状态  0 1
     * @param deviceNum 设置名称  d002  d004
     */
    public void updateMotorFanState(String motorNum, Integer state, String deviceNum) {

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("id", deviceNum);
        jsonMap.put(motorNum, state);

        String payload = null;
        Device device = deviceService.findByDeviceNum(deviceNum);

        try {
            payload = objectMapper.writeValueAsString(jsonMap);
            // 发送 MQTT 消息 更新硬件状态
            if (mqttService != null) {
                boolean publishSuccess = mqttService.publishString(MqttService.DEVICE_CTRL(deviceNum), payload);
                if (!publishSuccess) {
                    log.warn("MQTT消息发送失败: driverNum={}, motorNum={}", deviceNum, motorNum);
                }
                // 更新数据库，当前电机状态已经更新
                motorFanService.updateRunningStatusByParentAndCode( device.getId(), motorNum, state);

                log.warn("MQTT消息发送成功: driverNum={}, motorNum={}", deviceNum, motorNum);

                // 通知前端页面，更新状态
                mqttService.notifyToUpdate(deviceNum);

            } else {
                log.warn("MQTT服务不可用，无法发送消息: driverNum={}, motorNum={}", deviceNum, motorNum);
            }

            // 清除延时任务标记
            removeScheduleKey(motorNum, deviceNum);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送mqtt消息更新 motorFan 的状态
     *
     * @param motorNum  如   mt1  mt2 mt3
     * @param state     新状态  0 1
     * @param deviceNum 设置名称  d002  d004
     */
    public void updateMotorFanStateByDelayMessage(String motorNum, Integer state, String deviceNum) {

        // 如果延时消息，没有被覆盖，则执行状态更新
        String key = motorNum + ":" + deviceNum;
        Long time = scheduledUntil.getOrDefault(key, 0L);
        // 判断延时任务是否已被取消。
        if(time == 0){
            return ;
        }
        // 更新状态
        updateMotorFanState(motorNum, state, deviceNum);
        
        Device device = deviceService.findByDeviceNum(deviceNum);
        // 清理该电机(父设备)的排程标记，允许重新排程
        try {
            if (motorNum != null && deviceNum != null) {
                // 开始新一轮排程
                // 根据 deviceId 和 motorNum 获得 motorFan 对象
                if (device != null) {
                    MotorFan motorFan = motorFanService.findByDeviceIdAndMotorNum(device.getId(), motorNum);
                    if (motorFan != null) {
                        processMotorControl(motorFan, null, deviceNum);
                    }
                }

            }
        } catch (Exception e) {
            log.warn("清理调度标记失败: motorNum={}, deviceNum={}", motorNum, deviceNum, e);
        }
    }

    /**
     * 清除延时任务的 KEY
     * @param motorNum motorFan 的 num
     * @param deviceNum device的 num
     */
    private void removeScheduleKey(String motorNum, String deviceNum) {
        String key = motorNum + ":" + deviceNum;
        log.warn("removeScheduleKey : "+key);
        scheduledUntil.remove(key);
    }


    /**
     * 发送电机控制消息到RabbitMQ
     *
     * @param motorFan  电机配置
     * @param newState  新电机状态
     * @param delayTime 延时时间（毫秒）
     * @param deviceNum 设备编号  d002 等。
     */
    private void sendMotorControlMessage(MotorFan motorFan, Integer newState, Integer delayTime, String deviceNum) {
        MotorControlMessage message = MotorControlMessage.builder()
                .motorId(motorFan.getId())
                .deviceId(motorFan.getDeviceId())
                .deviceNum(motorFan.getDeviceNum())
                .parentDeviceNum(deviceNum)
                .state(newState)
                .controlMode(motorFan.getControlMode())
                .autoMode(motorFan.getAutoMode())
                .delayTime(delayTime)
                .timestamp(System.currentTimeMillis())
                .build();

        if (delayTime != null && delayTime > 0) {
            motorControlProducerService.sendMotorControlDelayMessage(message);
        } else {
            motorControlProducerService.sendMotorControlMessage(message);
        }
    }
}
