<template>
  <view class="svg-icon" :class="iconClass" :style="iconStyle">
    <!-- 设备 -->
    <view v-if="name === 'devices'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <rect width="14" height="20" x="5" y="2" rx="2" ry="2"></rect>
        <path d="M12 18h.01"></path>
      </svg>
    </view>


     <!-- 气体 -->
    <view v-else-if="name === 'timer'" class="icon-content">
      <svg version="1.1" id="Capa_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
      :fill="fill"
        :stroke="color"

	 width="485.213px" height="485.212px" viewBox="0 0 485.213 485.212" style="enable-background:new 0 0 485.213 485.212;"
	 xml:space="preserve">
<g>
	<path d="M242.607,0C108.629,0,0.001,108.628,0.001,242.606c0,133.976,108.628,242.606,242.606,242.606
		c133.978,0,242.604-108.631,242.604-242.606C485.212,108.628,376.585,0,242.607,0z M257.771,454.116v-29.557h-30.327v29.557
		c-104.925-7.458-188.883-91.419-196.346-196.344h29.554v-30.327H31.098c7.463-104.928,91.421-188.886,196.346-196.349v29.554
		h30.327V31.097c104.926,7.462,188.882,91.421,196.344,196.349h-29.552v30.327h29.552
		C446.652,362.697,362.696,446.658,257.771,454.116z M354.226,178.163c2.072,3.615,0.83,8.266-2.782,10.368l-80.092,46.227
		c0.691,2.577,1.582,5.066,1.582,7.848c0,16.762-13.581,30.324-30.326,30.324c-16.75,0-30.327-13.562-30.327-30.324l-82.299-47.503
		c-7.257-4.203-9.743-13.474-5.554-20.729c4.174-7.285,13.459-9.771,20.715-5.565l82.775,47.798
		c4.411-2.487,9.239-4.324,14.69-4.324c8.471,0,16.092,3.494,21.604,9.122l79.648-46.024
		C347.416,173.277,352.064,174.521,354.226,178.163z"/>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
