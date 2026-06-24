# 域名访问环境搭建（Nginx + Hosts）

按「Nginx + Windows/Mac 搭建域名访问环境」思路，用 hosts 把域名指到 Nginx，再由 Nginx 转发到网关和前端。

## 1. 修改本机 hosts，把域名指到 Nginx 所在机器

- **Windows**：用管理员打开  
  `C:\Windows\System32\drivers\etc\hosts`
- **macOS / Linux**：  
  `sudo vim /etc/hosts`

在文件末尾增加（把 `虚拟机IP` 换成 Nginx 所在机器的 IP；本机装 Nginx 就用 `127.0.0.1`）：

```text
# ecommerce 项目域名（指向 Nginx 所在机器）
虚拟机IP  ecommerce.com
虚拟机IP  www.ecommerce.com
虚拟机IP  search.ecommerce.com
虚拟机IP  item.ecommerce.com
虚拟机IP  member.ecommerce.com
```

例如本机既跑 Nginx 又跑网关/前端时：

```text
127.0.0.1  ecommerce.com
127.0.0.1  www.ecommerce.com
127.0.0.1  search.ecommerce.com
127.0.0.1  item.ecommerce.com
127.0.0.1  member.ecommerce.com
```

保存后，浏览器访问 `http://ecommerce.com` 会解析到该 IP。

## 2. 安装并配置 Nginx

- **本机**：用系统包管理安装（如 Mac: `brew install nginx`）。
- **虚拟机/服务器**：用 yum/apt 安装即可。

把项目里的示例配置复制到 Nginx 配置目录（或 `include` 进去），并改后端地址：

- 示例配置：`docs/nginx-ecommerce-domain.conf`
- 需根据实际修改：
  - `upstream` 里的网关地址（本机一般 `127.0.0.1:88`，Docker 用服务名如 `gulimall-gateway:88`）
  - 前端根目录 / 反向代理地址（Next 或静态资源）

然后重载 Nginx：

```bash
nginx -t && nginx -s reload
```

**本机一键（macOS + Homebrew）**：项目已提供 `docs/nginx-ecommerce-local.conf` 与脚本：

```bash
brew install nginx
# 80 端口（浏览器直接 http://www.ecommerce.com/，需输入本机密码）
./scripts/start-nginx-local.sh

# 或免 sudo 的 8088（访问 http://www.ecommerce.com:8088/）
./scripts/start-nginx-local.sh 8088
```

注意：`8080` 已被 renren-fast 占用，勿把 Nginx 绑到 8080。

## 3. 请求大致走向

```text
用户访问 ecommerce.com
  → hosts 解析到 虚拟机IP
  → Nginx 监听 80，按 server_name 匹配
  → 静态/前端：Nginx 直接提供或反代到 Next.js
  → /api/*：Nginx 反代到网关 :88
  → 网关再转发到对应微服务
```

## 4. 前端使用域名访问后端（可选）

若希望浏览器里用 `https://ecommerce.com` 访问页面，且接口也走同域名（减少跨域）：

- 方案 A：Nginx 同时代理前端和 `/api`，前端请求写相对路径 `/api/...`，由 Nginx 转给网关。
- 方案 B：前端仍用环境变量里的完整 API 地址（如 `http://ecommerce.com`），保证请求发到 Nginx，再由 Nginx 把 `/api` 转到网关。

生产/模拟环境可设：

```bash
NEXT_PUBLIC_API_BASE=http://ecommerce.com
```

或同域名时：

```bash
NEXT_PUBLIC_API_BASE=
```

前端用相对路径 `/api` 即可（需 Nginx 已配置 `/api` 反代到网关）。
