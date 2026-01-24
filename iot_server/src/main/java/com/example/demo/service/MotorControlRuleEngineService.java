package com.example.demo.service;

import com.example.demo.dto.MotorControlMessage;
import com.example.demo.entity.MotorFan;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 根据配置和传感器数据处理电机控制
     * @param motorId 电机ID
     * @param currentSensorValue 当前传感器数值（温度、湿度或气体）
     */
    public void processMotorControl(Long motorId, Double currentSensorValue) {
        try {
            MotorFan motorFan = motorFanService.findById(motorId);
            if (motorFan == null) {
                log.warn("未找到电机: motorId={}", motorId);
                return;
            }

            // 第一步：检查自动模式
            // 1 = 自动模式, 2 = 始终开启, 3 = 始终关闭
            if (motorFan.getAutoMode() == 2) {
                // Always on
                sendMotorControlMessage(motorFan, 1, 1000);
                return;
            } else if (motorFan.getAutoMode() == 3) {
                // Always off
                sendMotorControlMessage(motorFan, 0, 2000);
                return;
            } else if (motorFan.getAutoMode() != 1) {
                log.warn("无效的自动模式: motorId={}, autoMode={}", motorId, motorFan.getAutoMode());
                return;
            }

            // 第二步：按控制模式处理（仅当autoMode = 1时）
            Integer controlMode = motorFan.getControlMode();
            
            if (controlMode == null) {
                log.warn("未设置控制模式: motorId={}", motorId);
                return;
            }

            Integer newState = 0;

            switch (controlMode) {
                case 1:
                    // Temperature control
                    newState = processTemperatureControl(motorFan, currentSensorValue);
                    break;
                case 2:
                    // Cycle control (run/pause cycle)
                    newState = processCycleControl(motorFan);
                    break;
                case 3:
                    // Humidity control
                    newState = processHumidityControl(motorFan, currentSensorValue);
                    break;
                case 4:
                    // Gas control
                    newState = processGasControl(motorFan, currentSensorValue);
                    break;
                case 5:
                    // Timer control
                    newState = processTimerControl(motorFan);
                    break;
                default:
                    log.warn("未知的控制模式: motorId={}, controlMode={}", motorId, controlMode);
                    return;
            }

            // 如果状态改变，发送控制消息
            if (!newState.equals(motorFan.getIsRunning())) {
                sendMotorControlMessage(motorFan, newState, 2000);
            }

        } catch (Exception e) {
            log.error("处理电机控制错误: motorId={}", motorId, e);
        }
    }

    /**
     * 处理温度控制模式
     * @param motorFan 电机配置
     * @param currentTemp 当前温度
     * @return 新电机状态 (0 = 停止, 1 = 运行)
     */
    private Integer processTemperatureControl(MotorFan motorFan, Double currentTemp) {
        if (currentTemp == null) {
            return motorFan.getIsRunning();
        }

        Double upper = motorFan.getTempUpper();
        Double lower = motorFan.getTempLower();

        if (upper == null || lower == null) {
            log.warn("温度限制未设置: motorId={}", motorFan.getId());
            return motorFan.getIsRunning();
        }

        // 如果当前温度 >= 上限，则开启
        if (currentTemp >= upper) {
            return 1;
        }
        // 如果当前温度 <= 下限，则关闭
        else if (currentTemp <= lower) {
            return 0;
        }
        // 否则，保持当前状态
        else {
            return motorFan.getIsRunning();
        }
    }

    /**
     * 处理循环控制模式（运行X秒，然后暂停Y秒）
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
     * @param motorFan 电机配置
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
     * @param motorFan 电机配置
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
     * @param motorFan 电机配置
     * @return 新电机状态
     */
    private Integer processTimerControl(MotorFan motorFan) {
        LocalTime currentTime = LocalTime.now();
        int resultState = 0;

        // 检查定时器1
        if (motorFan.getTimer1Enabled() == 1) {
            if (isTimeInRange(currentTime, motorFan.getTimer1StartTime(), motorFan.getTimer1EndTime())) {
                resultState = 1;
            }
        }

        // 检查定时器2
        if (motorFan.getTimer2Enabled() == 1) {
            if (isTimeInRange(currentTime, motorFan.getTimer2StartTime(), motorFan.getTimer2EndTime())) {
                resultState = 1;
            }
        }

        // 检查定时器3
        if (motorFan.getTimer3Enabled() == 1) {
            if (isTimeInRange(currentTime, motorFan.getTimer3StartTime(), motorFan.getTimer3EndTime())) {
                resultState = 1;
            }
        }

        return resultState;
    }

    /**
     * 检查当前时间是否在指定范围内
     * @param currentTime 当前时间
     * @param startTime 开始时间（HH:mm格式）
     * @param endTime 结束时间（HH:mm格式）
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
     * 发送电机控制消息到RabbitMQ
     * @param motorFan 电机配置
     * @param newState 新电机状态
     * @param delayTime 延时时间（毫秒）
     */
    private void sendMotorControlMessage(MotorFan motorFan, Integer newState, Integer delayTime) {
        MotorControlMessage message = MotorControlMessage.builder()
                .motorId(motorFan.getId())
                .deviceId(motorFan.getDeviceId())
                .deviceNum(motorFan.getDeviceNum())
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
