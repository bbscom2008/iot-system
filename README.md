# 温控仪智能设备控制系统
> 温控仪和服务器之间传输数据采用 mqtt 协议。


## mqtt服务器地址：
ws://121.41.131.103:8083/mqtt
mqtt://121.41.131.103:1883

**登陆MQTT服务器时用户名为 STM32芯片ID，密码是 4G模块ID**
```
username: {STM32ID}
password: {IMEI}
```


## 1- 查询设备状态 （暂无）
> 说明：服务器添加设备后，发送该消息查询状态，设备收到后上报当前设备的状态。

主题:   `device/query-status/{STM32ID}`
数据格式：
```
QUERY_DEVICE_STATUS
```


## 2- 温控仪数据上报
主题:   `device/report/{STM32ID}`
数据格式：
```json
{
    "STM32ID": "464B21320F3936313536374D",
    "IMEI": "864814078766416",
    "ICCID": "89860124801774637014",
    "ts1": 48,
    "ts2": 33,
    "ts3": 32,
    "ts4": 22,
    "mt1": 1,
    "mt2": 1,
    "mt3": 0,
    "mt4": 0,
    "mt5": 0,
    "mt6": 0,
    "mt7": 0,
    "mt8": 0,
    "mt9": 1,
    "mt10": 0,
    "imt1": 40,
    "imt2": 60
}
```
> STM32ID: STM32单片机ID
> IMEI: 是4G模块的中的维一识别码。
> ICCID: IC卡的唯一ID
> ts1 - ts4 ：是 4个温度传感器，如果没有可以不传。
> mt1 - mt10 ：是10个电机，1 运行  0 停止
> imt1 - imt2 ： 是2个变频电机 数值是 0 - 100 
> power : 电量 0 - 100 的值
> signal : 信号强度 ，具体值是什么？ 1 - 10 还是多少？

## 3- 风机详情设置
主题 Topic
设备to服务器 ：device/report/{STM32ID}/{mtx}
服务器to设备 ：server/setting/{STM32ID}/{mtx}
> {STM32ID} : 芯片ID
> {mtx} : 设备mt1-mt10
> 如：
>  
>  device/report/464B21320F3936313536374D/mt1
>  
>  server/setting/464B21320F3936313536374D/mt2

数据格式：
```
{
  "wm": 0,
  "tcps": 7,
  "tcat": 25.0,
  "tcot": 30.0,
  "tcltrm": 0,
  "tcltrs": 0,
  "tcltpm": 0,
  "tcltps": 0,
  "tctcm": 1,
  "ccps": 7,
  "cctu": 30.0,
  "cctd": 20.0,
  "ccrm": 0,
  "ccrs": 0,
  "ccpm": 0,
  "ccpss": 0,
  "cccm": 0,
  "hchu": 700,
  "hchd": 400,
  "hcrm": 0,
  "hcrs": 0,
  "hcpm": 0,
  "hcps": 0,
  "hchcm": 0,
  "ncnu": 35,
  "ncnd": 10,
  "ncrm": 0,
  "ncrs": 0,
  "ncpm": 0,
  "ncps": 0,
  "tict1nf": 0,
  "tict1nh": 8,
  "tict1nm": 0,
  "tict1fh": 10,
  "tict1fm": 0,
  "tict2nf": 0,
  "tict2nh": 12,
  "tict2nm": 0,
  "tict2fh": 14,
  "tict2fm": 0,
  "tict3nf": 0,
  "tict3nh": 16,
  "tict3nm": 0,
  "tict3fh": 20,
  "tict3fm": 0,
  "ticps": 0,
  "ticat": 30.0,
  "ticot": 40.0,
  "tictitm": 1
}

```


