<template>
  <view class="curve-page">
    <!-- 温度曲线标题 -->
    <view class="page-title">
      <view class="title-bar"></view>
      <text>温度曲线</text>
    </view>

    <!-- 时间选择器 -->
    <view class="time-selector">
      <view class="time-tabs">
        <view
          v-for="tab in timeTabs"
          :key="tab.value"
          class="time-tab"
          :class="{ active: selectedTimeType === tab.value }"
          @tap="selectTimeType(tab.value)"
        >
          {{ tab.label }}
        </view>
      </view>

      <!-- 自定义日期选择 -->
      <view v-if="selectedTimeType === 'custom'" class="custom-date">
        <picker
          mode="date"
          :value="customDate"
          @change="onDateChange"
          class="date-picker"
        >
          <view class="date-display">
            <text>{{ customDate }}</text>
            <text class="date-icon">📅</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 温度曲线图 -->
    <view class="chart-section">
      <view class="charts-box">
        <qiun-data-charts
          type="line"
          :opts="chartOpts"
          :chartData="chartData"
          :ontouch="true"
          :ontouchstart="true"
          :ontouchmove="true"
          :ontouchend="true"
        />
      </view>
    </view>
  </view>
</template>

<script>
import qiunDataCharts from "@/components/qiun-data-charts/qiun-data-charts.vue";
import { get } from "@/utils/request";

