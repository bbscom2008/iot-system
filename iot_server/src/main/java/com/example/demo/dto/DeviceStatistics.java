package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatistics {

    private Long totalDevices;    // 总设备数
    private Long onlineDevices;   // 在线设备数
    private Long offlineDevices;  // 离线设备数
    private Long alarmDevices;    // 报警设备数
}

