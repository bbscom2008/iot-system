package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MotorFanListDTO;
import com.example.demo.entity.Device;
import com.example.demo.entity.MotorFan;
import com.example.demo.service.DeviceService;
import com.example.demo.service.MotorControlRuleEngineService;
import com.example.demo.service.MotorFanService;
import com.example.demo.service.MqttService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/motor-fan")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MotorFanController {

    private final MotorFanService motorFanService;
    private final DeviceService deviceService;
    private final MqttService mqttService;

    private final MotorControlRuleEngineService motorControlProducerService;

    /**
     * 获取所有风机列表（关联设备和用户信息）
     * GET /motor-fan/list
     */
    @GetMapping("/list")
    public ApiResponse<List<MotorFanListDTO>> getAllMotorFans(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userPhone,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String deviceNum,
            @RequestParam(required = false) String fanName) {

        // 构造查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("userName", userName);
        params.put("userPhone", userPhone);
        params.put("deviceName", deviceName);
        params.put("deviceNum", deviceNum);
        params.put("fanName", fanName);

        List<MotorFanListDTO> motorFans = motorFanService.findAll(params);
        return ApiResponse.success(motorFans);
    }

    /**
     * 获取父设备下的所有风机
     * GET /motor-fan/list/{parentId}
     */
    @GetMapping("/list/{parentId}")
    public ApiResponse<List<MotorFan>> getMotorFanList(@PathVariable Long parentId) {
        List<MotorFan> motorFans = motorFanService.findByParentId(parentId);
        return ApiResponse.success(motorFans);
    }

    /**
     * 根据ID获取风机详情
     * GET /motor-fan/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<MotorFan> getMotorFanById(@PathVariable Long id) {
        MotorFan motorFan = motorFanService.findById(id);
        return ApiResponse.success(motorFan);
    }

    /**
     * 新增风机
     * POST /motor-fan
     */
    @PostMapping
    public ApiResponse<String> addMotorFan(@RequestBody MotorFan motorFan) {
        // 验证必填字段
        if (motorFan.getFanName() == null || motorFan.getFanName().trim().isEmpty()) {
            throw new RuntimeException("风机名称不能为空");
        }
        if (motorFan.getDeviceId() == null) {
            throw new RuntimeException("父设备ID不能为空");
        }
        if (motorFan.getDeviceNum() == null || motorFan.getDeviceNum().trim().isEmpty()) {
            throw new RuntimeException("风机编码不能为空");
        }

        // 设置默认值
        if (motorFan.getIsRunning() == null) {
            motorFan.setIsRunning(0); // 默认停止
        }
        if (motorFan.getWm() == null) {
            motorFan.setWm(0); // 默认温控
        }
        if (motorFan.getAutoMode() == null) {
            motorFan.setAutoMode(1); // 默认自动
        }

        motorFanService.insert(motorFan);
        return ApiResponse.success("风机添加成功");
    }

    /**
     * 更新风机配置
     * PUT /motor-fan/update
     */
    @PutMapping("/update")
    public ApiResponse<String> updateMotorFan(@RequestBody MotorFan motorFan) {
        // 检查ID是否存在
        if (motorFan.getId() == null) {
            throw new RuntimeException("风机ID不能为空");
        }

        MotorFan existFan = motorFanService.findById(motorFan.getId());
        if (existFan == null) {
            throw new RuntimeException("风机不存在");
        }

        motorFanService.update(motorFan);

        MotorFan latestFan = motorFanService.findById(motorFan.getId());
        if (latestFan != null) {
            Device parentDevice = deviceService.findByDeviceId(latestFan.getDeviceId());
            if (parentDevice != null
                    && parentDevice.getDeviceNum() != null && !parentDevice.getDeviceNum().trim().isEmpty()
                    && latestFan.getDeviceNum() != null && !latestFan.getDeviceNum().trim().isEmpty()) {

                String stm32Id = parentDevice.getDeviceNum();
                String mtx = latestFan.getDeviceNum();
                String topic = "server/setting/" + stm32Id + "/" + mtx;

                Map<String, Object> payload = buildMotorFanSettingPayload(latestFan);
                boolean published = mqttService.publishMessage(topic, payload, 1);
                if (!published) {
                    throw new RuntimeException("保存成功，但下发MQTT设置失败");
                }
            }
        }

        // 使用新的规则控制电机（使用最新配置）
        motorControlProducerService.processMotorControl(latestFan != null ? latestFan : existFan, null, null);
        return ApiResponse.success("更新成功");
    }

    private Map<String, Object> buildMotorFanSettingPayload(MotorFan fan) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("wm", fan.getWm());

        // 0温控，1循环，2湿控，3氨气，4定时
        switch (fan.getWm()) {
            case 0: // 温控
                payload.put("tcps", fan.getTcps());
                payload.put("tcat", fan.getTcat());
                payload.put("tcot", fan.getTcot());
                payload.put("tcltrm", fan.getTcltrm());
                payload.put("tcltrs", fan.getTcltrs());
                payload.put("tcltpm", fan.getTcltpm());
                payload.put("tcltps", fan.getTcltps());
                payload.put("tctcm", fan.getTctcm());
                break;
            case 1: // 1循环
                payload.put("ccps", fan.getCcps());
                payload.put("cctu", fan.getCctu());
                payload.put("cctd", fan.getCctd());
                payload.put("ccrm", fan.getCcrm());
                payload.put("ccrs", fan.getCcrs());
                payload.put("ccpm", fan.getCcpm());
                payload.put("ccpss", fan.getCcpss());
                payload.put("cccm", fan.getCccm());
                break;
            case 2: // 湿控
                payload.put("hchu", fan.getHchu());
                payload.put("hchd", fan.getHchd());
                payload.put("hcrm", fan.getHcrm());
                payload.put("hcrs", fan.getHcrs());
                payload.put("hcpm", fan.getHcpm());
                payload.put("hcps", fan.getHcps());
                payload.put("hchcm", fan.getHchcm());
                break;
            case 3: // 氨气
                payload.put("ncnu", fan.getNcnu());
                payload.put("ncnd", fan.getNcnd());
                payload.put("ncrm", fan.getNcrm());
                payload.put("ncrs", fan.getNcrs());
                payload.put("ncpm", fan.getNcpm());
                payload.put("ncps", fan.getNcps());
                break;
            case 4: // 定时
                payload.put("tict1nf", fan.getTict1nf());
                payload.put("tict1nh", fan.getTict1nh());
                payload.put("tict1nm", fan.getTict1nm());
                payload.put("tict1fh", fan.getTict1fh());
                payload.put("tict1fm", fan.getTict1fm());

                payload.put("tict2nf", fan.getTict2nf());
                payload.put("tict2nh", fan.getTict2nh());
                payload.put("tict2nm", fan.getTict2nm());
                payload.put("tict2fh", fan.getTict2fh());
                payload.put("tict2fm", fan.getTict2fm());

                payload.put("tict3nf", fan.getTict3nf());
                payload.put("tict3nh", fan.getTict3nh());
                payload.put("tict3nm", fan.getTict3nm());
                payload.put("tict3fh", fan.getTict3fh());
                payload.put("tict3fm", fan.getTict3fm());

                payload.put("ticps", fan.getTicps());
                payload.put("ticat", fan.getTicat());
                payload.put("ticot", fan.getTicot());
                payload.put("tictitm", fan.getTictitm());
                break;

            default:
                break;
        }

        return payload;
    }

    /**
     * 删除风机
     * DELETE /motor-fan/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteMotorFan(@PathVariable Long id) {
        MotorFan existFan = motorFanService.findById(id);
        if (existFan == null) {
            throw new RuntimeException("风机不存在");
        }

        motorFanService.deleteById(id);
        return ApiResponse.success("风机删除成功");
    }
}