<g>
</g>
</svg>

    </view>


    <!-- 气体 -->
    <view v-else-if="name === 'recycle'" class="icon-content">
      <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" id="Layer_2" 
      width="512px" height="512px" viewBox="0 0 512 512" :fill="fill"
        :stroke="color"
      enable-background="new 0 0 512 512" xml:space="preserve"><g>
        <path  d="M223.151,159.114l-38.806-38.724c21.75-11.384,45.884-17.32,71.004-17.32   c84.594,0,153.417,68.676,153.417,153.091H486c0-61.478-23.992-119.277-67.556-162.749S316.958,26,255.349,26   c-45.859,0-89.552,13.167-127.306,38.208L93.421,29.659L45.936,206.498L223.151,159.114z"/>
        <path  d="M288.849,352.886l38.806,38.724c-21.75,11.384-45.884,17.32-71.004,17.32   c-84.594,0-153.417-68.676-153.417-153.092H26c0,61.478,23.992,119.277,67.556,162.749C137.12,462.059,195.042,486,256.651,486   c45.859,0,89.552-13.167,127.306-38.208l34.623,34.549l47.484-176.839L288.849,352.886z"/></g></svg>
    </view>

    <!-- 气体 -->
    <view v-else-if="name === 'gas'" class="icon-content">
      <svg
        version="1.1"
        id="Capa_1"
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        x="0px"
        y="0px"
        :fill="fill"
        :stroke="color"
        viewBox="0 0 33.062 33.062"
        style="enable-background: new 0 0 33.062 33.062"
        xml:space="preserve"
      >
        <g>
          <path
            d="M25.615,26.608H6.442C2.89,26.608,0,23.719,0,20.167c0-3.502,2.81-6.361,6.294-6.44c1.241-4.303,5.161-7.274,9.67-7.274
		c3.738,0,7.104,2.026,8.869,5.311c0.28-0.031,0.534-0.046,0.781-0.046c4.106,0,7.446,3.339,7.446,7.443
		C33.062,23.268,29.721,26.608,25.615,26.608z M6.442,14.726C3.441,14.726,1,17.167,1,20.167c0,3,2.441,5.44,5.442,5.44h19.173
		c3.555,0,6.446-2.892,6.446-6.446c0-3.553-2.892-6.443-6.446-6.443c-0.298,0-0.613,0.026-0.991,0.083
		c-0.214,0.032-0.426-0.079-0.522-0.273c-1.544-3.129-4.663-5.074-8.138-5.074c-4.184,0-7.804,2.839-8.804,6.904
		c-0.056,0.228-0.275,0.377-0.498,0.38L6.442,14.726z"
          />
          <path
            d="M25.615,26.608c-0.276,0-0.5-0.224-0.5-0.5s0.224-0.5,0.5-0.5c3.555,0,6.446-2.892,6.446-6.446
		c0-3.553-2.892-6.443-6.446-6.443c-1.862,0-3.633,0.805-4.858,2.208c-0.181,0.208-0.497,0.23-0.705,0.048
		c-0.208-0.181-0.229-0.497-0.048-0.705c1.414-1.621,3.459-2.55,5.611-2.55c4.106,0,7.446,3.339,7.446,7.443
		C33.062,23.268,29.721,26.608,25.615,26.608z"
          />
        </g>
      </svg>
    </view>

    <!-- 湿度计 -->
    <view v-else-if="name === 'humidity'" class="icon-content">
      <svg
        style="enable-background: new 0 0 64 64"
        version="1.1"
        viewBox="0 0 64 64"
        :fill="fill"
        :stroke="color"
        xml:space="preserve"
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
      >
        <g id="cloudy_sunny" />
        <g id="bright" />
        <g id="cloudy" />
        <g id="high_rainfall" />
        <g id="windy" />
        <g id="rain_with_thunder" />
        <g id="clear_night" />
        <g id="cloudy_night" />
        <g id="moon" />
        <g id="sun" />
        <g id="rainy_night" />
        <g id="windy_night" />
        <g id="night_rain_thunder" />
        <g id="windy_rain" />
        <g id="temperature" />
        <g id="humidity">
          <g>
            <path
              d="M49.7,35.9C47.3,21.2,29.5,4,28.7,3.3c-0.4-0.4-1-0.4-1.4,0C26.4,4.1,6,23.7,6,39c0,12.1,9.9,22,22,22    c3.4,0,6.7-0.8,9.7-2.3c2.1,1.4,4.6,2.3,7.3,2.3c7.2,0,13-5.8,13-13C58,42.5,54.6,37.8,49.7,35.9z M28,59C17,59,8,50,8,39    C8,26.1,24.4,9,28,5.4C31.3,8.7,45,23,47.6,35.3C46.7,35.1,45.9,35,45,35c-7.2,0-13,5.8-13,13c0,3.7,1.5,7,4,9.3    C33.5,58.4,30.8,59,28,59z M45,59c-6.1,0-11-4.9-11-11s4.9-11,11-11s11,4.9,11,11S51.1,59,45,59z"
            />
            <path
              d="M28,54c-8.3,0-15-6.7-15-15c0-0.6-0.4-1-1-1s-1,0.4-1,1c0,9.4,7.6,17,17,17c0.6,0,1-0.4,1-1S28.6,54,28,54z"
            />
            <path
              d="M48.4,40.1c-0.5-0.2-1.1,0-1.3,0.5l-6,14c-0.2,0.5,0,1.1,0.5,1.3C41.7,56,41.9,56,42,56c0.4,0,0.8-0.2,0.9-0.6l6-14    C49.1,40.9,48.9,40.3,48.4,40.1z"
            />
            <path
              d="M44,44c0-1.7-1.3-3-3-3s-3,1.3-3,3s1.3,3,3,3S44,45.7,44,44z M40,44c0-0.6,0.4-1,1-1s1,0.4,1,1s-0.4,1-1,1S40,44.6,40,44z    "
            />
            <path
              d="M49,49c-1.7,0-3,1.3-3,3s1.3,3,3,3s3-1.3,3-3S50.7,49,49,49z M49,53c-0.6,0-1-0.4-1-1s0.4-1,1-1s1,0.4,1,1S49.6,53,49,53z    "
            />
          </g>
        </g>
        <g id="air_pressure" />
        <g id="low_rainfall" />
        <g id="moderate_rainfall" />
        <g id="Sunset" />
      </svg>
    </view>

    <!-- 温度计 -->
    <view v-else-if="name === 'temperature'" class="icon-content">
      <svg
        width="30px"
        height="65px"
        viewBox="0 0 30 65"
        :fill="fill"
        :stroke="color"
        version="1.1"
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:sketch="http://www.bohemiancoding.com/sketch/ns"
      >
        <title>Temperature</title>
        <desc>Created with Sketch.</desc>
        <defs></defs>
        <g
          id="Page-1"
          :stroke="color"
          stroke-width="2"
          :fill="color"
          fill-rule="evenodd"
          sketch:type="MSPage"
        >
          <g
            id="Temperature"
            sketch:type="MSLayerGroup"
            transform="translate(0.000000, 2.000000)"
          >
            <path
              d="M22.1,42.7 L22.1,6.8 C22.1,2.1 19.1,-0.1 15.2,-0.1 C11.2,-0.1 8.2,2.1 8.2,6.8 L8.2,42.6 C5.8,44.6 4.1,47.8 4.1,51.2 C4.1,57.3 9.2,61.9 15.3,61.9 C21.4,61.9 26.2,57.3 26.2,51.2 C26.1,48 24.4,44.7 22.1,42.7 L22.1,42.7 Z"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M15.1,7 L15.1,41.8"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M21.4,47.9 C21.9,48.8 22.1,49.9 22.1,51 C22.1,54.8 19,58 15.1,58"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M25,9.9 L30,9.9"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M25,16.9 L28,16.9"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M25,23.9 L30,23.9"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M25,29.9 L28,29.9"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M25,36.9 L30,36.9"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M0,9.9 L5,9.9"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M2,16.9 L5,16.9"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M0,23.9 L5,23.9"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M2,29.9 L5,29.9"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
            <path
              d="M0,36.9 L5,36.9"
              id="Shape"
              sketch:type="MSShapeGroup"
            ></path>
          </g>
        </g>
      </svg>
    </view>

    <!-- 风扇 -->
    <view v-else-if="name === 'fan'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="32"
        height="32"
        viewBox="0 0 32 32"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path
          d="M23 28c-2.757 0-5-2.243-5-5v-3h-4v3c0 2.757-2.243 5-5 5s-5-2.243-5-5 2.243-5 5-5h3v-4h-3c-2.757 0-5-2.243-5-5s2.243-5 5-5 5 2.243 5 5v3h4v-3c0-2.757 2.243-5 5-5s5 2.243 5 5-2.243 5-5 5h-3v4h3c2.757 0 5 2.243 5 5s-2.243 5-5 5zM20 20v3c0 1.654 1.346 3 3 3s3-1.346 3-3-1.346-3-3-3h-3zM9 20c-1.654 0-3 1.346-3 3s1.346 3 3 3 3-1.346 3-3v-3h-3zM14 18h4v-4h-4v4zM20 12h3c1.654 0 3-1.346 3-3s-1.346-3-3-3-3 1.346-3 3v3zM9 6c-1.654 0-3 1.346-3 3s1.346 3 3 3h3v-3c0-1.654-1.346-3-3-3z"
        ></path>
      </svg>
    </view>

    <!-- WIFI 信号 -->
    <view v-else-if="name === 'wifi'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M12 20h.01"></path>
        <path d="M2 8.82a15 15 0 0 1 20 0"></path>
        <path d="M5 12.859a10 10 0 0 1 14 0"></path>
        <path d="M8.5 16.429a5 5 0 0 1 7 0"></path>
      </svg>
    </view>

    <!-- 报警 -->
    <view v-else-if="name === 'alarm'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path
          d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"
        ></path>
        <path d="M12 9v4"></path>
        <path d="M12 17h.01"></path>
      </svg>
    </view>

    <!-- 首页 -->
    <view v-else-if="name === 'home'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8"></path>
        <path
          d="M3 10a2 2 0 0 1 .709-1.528l7-6a2 2 0 0 1 2.582 0l7 6A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"
        ></path>
      </svg>
    </view>

    <!-- 电量 -->
    <view v-else-if="name === 'power-empty'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M 22 14 L 22 10"></path>
        <rect x="2" y="6" width="16" height="12" rx="2"></rect>
      </svg>
    </view>

    <!-- 电量充足 -->
    <view v-else-if="name === 'power-full'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <!-- 电池正极 -->
        <path d="M 22 14 L 22 10"></path>
        <!-- 电池主体 -->
        <rect x="2" y="6" width="16" height="12" rx="2"></rect>
        <!-- 电量指示条 - 四条竖线表示电量充足 -->
        <rect x="4" y="8" width="2" height="8" :fill="color"></rect>
        <rect x="7" y="8" width="2" height="8" :fill="color"></rect>
        <rect x="10" y="8" width="2" height="8" :fill="color"></rect>
        <rect x="12" y="8" width="2" height="8" :fill="color"></rect>
      </svg>
    </view>

    <!-- 用户 -->
    <view v-else-if="name === 'user'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"></path>
        <circle cx="12" cy="7" r="4"></circle>
      </svg>
    </view>

    <!-- 网络信号 -->
    <view v-else-if="name === 'signal'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M2 20h.01"></path>
        <path d="M7 20v-4"></path>
        <path d="M12 20v-8"></path>
        <path d="M17 20V8"></path>
        <path d="M22 4v16"></path>
      </svg>
    </view>

    <!-- 眼睛图标 - 显示密码 -->
    <view v-else-if="name === 'eye'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
        <circle cx="12" cy="12" r="3"></circle>
      </svg>
    </view>

    <!-- 眼睛关闭图标 - 隐藏密码 -->
    <view v-else-if="name === 'eye-off'" class="icon-content">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        :fill="fill"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"></path>
        <path
          d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"
        ></path>
        <path
          d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"
        ></path>
        <line x1="2" x2="22" y1="2" y2="22"></line>
      </svg>
    </view>
  </view>
</template>

<script>
export default {
  name: "SvgIcon",
  props: {
    name: {
      type: String,
      required: true,
    },
    size: {
      type: [String, Number],
      default: "24",
    },
    color: {
      type: String,
      default: "#4ea3ff",
    },
    fill: {
      type: String,
      default: "none",
    },
  },
  computed: {
    iconClass() {
      return `svg-icon-${this.name}`;
    },
    iconStyle() {
      return {
        width: this.size + "px",
        height: this.size + "px",
        color: this.color,
      };
    },
  },
};
</script>

<style scoped>
.svg-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.icon-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-content svg {
  width: 100%;
  height: 100%;
}
</style>
