# 如何购买并配置 AWS 云服务器（EC2）

按下面步骤在 AWS 上开一台 EC2，装好 Docker，并放行 80、88、22 端口，用于部署 Gulimall。

---

## 一、前提

- 已有 **AWS 账号**（没有可到 [aws.amazon.com](https://aws.amazon.com) 注册，需信用卡）
- 能访问 [AWS 管理控制台](https://console.aws.amazon.com/)

---

## 二、创建 EC2 实例（买服务器）

### 1. 进入 EC2 控制台

1. 登录 [AWS 控制台](https://console.aws.amazon.com/)
2. 顶部搜索 **EC2**，进入 **EC2 控制台**
3. 左上角选**区域**（如 **北京** cn-north-1 或 **宁夏** cn-northwest-1；海外可选 ap-northeast-1 等）

### 2. 启动实例

1. 在 EC2 控制台点击橙色按钮 **「启动实例」(Launch instance)**

### 3. 名称（可选）

- **名称**：填一个标签，如 `gulimall-server`

### 4. 选择系统镜像（AMI）

- **应用程序和 OS 镜像**：选 **Ubuntu**
- 版本选 **Ubuntu Server 22.04 LTS**

### 5. 选择实例类型（配置）

- 选 **t3.medium**（约 2 vCPU、4 GB 内存），适合跑 Gulimall
- 若只是体验、省成本，可先选 **t3.small**（2 vCPU、2 GB），不够再改大

### 6. 密钥对（用于 SSH 登录，必选）

- **密钥对名称**：选 **创建新密钥对**
- 名称填如 `gulimall-key`，类型选 **RSA**，格式选 **.pem**
- 点击「创建密钥对」，浏览器会下载一个 `.pem` 文件，**妥善保存**（丢失无法再下载）

### 7. 网络设置（安全组 = 防火墙）

- 展开 **「网络设置」**，点 **「编辑」**
- **创建安全组**：选「创建安全组」
- **安全组名称**：如 `gulimall-sg`
- **入站规则** 添加三条：

| 类型   | 端口范围 | 来源     | 说明     |
|--------|----------|----------|----------|
| SSH    | 22       | 我的 IP（或 0.0.0.0/0） | 登录服务器 |
| 自定义 TCP | 80   | 0.0.0.0/0 | 前端     |
| 自定义 TCP | 88   | 0.0.0.0/0 | Gateway  |

- **来源** 选「任何位置」(0.0.0.0/0) 表示公网可访问；若只允许自己访问，22 可改为「我的 IP」
- 出站默认放行即可，不用改

### 8. 存储

- 默认 8 GB 根卷一般够用；若打算长期用，可改成 **20 GB** 或更大

### 9. 启动

- 点击 **「启动实例」**
- 等状态变为 **运行中**，在实例列表里记下 **公有 IPv4 地址**（即公网 IP）

---

## 三、用 SSH 登录服务器

在**本机**终端执行（把路径和 IP 换成你的）：

```bash
# 密钥权限（仅第一次需要）
chmod 400 /path/to/gulimall-key.pem

# 登录（Ubuntu 默认用户名是 ubuntu）
ssh -i /path/to/gulimall-key.pem ubuntu@你的公网IP
```

例如：

```bash
ssh -i ~/Downloads/gulimall-key.pem ubuntu@3.112.23.45
```

---

## 四、在服务器上安装 Docker 和 Docker Compose

登录到 EC2 后，在**服务器上**执行：

```bash
# 1. 更新包并装依赖
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

# 2. 添加 Docker 官方 GPG 和仓库
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 3. 安装 Docker Engine 和 Compose 插件
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 4. 让当前用户不用 sudo 跑 docker（可选）
sudo usermod -aG docker ubuntu
```

执行完后**先退出 SSH 再重新登录**一次，这样 `docker` 不用加 `sudo` 才生效。

验证：

```bash
docker --version
docker compose version
```

两条都有版本号即表示安装成功。

---

## 五、小结检查

| 项目           | 状态 |
|----------------|------|
| EC2 实例       | Ubuntu 22.04，建议 t3.medium（2 核 4G） |
| 安全组         | 22（SSH）、80（前端）、88（Gateway）已放行 |
| 密钥 .pem      | 已保存，用于 `ssh -i xxx.pem ubuntu@公网IP` |
| Docker         | 已安装 `docker`、`docker compose` |

下一步：在服务器上部署 Gulimall（上传代码/镜像、配置数据库、执行 `docker compose up`），可继续按 [上线清单.md](./上线清单.md) 中的「数据库」和「部署」部分操作。

---

## 常见问题

**Q：没有信用卡能注册 AWS 吗？**  
A：注册时会要求填信用卡，部分区域新账号有 12 个月免费套餐（如 t2.micro 等），超出会扣费，注意在控制台查看用量。

**Q：选哪个区域？**  
A：国内访问可选**北京**或**宁夏**；海外可选东京、新加坡等，延迟和价格略有不同。

**Q：22 端口开放 0.0.0.0/0 安全吗？**  
A：不建议长期对全世界开放。更稳妥做法是「来源」选「我的 IP」，或配合 VPN/堡垒机使用。

**Q：公网 IP 会变吗？**  
A：重启/关机再开，默认会变。需要固定 IP 可给该实例分配 **弹性 IP（EIP）**，在 EC2 控制台「网络与安全」→「弹性 IP」里操作。
