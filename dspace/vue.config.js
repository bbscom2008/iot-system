const isProd = process.env.NODE_ENV === 'production';
const isMpWeixin = process.env.UNI_PLATFORM === 'mp-weixin';

module.exports = {
  // 生成 source map，便于在微信开发者工具中定位到源码
  productionSourceMap: true,

  configureWebpack: () => {
    // 仅对微信小程序产物强制生成可映射的 source-map
    if (isProd && isMpWeixin) {
      return {
        devtool: 'source-map',
      };
    }
    return {};
  },
};
