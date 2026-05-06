package com.example.demo.task;

import com.example.demo.mapper.DeviceMapper;
import com.example.demo.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InitTask {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    @Autowired
    public DeviceMapper deviceMapper;

    // 在应用启动完成后，将所有设备的在线状态和报警状态初始化为 0，并更新 updated_time
    @EventListener(ApplicationReadyEvent.class)
    public void initDeviceStatesOnStartup() {
            logger.info("初始化，现在啥也不干");
        // try {
        //    deviceMapper.resetAllDeviceStates();
        //     logger.info("Reset all device states on startup (device_line_state=0, warning_status=0, updated_time=now)");
        // } catch (Exception e) {
        //     logger.error("Failed to reset device states on startup", e);
        // }
    }
}
