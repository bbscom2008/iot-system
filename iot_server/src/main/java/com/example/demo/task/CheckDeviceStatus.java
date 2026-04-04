package com.example.demo.task;

import com.example.demo.entity.Device;
import com.example.demo.mapper.DeviceMapper;
import com.example.demo.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Component
public class CheckDeviceStatus {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    @Autowired
    public DeviceMapper deviceMapper;
    /**
      *  每 20 分钟检测一次设备更新时间，超过 5 分钟则标记为离线
      */
    @Scheduled(fixedRate = 20 * 60 * 1000)
    public void scheduledCheckDeviceOnlineStatus() {
        try {
            List<Device> devices = deviceMapper.findList(new HashMap<>());
            LocalDateTime now = LocalDateTime.now();
            for (Device d : devices) {
                LocalDateTime lastOnlineTime = d.getLastOnlineTime();
                int state = 0;
                if (lastOnlineTime != null) {
                    Duration diff = Duration.between(lastOnlineTime, now);
                    if (Math.abs(diff.getSeconds()) <= 5 * 60) {
                        state = 1;
                    } else {
                        state = 0;
                    }
                } else {
                    state = 0;
                }

                try {
                    // 如果新状态和原状态不同，就更新状态
                    if (!Objects.equals(d.getDeviceLineState(), state)) {
                        deviceMapper.updateDeviceOnlineState(d.getDeviceNum(), state);
                    }
                } catch (Exception ex) {
                    logger.error("Failed to update device online state for deviceNum={}", d.getDeviceNum(), ex);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to check device online status", e);
        }
    }
}
