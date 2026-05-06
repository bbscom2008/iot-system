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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class CheckDeviceStatus {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    @Autowired
    public DeviceMapper deviceMapper;
    /**
      *  每 20 分钟检测一次设备更新时间，超过 5 分钟则标记为离线
      *  应用启动后立即执行一次，然后每 20 分钟执行一次
      */
    @Scheduled(initialDelay = 0, fixedRate = 20 * 60 * 1000)
    public void scheduledCheckDeviceOnlineStatus() {
        try {
            List<Device> devices = deviceMapper.findList(new HashMap<>());
            LocalDateTime now = LocalDateTime.now();
            List<Map<String, Object>> updates = new ArrayList<>();
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

                // 如果新状态和原状态不同，就添加到更新列表
                if (!Objects.equals(d.getDeviceLineState(), state)) {
                    Map<String, Object> update = new HashMap<>();
                    update.put("deviceNum", d.getDeviceNum());
                    update.put("state", state);
                    updates.add(update);
                }
            }

            // 批量更新设备状态
            if (!updates.isEmpty()) {
                try {
                    deviceMapper.updateDeviceOnlineStates(updates);
                    logger.info("批量更新了 {} 个设备的在线状态", updates.size());
                } catch (Exception ex) {
                    logger.error("批量更新设备在线状态失败", ex);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to check device online status", e);
        }
    }
}