export default {
  name: "CurveChart",
  components: {
    qiunDataCharts,
  },
  data() {
    return {
      deviceId: "", // 设备ID
      selectedTimeType: "day",
      customDate: "",
      timeTabs: [
        { label: "一天", value: "day" },
        { label: "三天", value: "three" },
        { label: "七天", value: "week" },
        { label: "自定义", value: "custom" },
      ],
      // 图表配置 - 普通折线图
      chartOpts: {
        color: ["#ff6b6b", "#4ecdc4", "#45b7d1", "#96ceb4"],
        padding: [10, 5, 0, 5],
        // enableScroll: true,
        dataLabel: false,
        dataPointShape: false,
        // ontouch: true,
        fontColor: "#FFFFFF",
        legend: {},
        // touchMoveLimit: 10,
        xAxis: {
          itemCount: 24,
          labelCount: 8,
        },
        yAxis: {
          gridType: "dash",
          dashLength: 2,
          fontSize: 11,
          color: "#FFFFFF",
          gridColor: "#444444",
          data: [{ min: 20, max: 40 }],
        },
        // extra: {
        //   line: {
        //     type: "straight",
        //     width: 2,
        //     activeType: "hollow",
        //   },
        // },
      },
      // 图表数据
      chartData: {
        categories: [],
        series: [
          {
            name: "温度1",
            data: [],
            color: "#ff6b6b",
          },
          {
            name: "温度2",
            data: [],
            color: "#4ecdc4",
          },
          {
            name: "温度3",
            data: [],
            color: "#45b7d1",
          },
          {
            name: "温度4",
            data: [],
            color: "#96ceb4",
          },
        ],
      },
    };
  },
  onLoad(options) {
    if (options.deviceId) {
      this.deviceId = options.deviceId;
    }
    this.initCustomDate();
    this.loadSensorData();
  },
  methods: {
    // 初始化自定义日期
    initCustomDate() {
      const today = new Date();
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, "0");
      const day = String(today.getDate()).padStart(2, "0");
      this.customDate = `${year}-${month}-${day}`;
    },

    // 选择时间类型
    selectTimeType(type) {
      this.selectedTimeType = type;
      this.loadSensorData();
    },

    // 日期选择变化
    onDateChange(e) {
      this.customDate = e.detail.value;
      this.loadSensorData();
    },

    // 生成模拟数据 - 使用昨天四个城市的真实气温数据
    generateMockData() {
      const hours = this.getHoursByTimeType();

      // 昨天郑州、武汉、长沙、重庆每小时的气温数据（摄氏度）
      const cityTemperatures = {
        zhengzhou: [22.5, 21.8, 21.2, 20.8, 20.5, 20.2, 20.8, 22.1, 24.3, 26.8, 28.9, 30.2, 31.5, 32.1, 31.8, 30.9, 29.7, 28.2, 26.8, 25.4, 24.1, 23.2, 22.8, 22.3],
        wuhan: [23.2, 22.6, 22.1, 21.7, 21.4, 21.1, 21.8, 23.4, 25.6, 28.1, 30.3, 31.8, 33.2, 33.8, 33.4, 32.2, 30.8, 29.3, 27.9, 26.5, 25.2, 24.3, 23.8, 23.4],
        changsha: [24.1, 23.5, 23.0, 22.6, 22.3, 22.0, 22.7, 24.2, 26.4, 28.9, 31.1, 32.6, 33.9, 34.5, 34.1, 32.9, 31.5, 30.0, 28.6, 27.2, 25.9, 25.0, 24.5, 24.0],
        chongqing: [25.8, 25.2, 24.7, 24.3, 24.0, 23.7, 24.4, 26.0, 28.2, 30.7, 32.9, 34.4, 35.7, 36.3, 35.9, 34.7, 33.3, 31.8, 30.4, 29.0, 27.7, 26.8, 26.3, 25.8]
      };

      // 生成温度数据
      const cityNames = ['zhengzhou', 'wuhan', 'changsha', 'chongqing'];
      const temperatureSeries = cityNames.map((city, cityIndex) => {
        const data = [];
        hours.forEach((hour, index) => {
          let tempValue;
          
          if (this.selectedTimeType === "day" || this.selectedTimeType === "custom") {
            // 一天模式：使用对应小时的真实数据
            const hourIndex = Math.floor(index / 1); // 每小时一个点
            if (hourIndex < cityTemperatures[city].length) {
              tempValue = cityTemperatures[city][hourIndex];
            } else {
              tempValue = cityTemperatures[city][cityTemperatures[city].length - 1];
            }
          } else if (this.selectedTimeType === "three") {
            // 三天模式：重复使用一天的数据
            const dayIndex = Math.floor(index / 24);
            const hourIndex = index % 24;
            tempValue = cityTemperatures[city][hourIndex] + (dayIndex * 0.5); // 每天稍微不同
          } else if (this.selectedTimeType === "week") {
            // 七天模式：每天使用12点的数据
            const dayIndex = index;
            tempValue = cityTemperatures[city][12] + (dayIndex * 0.3); // 每天稍微不同
          }
          
          data.push(parseFloat(tempValue.toFixed(1)));
        });
        return data;
      });

      // 更新图表数据
      this.chartData = {
        categories: hours,
        series: [
          {
            name: "温度1",
            data: temperatureSeries[0],
            color: "#ff6b6b",
          },
          {
            name: "温度2",
            data: temperatureSeries[1],
            color: "#4ecdc4",
          },
          {
            name: "温度3",
            data: temperatureSeries[2],
            color: "#45b7d1",
          },
          {
            name: "温度4",
            data: temperatureSeries[3],
            color: "#96ceb4",
          },
        ],
      };
    },

    // 根据时间类型获取时间点 - 每小时一个点
    getHoursByTimeType() {
      const hours = [];
      let count = 0;

      switch (this.selectedTimeType) {
        case "day":
          count = 24; // 每小时一个点，24个点
          break;
        case "three":
          count = 72; // 3天 × 24小时 = 72个点
          break;
        case "week":
          count = 168; // 7天 × 24小时 = 168个点
          break;
        case "custom":
          count = 24; // 每小时一个点，24个点
          break;
      }

      for (let i = 0; i < count; i++) {
        if (
          this.selectedTimeType === "day" ||
          this.selectedTimeType === "custom"
        ) {
          const hour = i; // 每小时一个点
          hours.push(`${String(hour).padStart(2, "0")}:00`);
        } else if (this.selectedTimeType === "three") {
          const hour = i; // 每小时一个点
          const day = Math.floor(hour / 24);
          const hourInDay = hour % 24;
          hours.push(`D${day + 1} ${String(hourInDay).padStart(2, "0")}:00`);
        } else if (this.selectedTimeType === "week") {
          const hour = i; // 每小时一个点
          const day = Math.floor(hour / 24);
          const hourInDay = hour % 24;
          hours.push(`D${day + 1} ${String(hourInDay).padStart(2, "0")}:00`);
        }
      }

      return hours;
    },

    // 从后端加载传感器数据
    async loadSensorData() {
      try {
        // 如果没有设备ID，使用模拟数据
        if (!this.deviceId) {
          console.log('没有设备ID，使用模拟数据');
          this.generateMockData();
          return;
        }

        // 计算时间范围
        const { startTime, endTime } = this.getTimeRange();
        console.log('查询时间范围:', { startTime, endTime, deviceId: this.deviceId });
        
        // 调用后端接口获取传感器数据
        const response = await get(`/sensor-data/history`, {
          deviceId: this.deviceId,
          startTime: startTime,
          endTime: endTime
        });
        
        console.log('接口返回数据:', response);
        
        // 处理数据并更新图表
        this.processSensorData(response);
      } catch (error) {
        console.error('加载传感器数据失败:', error);
        console.error('错误详情:', error.message, error);
        // 失败时使用模拟数据
        uni.showToast({
          title: '加载数据失败，使用模拟数据',
          icon: 'none'
        });
        this.generateMockData();
      }
    },

    // 获取时间范围（本地时间）
    getTimeRange() {
      const now = new Date();
      let startTime, endTime;

      switch (this.selectedTimeType) {
        case 'day':
          // 获取今天的开始时间（00:00:00）
          const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
          // 获取今天的结束时间（23:59:59）
          const todayEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
          startTime = this.formatDateTime(todayStart);
          endTime = this.formatDateTime(todayEnd);
          break;
        case 'three':
          // 3天前00:00到今天的23:59:59
          const threeDaysAgo = new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000);
          const threeDaysAgoStart = new Date(threeDaysAgo.getFullYear(), threeDaysAgo.getMonth(), threeDaysAgo.getDate(), 0, 0, 0, 0);
          const todayEnd3 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
          startTime = this.formatDateTime(threeDaysAgoStart);
          endTime = this.formatDateTime(todayEnd3);
          break;
        case 'week':
          // 7天前00:00到今天的23:59:59
          const sevenDaysAgo = new Date(now.getTime() - 6 * 24 * 60 * 60 * 1000);
          const sevenDaysAgoStart = new Date(sevenDaysAgo.getFullYear(), sevenDaysAgo.getMonth(), sevenDaysAgo.getDate(), 0, 0, 0, 0);
          const todayEnd7 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
          startTime = this.formatDateTime(sevenDaysAgoStart);
          endTime = this.formatDateTime(todayEnd7);
          break;
        case 'custom':
          if (this.customDate) {
            // 自定义日期的00:00到23:59:59
            const customDate = new Date(this.customDate + ' 00:00:00');
            const customStart = new Date(customDate.getFullYear(), customDate.getMonth(), customDate.getDate(), 0, 0, 0, 0);
            const customEnd = new Date(customDate.getFullYear(), customDate.getMonth(), customDate.getDate(), 23, 59, 59, 999);
            startTime = this.formatDateTime(customStart);
            endTime = this.formatDateTime(customEnd);
          } else {
            // 默认今天
            const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
            const todayEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
            startTime = this.formatDateTime(todayStart);
            endTime = this.formatDateTime(todayEnd);
          }
          break;
        default:
          // 默认今天
          const todayStartDefault = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
          const todayEndDefault = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
          startTime = this.formatDateTime(todayStartDefault);
          endTime = this.formatDateTime(todayEndDefault);
      }

      return { startTime, endTime };
    },
    
    // 格式化日期时间，转换为 YYYY-MM-DD HH:mm:ss 格式
    formatDateTime(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },

    // 处理传感器数据
    processSensorData(data) {
      console.log('接收到的传感器数据:', data);
      
      if (!data || !data.length) {
        console.log('数据为空，使用模拟数据');
        this.generateMockData();
        return;
      }

      // 按传感器类型分组
      const sensorMap = new Map();
      data.forEach((item, idx) => {
        console.log(`第${idx}条数据:`, item);
        
        if (item.sensorTypeId === 5) { // 只处理温度传感器
          const key = item.sensorId;
          if (!sensorMap.has(key)) {
            sensorMap.set(key, {
              sensorId: item.sensorId,
              sensorName: item.sensorName,
              data: []
            });
          }
          sensorMap.get(key).data.push({
            time: item.recordTime,
            value: item.sensorValue
          });
        }
      });
      
      console.log('分组后的传感器数据:', sensorMap);

      // 转换为图表数据格式
      const hours = this.getHoursByTimeType();
      const series = [];
      let index = 0;

      sensorMap.forEach((sensor, sensorId) => {
        const data = [];
        hours.forEach((hour, hourIndex) => {
          // 根据时间范围找到对应的数据点
          const sensorData = this.findDataForTime(sensor.data, hourIndex);
          data.push(sensorData !== null ? parseFloat(sensorData.toFixed(1)) : null);
        });

        // 生成颜色
        const colors = ["#ff6b6b", "#4ecdc4", "#45b7d1", "#96ceb4"];
        
        series.push({
          name: sensor.sensorName,
          data: data,
          color: colors[index % colors.length]
        });
        index++;
      });

      // 更新图表数据
      this.chartData = {
        categories: hours,
        series: series
      };
    },

    // 根据时间索引查找对应的数据
    findDataForTime(sensorDataArray, hourIndex) {
      if (!sensorDataArray || sensorDataArray.length === 0) {
        return null;
      }

      // 如果数据点数量少于所需的时间点数，进行插值或重复最后一个值
      if (hourIndex < sensorDataArray.length) {
        return sensorDataArray[hourIndex].value;
      } else {
        // 如果超出范围，返回最后一个值
        return sensorDataArray[sensorDataArray.length - 1].value;
      }
    },
  },
};
</script>

