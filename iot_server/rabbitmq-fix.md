
# rabbitmq 面板 

http://192.168.56.128:15672/


# 问题修复脚本

```shell

cat > fix-all-rabbitmq.sh << 'EOF'
#!/bin/bash
echo "=== 一键修复 RabbitMQ 安装问题 ==="

# 1. 停止并清理
echo "1. 停止并清理..."
docker stop rabbitmq 2>/dev/null || true
docker rm rabbitmq 2>/dev/null || true
docker network prune -f
docker container prune -f
docker volume prune -f

# 2. 释放端口
echo "2. 释放端口..."
for port in 5672 15672 25672 4369; do
    echo "检查端口 ${port}..."
    if sudo ss -tulpn | grep -q ":${port} "; then
        echo "  端口 ${port} 被占用，释放..."
        sudo kill -9 $(sudo lsof -t -i:${port}) 2>/dev/null || true
        sleep 1
    fi
done

# 3. 重启 Docker
echo "3. 重启 Docker 服务..."
sudo systemctl restart docker
sleep 5

# 4. 检查 Docker 状态
echo "4. 检查 Docker 状态..."
if ! sudo systemctl is-active --quiet docker; then
    echo "Docker 未运行，启动..."
    sudo systemctl start docker
fi

# 5. 使用最简单的方式运行
echo "5. 启动 RabbitMQ..."
docker run -d \
  --name rabbitmq \
  --network host \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  -e RABBITMQ_NODE_PORT=5672 \
  rabbitmq:3.9-management

# 6. 等待并检查
echo "6. 等待启动..."
sleep 30

# 7. 验证
echo "7. 验证安装..."
if docker ps | grep -q rabbitmq; then
    echo "✅ 容器运行中"
    
    # 测试端口
    echo "测试端口监听:"
    if ss -tulpn | grep -q ":5672 "; then
        echo "✅ 5672 端口监听正常"
    else
        echo "❌ 5672 端口未监听"
    fi
    
    if ss -tulpn | grep -q ":15672 "; then
        echo "✅ 15672 端口监听正常"
    else
        echo "❌ 15672 端口未监听"
    fi
    
    # 测试访问
    echo "测试管理界面访问..."
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:15672 2>/dev/null | grep -q "200\|401"; then
        echo "✅ 管理界面可访问"
        echo ""
        echo "========================================"
        echo "安装成功！"
        echo "管理界面: http://$(curl -s ifconfig.me):15672"
        echo "用户名: admin"
        echo "密码: admin123"
        echo "AMQP端口: 5672"
        echo "========================================"
    else
        echo "⚠️  管理界面暂时无法访问"
        echo "查看容器日志: docker logs rabbitmq"
    fi
else
    echo "❌ 容器启动失败"
    echo "查看错误: docker logs rabbitmq"
fi
EOF

chmod +x fix-all-rabbitmq.sh
./fix-all-rabbitmq.sh


```



# 延时插件的使用

```

插件下载地址：

https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases?page=2

# 2. 复制到容器
echo "2. 复制插件到容器..."
docker cp rabbitmq_delayed_message_exchange-3.9.0.ez rabbitmq:/opt/rabbitmq/plugins/

# 3. 启用插件
echo "3. 启用插件..."
docker exec rabbitmq rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 4. 重启容器
echo "4. 重启 RabbitMQ..."
docker restart rabbitmq


```