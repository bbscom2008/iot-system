# 静态资源说明

## 图标文件

请将以下图标文件从原微信小程序项目复制到 `src/static/xcx/` 目录：

### TabBar 图标
- `w1.png` - 首页未选中图标
- `shoye1.png` - 首页选中图标  
- `w2.png` - 报警未选中图标
- `baojing.png` - 报警选中图标
- `w3.png` - 我的未选中图标
- `geren.png` - 我的选中图标

### 其他图标
- `wechat.png` - 微信头像默认图标
- 其他需要的图标文件

## 复制步骤

1. 从 `C:\Users\Administrator\Desktop\server\app\xcx\` 目录复制所有图标文件
2. 粘贴到当前项目的 `src/static/xcx/` 目录
3. 确保图标文件名与 pages.json 中配置的路径一致

## 图标要求

- 格式：PNG
- 尺寸：建议 81x81px（TabBar 图标）
- 背景：透明
- 颜色：白色或浅色（适配深色主题）