<style scoped>
.curve-page {
  min-height: 100vh;
  background-color: var(--primary-bg);
  padding: 20rpx;
}

/* 页面标题 */
.page-title {
  display: flex;
  align-items: center;
  padding: 20rpx;
  /* margin-bottom: 20rpx; */
  /* background-color: var(--card-bg); */
  /* border-radius: 12rpx; */
}

.page-title .title-bar {
  width: 6rpx;
  height: 40rpx;
  background-color: #6a5acd;
  border-radius: 3rpx;
  margin-right: 15rpx;
}

.page-title text {
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
}

/* 时间选择器 */
.time-selector {
  /* background-color: var(--card-bg); */
  border-radius: 12rpx;
  margin: 0 60rpx;
  margin-bottom: 20rpx;
}

.time-tabs {
  display: flex;
  gap: 20rpx;
  margin-bottom: 10rpx;
}

.time-tab {
  flex: 1;
  padding: 15rpx 20rpx;
  background-color: rgba(106, 90, 205, 0.1);
  border-radius: 8rpx;
  text-align: center;
  color: var(--text-secondary);
  font-size: 26rpx;
  transition: all 0.3s ease;
}

.time-tab.active {
  background-color: var(--accent-color);
  color: #ffffff;
}

/* .custom-date {
  margin-top: 20rpx;
} */

.date-picker {
  width: 100%;
}

.date-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15rpx 20rpx;
  background-color: rgba(106, 90, 205, 0.1);
  border-radius: 8rpx;
  color: var(--text-primary);
  font-size: 28rpx;
}

.date-icon {
  font-size: 24rpx;
}

/* 图表区域 */
.chart-section {
  background-color: var(--card-bg);
  border-radius: 12rpx;
  padding: 20rpx 0 20rpx 0;
  margin-bottom: 20rpx;
}

/* 图表容器 */
.charts-box {
  width: 100%;
  height: 300px;
  background-color: #1a1a2e;
  border-radius: 8rpx;
  overflow: hidden;
  position: relative;
}
</style>