> wm:工作模式，0温控，1循环，2湿控，3氨气，4定时
> tcps:温控探头选择，0探头1，1探头2，2探头3，3探头4，4探头12，5探头34，6探头1234，7探头1234
> tcat:温控启动温度
> tcot:温控停止温度
> tcltrm:温控低温运行时间-分
> tcltrs:温控低温运行时间-秒
> tcltpm:温控低温暂停时间-分
> tcltps:温控低温暂停时间-秒
> tctcm:温控模式0降温1升温
> ccps:循环探头选择，0探头1，1探头2，2探头3，3探头4，4探头12，5探头34，6探头1234，7探头1234
> cctu:循环温度上限
> cctd:循环温度下限
> ccrm:循环运行时间-分
> ccrs:循环运行时间-秒
> ccpm:循环暂停时间-分
> ccps:循环暂停时间-秒
> cccm:循环模式0降温1升温2时间
> 
> hchu:湿控湿度上限
> hchd:湿控湿度下限
> hcrm:湿控运行时间-分
> hcrs:湿控运行时间-秒
> hcpm:湿控暂停时间-分
> hcps:湿控暂停时间-秒
> hchcm:湿控模式0除湿1加湿
> 
> ncnu:nh3上限
> ncnd:nh3下限
> ncrm:nh3运行时间-分
> ncrs:nh3运行时间-秒
> ncpm:nh3暂停时间-分
> ncps:nh3暂停时间-秒
> 
> tict1nf:定时1，0开1关
> tict1nh:定时1开-时
> tict1nm:定时1开-分
> tict1fh:定时1关-时
> tict1fm:定时1关-分
> 
> tict2nf:定时2，0开1关
> tict2nh:定时2开-时
> tict2nm:定时2开-分
> tict2fh:定时2关-时
> tict2fm:定时2关-分
> tict3nf:定时3，0开1关
> tict3nh:定时3开-时
> tict3nm:定时3开-分
> tict3fh:定时3关-时
> tict3fm:定时3关-分
> ticps:定时探头选择，0探头1，1探头2，2探头3，3探头4，4探头12，5探头34，6探头1234，7探头1234
> ticat:定时启动温度
> ticot:定时停止温度
> tictitm:定时温控模式0降温1升温

## 变频详情设置

主题 Topic
设备to服务器 ：device/report/{STM32ID}/{imtx}
服务器to设备 ：server/setting/{STM32ID}/{imtx}

> {STM32ID} : 芯片ID
> {imtx} : 设备imt1-imt2
> 
> 如：
>  device/report/464B21320F3936313536374D/mt1
>  server/setting/464B21320F3936313536374D/mt2


数据格式：
```
{
  "fcm": 0,
  "ms": 30,
  "mrtm": 2,
  "mrts": 0,
  "mptm": 0,
  "mpts": 0,
  "atps": 7,
  "atls": 20,
  "atul": 40.0,
  "atdl": 30.0,
  "aswt": 20.0,
  "atrtm": 0,
  "atrts": 0,
  "atptm": 0,
  "atpts": 0,
  "ahls": 10,
  "ahul": 900,
  "ahdl": 500,
  "ahrtm": 0,
  "ahrts": 0,
  "ahptm": 0,
  "ahpts": 0,
  "anls": 30,
  "anul": 70,
  "andl": 40,
  "anrtm": 0,
  "anrts": 5,
  "anptm": 0,
  "anpts": 5
}

```
> 说明：
> {STM32ID} 是 芯片STM32的ID
> {imtx} 是变频器1-2，目前只有2 个变频器
> fcm : 变频模式选择：0手动，1自动温控，2自动湿控，3自动氨气
> ms : 手动转速
> mrtm : 手动运行时间-分
> mrts : 手动运行时间-秒
> mptm : 手动暂停时间-分
> mpts : 手动暂停时间-秒
> 
> atps : 自动温控探头选择，探头选择 0探头1,1探头2,2探头3,3探头4,4探头12,5探头34,6探头1234
> atls : 自动温控最低转速
> atul : 自动温控温度上限
> atdl : 自动温控温度下限
> aswt : 自动温控停止工作温度
> atrtm : 自动温控运行时间-分
> atrts : 自动温控运行时间-秒
> atptm : 自动温控暂停时间-分
> atpts : 自动温控暂停时间-秒
> 
> ahls : 自动湿控最低转速
> ahul : 自动湿控湿度上限
> ahdl : 自动湿控湿度下限
> ahrtm : 自动湿控运行时间-分
> ahrts : 自动湿控运行时间-秒
> ahptm : 自动湿控暂停时间-分
> ahpts : 自动湿控暂停时间-秒
> 
> anls : 自动nh3最低转速
> anul : nh3上限
> andl : nh3下限
> anrtm : 自动nh3运行时间-分
> anrts : 自动nh3运行时间-秒
> anptm : 自动nh3暂停时间-分
> anpts : 自动nh3暂停时间-秒

