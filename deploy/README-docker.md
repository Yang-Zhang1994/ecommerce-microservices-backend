# Gulimall Docker 部署说明（B 方案）

## 1. 本地打包

在项目根目录执行（已通过可跳过）：

```bash
cd /Users/samuelcoe/IdeaProjects/gulimall
mvn clean package -DskipTests
```

## 2. 前端构建（必做一次）

前端镜像**不再在 Docker 内构建**（避免 “Build complete.” 后卡住），必须在本地先构建好 `dist`，镜像只做 COPY：

```bash
./scripts/build-frontend-for-docker.sh
```

或手动：

```bash
cd renren-fast-vue
sed -i.bak 's|http://localhost:88/api|/api|g' static/config/index.js   # Mac 用 sed -i '' 's|...|...|g' ...
npm run build
cd ..
```

未执行本步就构建镜像会因缺少 `dist/` 而报错。

## 3. 一键启动

前端已改为“本地构建 + 镜像只拷贝”，不再在镜像内跑 gulp，可避免 “Build complete.” 后卡住。其他镜像若出现 “resolving provenance for metadata file”，可用**传统构建器**：

```bash
DOCKER_BUILDKIT=0 docker compose -f docker-compose.app.yml up -d --build
```

首次会构建各模块镜像，稍等几分钟。之后只需：

```bash
docker compose -f docker-compose.app.yml up -d
```

## 4. 访问

- **前端（给 HR 的链接）**: http://localhost  
- **Gateway**: http://localhost:88  
- **Consul UI**: http://localhost:8500  

### 已部署到 EC2 时

- **前端**: http://3.145.65.47 （或你的 EC2 公网 IP）  
- **Gateway**: http://3.145.65.47:88  
- Gateway 和后台服务启动后约 1–2 分钟才就绪；若接口不可用，检查 RDS 安全组是否放行该 EC2 的 5432 访问。  

### 浏览器空白 + 控制台报 Unexpected token '<'（index.js）

**现象**：打开前端地址后白屏，开发者工具里看到 `Uncaught SyntaxError: Unexpected token '<'` 在 `index.js?t=...:1:1`，以及后续 `Cannot set properties of undefined (setting 'storeState')`。

**原因**：生产环境里 `index.html` 会先加载 **`./config/index.js`**（API 地址等配置）。该文件在构建产物里实际在 **`static/config/index.js`**。若镜像里没有把 `static/config` 复制为 **`config`**，则请求 `/config/index.js` 时 Nginx 会走 `try_files` 最后返回 **index.html**；浏览器把这段 HTML 当 JS 执行，首字符是 `<`，就报 `Unexpected token '<'`，后续 Vue 未初始化导致 `storeState` 报错。

**处理**：

1. **本地/EC2 重新构建前端镜像并重启前端容器**  
   - 本地先执行前端构建（见上文「前端构建」），再构建镜像并部署：
     ```bash
     ./scripts/build-frontend-for-docker.sh
     DOCKER_BUILDKIT=0 docker compose -f docker-compose.app.yml build gulimall-frontend
     docker compose -f docker-compose.app.yml up -d gulimall-frontend
     ```
   - 若已部署到 EC2：把包含「复制 static/config → config」步骤的 Dockerfile 和最新 `dist/`（或构建好的前端镜像）同步到 EC2 后，在 EC2 上执行上述 build + up（或通过 SSM 执行对应命令）。
2. **确认镜像内确有 `/config/index.js`**  
   Dockerfile 中应有：`RUN cp -r /usr/share/nginx/html/static/config /usr/share/nginx/html/config`（且构建前 `dist/static/config` 存在）。构建时若缺少 `static/config`，当前 Dockerfile 会直接报错，避免打出缺文件的镜像。

### 放行 EC2 访问 RDS 5432（必做）

微服务要连 RDS PostgreSQL，必须在 **RDS 的安全组** 里放行来自 EC2 的 5432 端口，否则会连不上库。

