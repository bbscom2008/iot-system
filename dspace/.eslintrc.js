module.exports = {
  root: true, // 标记为根配置，防止向上查找其他配置
  env: {
    browser: true,
    node: true,
    es6: true
  },
  parser: "vue-eslint-parser", // Vue2 必须用这个解析器
  parserOptions: {
    parser: "babel-eslint", // 兼容 Vue2 的语法
    sourceType: "module"
  },
  extends: [
    "eslint:recommended",
    "plugin:vue/essential" // Vue2 核心规则（必须是这个，不是 vue3）
  ],
  plugins: ["vue"],
  rules: {
    // 彻底关闭 Vue 未使用变量检测（解决 Vetur 中的 index 报错）
    "vue/no-unused-vars": "off",
    // 关闭原生 ESLint 未使用变量规则（双重保险）
    "no-unused-vars": "off",
    // 可选：仅忽略下划线开头的参数（如 _index），保留其他检测
    // "vue/no-unused-vars": ["error", {
    //   "argsIgnorePattern": "^_"
    // }]
  }
};