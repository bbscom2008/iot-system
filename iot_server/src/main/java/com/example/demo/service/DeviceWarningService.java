package com.example.demo.service;

import com.example.demo.dto.PageResult;
import com.example.demo.entity.DeviceWarning;
import com.example.demo.mapper.DeviceWarningMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeviceWarningService {

    private final DeviceWarningMapper deviceWarningMapper;

    /**
     * 获取报警列表（分页）
     * @param userId 用户ID，为null时查询所有报警
     */
    public PageResult<DeviceWarning> getWarningList(Long userId, Integer pageNum, Integer pageSize) {
        Map<String, Object> params = new HashMap<>();
        // userId为null时不添加过滤条件，查询所有报警
        if (userId != null) {
            params.put("userId", userId);
        }
        params.put("isRead", 0); // 只查询未读

        // 分页参数
        if (pageNum != null && pageSize != null) {
            int offset = (pageNum - 1) * pageSize;
            params.put("offset", offset);
            params.put("pageSize", pageSize);
        }

        List<DeviceWarning> list = deviceWarningMapper.findList(params);
        Long total = deviceWarningMapper.countWarning(params);

        return PageResult.of(list, total, pageNum, pageSize);
    }

    /**
     * 标记报警已读
     */
    @Transactional
    public void markWarningRead(String deviceNum, Long userId) {
        deviceWarningMapper.markRead(deviceNum, userId);
    }
}