1. **打开 RDS 控制台**  
   登录 [AWS 控制台](https://console.aws.amazon.com/) → 搜索 **RDS** → 进入 RDS。

2. **找到你的数据库实例**  
   在「数据库」列表里点你的实例名（例如 `ecommerce-db`）。

3. **打开该实例使用的安全组**  
   在实例详情里找到 **「安全组」**（VPC security groups），点进**第一个安全组**（通常只有一个）。

4. **编辑入站规则**  
   - 在安全组页面点 **「编辑入站规则」**（Edit inbound rules）。  
   - 点 **「添加规则」**（Add rule）。  
   - **类型**：选 **PostgreSQL**（或「自定义 TCP」）。  
   - **端口**：**5432**（PostgreSQL 已选时一般会自动填好）。  
   - **来源**（Source）二选一：  
     - **推荐**：选 **「自定义」** → 在下方选 **「安全组」** → 选你 **EC2 实例所在的安全组**（例如 `launch-wizard-1` 或你创建 EC2 时用的那个）。这样只有该 EC2 能连 RDS。  
     - 或：选 **「任意位置 IPv4」**（0.0.0.0/0），任何 IP 都能试连 5432，仅适合测试，不建议生产。  
   - **描述**（可选）：如 `EC2 gulimall`。  
   - 点 **「保存规则」**（Save rules）。

5. **生效**  
   保存后立即生效，无需重启。EC2 上的容器若之前连不上数据库，稍等几秒后会自动重连（或重启一次容器：`docker compose -f docker-compose.app.yml restart`）。

### 用 AWS CLI 检查 EC2 与安全组

在本机安装并配置好 [AWS CLI](https://aws.amazon.com/cli/) 后，可用下面命令自查实例和安全组是否正常：

```bash
# 1. 看 us-east-2 里运行中的实例（实例 ID、状态、公网 IP、安全组）
aws ec2 describe-instances --region us-east-2 \
  --filters "Name=instance-state-name,Values=running" \
  --query "Reservations[*].Instances[*].[InstanceId,State.Name,PublicIpAddress,SecurityGroups[*].GroupId]" \
  --output table

# 2. 看某安全组的入站规则（把 sg-xxx 换成上面输出的 GroupId）
aws ec2 describe-security-groups --region us-east-2 --group-ids sg-0c42257e7b56cb51d \
  --query "SecurityGroups[*].IpPermissions[*].{Port:FromPort,Source:IpRanges[0].CidrIp}" --output table
```

- 实例状态为 **running**、有 **PublicIpAddress** → EC2 正常。  
- 入站规则里有 **22**（来源 0.0.0.0/0 或你的 IP）、**80**、**88** → 安全组配置正确。  
若本机 SSH 仍超时，多半是本地网络或防火墙限制出站 22，可换网络（如手机热点）再试。

### 解决 SSH 超时：本机配置 + 用 SSM 代替 SSH

**1. 本机 SSH 配置（减少超时、断线）**

在本机 `~/.ssh/config` 里为这台 EC2 加一段（没有就新建），保存后再用 `ssh gulimall-ec2` 连接：

```
Host gulimall-ec2
  HostName 3.145.65.47
  User ubuntu
  IdentityFile ~/Downloads/ecommerce-key.pem
  ServerAliveInterval 30
  ServerAliveCountMax 6
  ConnectTimeout 20
```

- **ServerAliveInterval 30**：每 30 秒发一次保活，减少中途断线。  
- **ConnectTimeout 20**：连接阶段最多等 20 秒。  
- 连接命令：`ssh gulimall-ec2`（IP 变了就改上面 HostName）。

**连接 EC2 再试一次（推荐顺序）**

1. **本机 SSH**：`ssh gulimall-ec2`（或 `ssh -i ~/Downloads/ecommerce-key.pem ubuntu@3.145.65.47`）。若超时，先查当前 IP：`aws ec2 describe-instances --region us-east-2 --instance-ids i-060d3cbec14d7835a --query "Reservations[0].Instances[0].PublicIpAddress" --output text`，把 IP 换成输出再试。  
2. **换网络**：用手机热点再试 SSH 或下面第 3 步（很多环境会封 22 或到 AWS 的出口）。  
3. **浏览器 EC2 实例连接**：EC2 控制台 → 选中实例 → **连接** → **EC2 实例连接** → **连接**。不依赖本机 22 端口。  
4. **Session Manager**：若实例已注册 SSM，EC2 控制台 → **连接** → **Session Manager** → **连接**；或本机执行 `aws ssm start-session --region us-east-2 --target i-060d3cbec14d7835a`（需安装 [Session Manager 插件](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html)）。  
5. **SSM 命令**：`aws ssm send-command --region us-east-2 --instance-ids i-060d3cbec14d7835a --document-name "AWS-RunShellScript" --parameters 'commands=["hostname"]'`；若报 `InvalidInstanceId`，说明实例尚未在 SSM 就绪，等几分钟或重启实例后再试。

**2. 用 AWS Systems Manager (SSM) 执行命令（不依赖 22 端口）**

已为实例 `i-060d3cbec14d7835a` 绑定 IAM 角色 **EC2-SSM-Gulimall**，可用 SSM 在实例上执行命令，无需 SSH。适合本机或 CI 到 EC2 网络不通 22 时使用。

- 实例首次绑定角色后，SSM 代理约 **2～5 分钟** 内完成注册，之后下面命令才可用。  
- 本机需已安装并配置好 [AWS CLI](https://aws.amazon.com/cli/)（`aws configure`）。

```bash
# 在实例上执行一条命令并取回输出（把 COMMAND_ID 换成下面返回的 ID）
COMMAND_ID=$(aws ssm send-command --region us-east-2 \
  --instance-ids i-060d3cbec14d7835a \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=["cd /tmp && echo hello"]' \
  --query "Command.CommandId" --output text)

# 等几秒后查结果
sleep 5
aws ssm get-command-invocation --region us-east-2 \
  --command-id "$COMMAND_ID" --instance-id i-060d3cbec14d7835a \
  --query "[Status,StandardOutputContent]" --output text
```

- 若报 `InvalidInstanceId` 或 `Instances not in a valid state`，说明实例尚未在 SSM 中就绪，见下文「SSM 实例未注册的修复」。  
- 交互式 shell：`aws ssm start-session --region us-east-2 --target i-060d3cbec14d7835a`（需本机安装 [Session Manager 插件](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html)）。

**SSM 实例未注册的修复（InvalidInstanceId）**

若 IAM 角色已绑定、安全组出站正常，但实例仍无法在 SSM 注册，多为实例无法通过公网访问 SSM 端点。可在该实例所在 VPC 内创建 **VPC 接口端点**（并启用私有 DNS），让 SSM Agent 经 VPC 内网注册：

1. 在 VPC 中新建安全组（如 `SSM-VPC-Endpoints-sg`），入站放行 **TCP 443**，来源为 **VPC CIDR**（如 `172.31.0.0/16`）。  
2. 创建三个 **接口类型** VPC 端点，并勾选 **启用私有 DNS**：  
   - `com.amazonaws.us-east-2.ssm`  
   - `com.amazonaws.us-east-2.ec2messages`  
   - `com.amazonaws.us-east-2.ssmmessages`  
   子网选实例所在子网，安全组选上一步创建的安全组。  
3. 重启实例，等 1～2 分钟后用 `aws ssm describe-instance-information --region us-east-2 --filters "Key=InstanceIds,Values=i-060d3cbec14d7835a"` 检查是否出现且 `PingStatus` 为 `Online`。

本实例已按上述方式配置，SSM 与 Session Manager 可用。

**原理说明：为什么公网访问不可靠、VPC 端点能解决**

1. **SSM 注册在做什么**  
   实例里的 SSM Agent 启动后，需要主动连到 AWS 的三个服务端点做「心跳」和注册：  
   - **ssm.***region*.amazonaws.com：下发命令、取结果等 API。  
   - **ec2messages.***region*.amazonaws.com：Agent 与 SSM 之间的消息通道（长连接/轮询）。  
   - **ssmmessages.***region*.amazonaws.com：Session Manager 的交互式会话数据。  
   只有这三个端点都可达（HTTPS 443），Agent 才能完成注册并保持 **PingStatus: Online**；否则会报 `InvalidInstanceId` / 实例不在有效状态。

2. **为什么「有公网 IP」仍可能注册失败**  
   实例有公网 IP、安全组出站也放行，理论上可以走公网访问上述域名。但在实际环境里常会出现：  
   - **出站被限制**：公司/运营商对出站 443 做白名单或代理，只放行部分域名，`*.amazonaws.com` 可能被拦或解析到错误地址。  
   - **DNS 或路由异常**：实例解析到的 IP 或路由路径不稳定，导致连接超时、丢包。  
   - **AWS 侧策略**：部分区域或账号下，对从公网访问 SSM 的限流/策略更严，内网访问更稳定。  
   所以会出现「IAM 正确、安全组也放行，但实例就是注册不上」的情况，本质是 **Agent 无法可靠地通过公网连到 SSM 端点**。

3. **VPC 接口端点 + 私有 DNS 如何解决**  
   - **VPC 接口端点**：在你这台实例所在的 VPC 里，为上述三个服务各建一个 **接口类型** 的 VPC 端点。流量不再从实例经公网到 AWS 公网入口，而是：实例 → VPC 内网 → 该 VPC 的端点网卡 → AWS 内部直达 SSM。不依赖公网出口，也不受你本地/运营商对 `*.amazonaws.com` 的限制。  
   - **启用私有 DNS**：在端点创建时勾选「私有 DNS」后，AWS 会在该 VPC 的 DNS（Route 53 Resolver）里注入解析：  
     - `ssm.us-east-2.amazonaws.com` → 端点 `ssm` 的私有 IP  
     - `ec2messages.us-east-2.amazonaws.com` → 端点 `ec2messages` 的私有 IP  
     - `ssmmessages.us-east-2.amazonaws.com` → 端点 `ssmmessages` 的私有 IP  
   Agent 仍然按官方文档里写的「公网域名」去连，不用改配置；只是在你这台 VPC 里，这些域名被解析到端点私有 IP，流量自动走 VPC 内网到 SSM，从而稳定完成注册。

4. **小结**  
   「实例上的 SSM Agent 无法通过公网可靠访问 SSM 端点」= Agent 连不上 ssm/ec2messages/ssmmessages 三个 HTTPS 端点，导致无法在 SSM 注册。  
   通过在同一 VPC 创建这三个服务的接口端点并启用私有 DNS，让 Agent 的流量经 VPC 内网直达 AWS，不依赖公网，注册即可稳定成功。

### SSH 仍然不行时：优先用浏览器连

本机 SSH 一直超时、换网络也不行时，按下面顺序试：

**1. 先确认当前公网 IP（可能已变）**

实例若经历过「停止 → 再启动」，公网 IP 会变，SSH 要用新 IP：

```bash
aws ec2 describe-instances --region us-east-2 --instance-ids i-060d3cbec14d7835a \
  --query "Reservations[0].Instances[0].PublicIpAddress" --output text
```

把 `~/.ssh/config` 里的 `HostName` 和实际连接命令改成上面输出的 IP 再试 SSH。

**2. 用浏览器里的「EC2 实例连接」（不依赖本机 22 端口）**

这种方式走 AWS 控制台，不经过你本机到 EC2 的 22 端口，很多「本机 22 被墙」的情况能连上：

1. 打开 [EC2 控制台](https://console.aws.amazon.com/ec2/) → **实例**。  
2. 选中实例 **i-060d3cbec14d7835a** → 点 **「连接」**。  
3. 选 **「EC2 实例连接」**（或 **EC2 Instance Connect**）→ 点 **「连接」**。  
4. 会在浏览器里打开一个终端，直接就是 `ubuntu@ip-xxx` 的 shell。

- **能打开** → 说明实例和 sshd 正常，多半是你本机或运营商限制了出站 22；以后可以在浏览器里操作，或换网络（手机热点）再试本机 SSH。  
- **也连不上** → 再按下面「控制台也连不上时」排查实例/VPC。

**3. 换网络试本机 SSH**

用手机开热点，电脑连热点后执行：

```bash
ssh -i ~/Downloads/ecommerce-key.pem -o ConnectTimeout=20 ubuntu@当前公网IP
```

（当前公网 IP 用上面第 1 步命令查。）

**4. 给实例绑弹性 IP（避免以后停启换 IP）**

停启后公网 IP 会变，绑上弹性 IP 后 IP 固定，方便记和配置 SSH：

1. EC2 控制台 → **网络与安全** → **弹性 IP** → **分配弹性 IP 地址**。  
2. 分配后选中该 IP → **操作** → **关联弹性 IP 地址** → 选实例 **i-060d3cbec14d7835a**。  
3. 之后 SSH 和 `~/.ssh/config` 里的 **HostName** 都改成这个弹性 IP。

### 浏览器「EC2 实例连接」一直停在 Establishing Connection...

现象：点「连接」后只有转圈和 **Establishing Connection...**，一直不进入终端、也不报错。

说明：浏览器已连上 AWS，但 **AWS 到实例的 SSH** 没建立成功，所以卡在这一步。

**已排查（当前实例）：**

- 实例状态 **running**，状态检查 **passed**；安全组入站 **22** 来自 **0.0.0.0/0**；子网 NACL 允许全部流量。
- 控制台输出里 **ec2-instance-connect**（Host Key Harvesting）和 **amazon-ssm-agent** 已正常启动。

**可能原因与对应处理：**

| 可能原因 | 建议操作 |
|----------|----------|
| AWS 到实例的 22 端口首次建立较慢或超时 | 多等 30～60 秒再试；仍卡住则关掉标签页，过 1～2 分钟重新点「连接」。 |
| 浏览器/扩展/代理干扰 WebSocket | 用无痕模式或换浏览器（Chrome/Edge/Safari），关闭代理/VPN 后再试。 |
| 实例刚启动，sshd 或 ec2-instance-connect 未就绪 | 重启后等 2～3 分钟再试「EC2 实例连接」。 |
| AWS 侧 EC2 Instance Connect 临时异常 | 过几分钟再试；或改用 **Session Manager**（若实例已出现在 SSM 中）。 |

**可先试：**

1. **重启实例**  
   EC2 控制台 → 选中实例 → **实例状态** → **重启实例**。等 2～3 分钟后再次用「EC2 实例连接」连接。  
2. **换网络**  
   用手机热点再试一次（排除本机网络对 WebSocket 或 AWS 出口的限制）。  
3. **Session Manager**  
   控制台 **连接** 里若出现 **Session Manager**，可直接用（不依赖 22 端口）。当前该实例若尚未在 SSM 注册，可重启后再等几分钟，有时会注册成功。

若多次重启、换网络、换浏览器后仍一直停在 Establishing Connection...，可考虑 **新开一台实例**（同区域、推荐同 AMI）做迁移，旧实例保留对比排查。

---

### 浏览器「EC2 实例连接」也连不上时：排查原因

本机 SSH 和浏览器里的 **EC2 实例连接** 都连不上（或一直转圈）时，可按下面排查。

**已确认正常的部分（用 AWS CLI 查过）：**

- 实例状态 **running**，状态检查 **passed**（reachability 正常）。
- 安全组 **sg-0c42257e7b56cb51d** 入站 **22** 来自 **0.0.0.0/0**，无限制。
- 控制台输出里有 **"Listening on ssh.socket - OpenBSD Secure Shell server socket"**，说明实例内 SSH 在监听。

**可能原因（EC2 实例连接 / SSH 都失败时）：**

1. **本机或公司网络限制**  
   出站 22 被墙、或到 AWS 的 WebSocket/HTTPS 被拦截，会导致本机 SSH 和浏览器里「EC2 实例连接」都失败（浏览器连接也会经你本机到 AWS）。  
   **建议**：用手机开热点，电脑连热点后再试一次「EC2 实例连接」和本机 SSH。

2. **浏览器 / 扩展 / 代理**  
   广告拦截、安全软件、代理或 VPN 可能拦截 EC2 实例连接的 WebSocket。  
   **建议**：用无痕/隐私模式、换一个浏览器（Chrome / Edge / Safari）、或关掉代理/VPN 再试「连接」。

3. **EC2 实例连接服务或区域问题**  
   个别时段 AWS 侧 EC2 Instance Connect 异常，或所在区域有差异。  
   **建议**：过几分钟再试；或换一种连接方式：**Session Manager**（需实例已注册 SSM，见上文「用 SSM 代替 SSH」）。

4. **实例刚重启，服务未就绪**  
   刚重启后 ssh 或 ec2-instance-connect 可能尚未完全就绪。  
   **建议**：等 2～3 分钟再试「EC2 实例连接」和 SSH。

**可选：用 Session Manager（不依赖 22 端口）**

实例已绑定 IAM 角色 **EC2-SSM-Gulimall**。等 SSM 代理注册成功后（绑定角色后约 2～10 分钟），可用 Session Manager 在浏览器里连：

1. 本机安装 [Session Manager 插件](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html)。  
2. 在 EC2 控制台选中实例 → **连接** → 选 **Session Manager**（若可用）→ **连接**。  
3. 或在终端执行：`aws ssm start-session --region us-east-2 --target i-060d3cbec14d7835a`。

**最后手段：新实例迁移**

若以上都无效，可新开一台实例（同区域、同 AMI、同安全组），用「EC2 实例连接」或 SSH 连上新实例，再把应用部署过去（或从旧实例做快照/AMI 后挂到新实例再起）。旧实例可先保留做对比排查。

### 控制台也连不上时：排查实例/VPC

若 **EC2 实例连接**（浏览器）也连不上，可用 AWS CLI 排查并看控制台输出：

```bash
# 实例状态与系统状态
aws ec2 describe-instance-status --region us-east-2 --instance-ids i-060d3cbec14d7835a --include-all-instances

# 控制台输出（最近一次启动的日志，可看到 systemd/SSH 是否报错）
aws ec2 get-console-output --region us-east-2 --instance-id i-060d3cbec14d7835a --latest --query "Output" --output text | tail -80
```

- **VPC/子网/路由**：默认 VPC 下 0.0.0.0/0 → igw，子网 MapPublicIpOnLaunch=true、NACL 允许入站即正常。  
- **控制台输出里若出现** `Failed to start systemd-journald`、`snapd.service: Watchdog timeout` 等 → 说明实例内系统异常，可能影响 sshd。  
- **处理建议**：在 EC2 控制台对该实例做 **「重启」**（Reboot instance）；若仍无法连接，再考虑 **停止 → 再启动**（公网 IP 会变，需重新记下或绑定弹性 IP）。

**用 AWS CLI 操作：**

```bash
# 1. 重启实例（公网 IP 不变）
aws ec2 reboot-instances --region us-east-2 --instance-ids i-060d3cbec14d7835a

# 等 2～3 分钟后检查状态
aws ec2 describe-instance-status --region us-east-2 --instance-ids i-060d3cbec14d7835a --include-all-instances --output table

# 2. 若重启后仍连不上，可 停止 → 再启动（会换公网 IP）
aws ec2 stop-instances --region us-east-2 --instance-ids i-060d3cbec14d7835a
# 等实例状态变为 stopped 后：
aws ec2 start-instances --region us-east-2 --instance-ids i-060d3cbec14d7835a
# 等实例 running 后，查新公网 IP：
aws ec2 describe-instances --region us-east-2 --instance-ids i-060d3cbec14d7835a --query "Reservations[0].Instances[0].PublicIpAddress" --output text
# 用新 IP 再试 SSH： ssh -i ~/Downloads/ecommerce-key.pem ubuntu@新IP
```

### 怎么检查 RDS 放行是否成功

按下面顺序在本机或 SSH 到 EC2 后执行即可。

**1. 本机浏览器（最快）**

- 打开 **http://3.145.65.47**（或你的 EC2 公网 IP）  
  - 能打开登录页 → 前端正常。  
- 登录后能进后台、菜单/接口不报错 → 说明 Gateway 和微服务已连上 RDS，**放行成功**。  
- 若一直转圈或提示网络错误 → 再按下面 2、3 步排查。

**2. 在 EC2 上测 5432 端口（SSH 登录后执行）**

```bash
ssh -i ~/Downloads/ecommerce-key.pem ubuntu@3.145.65.47

# 测试能否连上 RDS 的 5432（把下面的 RDS 地址换成你的）
nc -zv ecommerce-db.cxyaw0eooo9c.us-east-2.rds.amazonaws.com 5432
```

- 出现 **`Connection to ... 5432 port [tcp/postgresql] succeeded!`** → 网络/安全组放行**成功**。  
- 若 **`Connection timed out`** 或 **`Connection refused`** → 多半是 RDS 安全组还没放行该 EC2（或 IP/安全组填错），回去检查入站规则。

**3. 看微服务日志（SSH 登录后执行）**

```bash
# 看后台服务是否已连上 DB 并启动完成
docker logs renren-fast 2>&1 | tail -30
docker logs gulimall-gateway 2>&1 | tail -30
```

- 若有 **`Started RenrenApplication`**、**`Started GulimallGatewayApplication`** 且没有一堆 `Connection refused` / `could not connect` → 说明已连上数据库，**放行成功**。  
- 若日志里一直是连接超时或 refused → 仍可能是 5432 未放行或 RDS 地址/账号错误。

**4. 若刚改完安全组，可重启容器再测**

```bash
cd ~/gulimall && docker compose -f docker-compose.app.yml restart
# 等 1～2 分钟后，再打开 http://3.145.65.47 登录试一次
```

## 5. 数据库

各微服务（product / member / ware / coupon / order / renren-fast）的数据库连接由各自 `application.yml` 或环境变量决定。  
若使用 RDS 或远程库，可在项目根目录建 `.env`，例如：

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds:5432/gulimall
SPRING_DATASOURCE_USERNAME=xxx
SPRING_DATASOURCE_PASSWORD=xxx
```

再在 `docker-compose.app.yml` 里给对应 service 增加 `env_file: - .env` 或逐项写 `environment`。

## 6. 停止

```bash
docker compose -f docker-compose.app.yml down
```

## 7. 云服务器（如 EC2）

1. 将整个项目（或打包好的 jar + Dockerfile + docker-compose.app.yml）上传到服务器。  
2. 安装 Docker 与 Docker Compose。  
3. 在项目根执行 `mvn clean package -DskipTests`（或本机打好把 `target/` 和各 Dockerfile 一起上传）。  
4. 执行 `docker compose -f docker-compose.app.yml up -d --build`。  
5. 安全组开放 80、88、8500（按需）。简历链接写 `http://你的公网IP` 或绑定域名。

**用 SSH 连接 EC2 然后启动服务**

若能 SSH 登录（如 `ssh gulimall-ec2` 或 `ssh -i ~/Downloads/ecommerce-key.pem ubuntu@3.145.65.47`），登录后执行：

```bash
cd ~/gulimall
docker compose -f docker-compose.app.yml up -d
```

（SSH 一般用 `ubuntu` 用户，`~` 即 `/home/ubuntu/gulimall`。）

**用 SSM 在实例上启动服务（不依赖 SSH）**

若本机 SSH 不通、已用 SSM 连上实例，可在本机执行（需已配置 AWS CLI）：

```bash
aws ssm send-command --region us-east-2 --instance-ids i-060d3cbec14d7835a \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=["cd /home/ubuntu/gulimall && docker compose -f docker-compose.app.yml up -d"]' \
  --query "Command.CommandId" --output text
```

注意：SSM 的 `send-command` 默认以 **root** 运行，`~` 为 `/root`，项目在 ubuntu 用户下，故路径需写 **`/home/ubuntu/gulimall`**。
