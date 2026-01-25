


这是一个养殖场智能硬件控制的项目。

dspace 是前端兼容微信小程序和H5，
iot_server 是springboot 开发的后端服务器
数据库表结构和数据：sql/iot_system.sql

核心逻辑：
有N个温控仪，每个温控仪链接4个温度传感器、10个电机、2个变频电机。
温控仪可以向服务器发送 mqtt 消息。mqtt消息的格式示例为：
topic:  device/d002
message:
{
	id:d002,
    ts1: 22,
    ts2: 22,
    ts3: 22,
    ts4: 22,
    mt1: 1,
    mt2: 0,
    mt3: 1,
    mt4: 0,
    mt5: 1,
    mt6: 0,
    mt7: 1,
    mt8: 0,
    mt9: 1,
    mt10: 0,
    imt1: 40,
    imt2: 60,
}

说明：
id：当前温控仪的ID
ts1: 温度传感器1 的数值
mt1: 电机1的运行状态，1 运行 0 停止
imt1: 变频电机1的动行状态，从 0% 到 100% 最大功率运行

现有的功能都可以正常运行，现添加新功能：

收到mqtt消息以后，在MqttService.java中批量更新电机运行状态，之后，还要根据规则，管理延时消息控制电机的开关状态。
现在先做
第一步： 添加延时消息对列RabbitMQ
第二步：读取数据库从表 motor_fan 中获得相关电机的的配置信息，
1、先根据auto_mode 来确定 这个电机手工控制还是自动控制
2、如果是自动控制，再根据control_mode 来选择控制模式，根据模式规则对电机进行控制



# 在 docker 中创建 rabbitmq 的命令

docker run -d   --name rabbitmq   -p 5672:5672   -p 15672:15672   -e RABBITMQ_DEFAULT_USER=admin   -e RABBITMQ_DEFAULT_PASS=admin123   rabbitmq:3.9-management


# EMQX 
页面登录地址：
http://192.168.56.128:18083/
admin
test123123