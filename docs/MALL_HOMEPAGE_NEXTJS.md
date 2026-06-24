# 商城首页（Next.js）实施说明

## 1. 定位与架构

- **商城首页**：面向 C 端用户的谷粒商城首页，与后台管理（renren-fast-vue）分离。
- **技术选型**：Next.js（App Router），数据由 **product 微服务** 提供（分类、后续可扩展 SPU/推荐等）。
- **现有静态资源**：`/Users/samuelcoe/Documents/Project/gulimall-master/资料源码/代码/html/首页资源` 内有 `index.html`、`index/css/GL.css`、`index/js/*`、`index/img`、`index/json/catalog.json`，可作为 UI 与交互参考，逐步用 React 组件 + CSS 复刻。

## 2. 项目结构（已创建）

```
gulimall/
  gulimall-mall/          # Next.js 商城前端
    src/
      app/
        layout.tsx        # 根布局
        page.tsx           # 首页（服务端拉取分类）
        globals.css
        page.module.css
      lib/
        api.ts             # 请求网关 /api（如 /api/product/category/list/tree）
      types/
        api.ts             # CategoryEntity、R 等类型
    package.json
    next.config.js
    tsconfig.json
```

## 3. 实施步骤

### 3.1 安装依赖与运行

```bash
cd gulimall/gulimall-mall
npm install
npm run dev
```

访问：`http://localhost:3001`。首页会请求 `GET /api/product/category/list/tree`（通过环境变量 `NEXT_PUBLIC_API_BASE`，默认 `http://localhost:88`），需保证 **网关** 和 **gulimall-product** 已启动。

### 3.2 环境变量

在 `gulimall-mall/.env.local` 中配置（可选）：

```env
# 开发时网关地址（Next 服务端与客户端请求都会用）
NEXT_PUBLIC_API_BASE=http://localhost:88
```

**生产环境**（二选一即可）：

1. **方式一：用 `.env.production`**  
   在 `gulimall-mall/` 下新建 `.env.production`，内容例如：
   ```env
   NEXT_PUBLIC_API_BASE=https://api.yourdomain.com
   ```
   把 `https://api.yourdomain.com` 换成你线上网关的真实地址（如 `https://gateway.你的域名.com`）。  
   然后执行 `npm run build`，再 `npm run start` 或把构建产物部署到服务器。  
   （可参考仓库里的 `.env.production.example`，复制为 `.env.production` 再改。）

2. **方式二：构建时临时指定**  
   不建 `.env.production`，在构建时直接写死生产网关地址，例如：
   ```bash
   NEXT_PUBLIC_API_BASE=https://api.yourdomain.com npm run build
   ```
   再用 `npm run start` 或你的方式跑生产。

注意：`NEXT_PUBLIC_*` 会在 **构建时** 打进前端包，所以生产地址要在 **执行 `npm run build` 时** 就确定（要么写在 `.env.production`，要么在构建命令前加环境变量）。部署到服务器后一般不再改这个值，除非重新构建。

### 3.3 复用“首页资源”的静态资源

1. **图片/字体**：将 `首页资源/index/img` 拷贝到 `gulimall-mall/public/static/index/img`，在组件中用 `/static/index/img/xxx.png` 引用。
2. **样式**：参考 `首页资源/index/css/GL.css` 与 `index.html` 结构，把顶部搜索、左侧分类、轮播等拆成 React 组件，样式迁到 `page.module.css` 或独立 `.module.css`。
3. **轮播**：原页面使用 Swiper，可在 Next 中安装 `swiper`，在客户端组件里做轮播（或用 CSS 简单轮播）。
4. **catalog 数据**：不再使用静态 `catalog.json`，首页分类直接使用接口 `GET /api/product/category/list/tree` 返回的树形数据。

### 3.4 与网关 / 后端的关系

- **product 微服务**：已提供 `CategoryController.list()` → `/product/category/list/tree`，返回树形分类。
- **网关**：已有 `/api/product/**` → `gulimall-product` 的路由；Next 中所有请求发往 `NEXT_PUBLIC_API_BASE + '/api/...'` 即可。
- **跨域**：开发时 Next 跑在 3001，网关在 88。网关已配置 CORS（`GulimallCorsConfiguration`）显式允许 `http://localhost:3001` 与 `http://127.0.0.1:3001`，并保留 `*` 兜底；同时有 `DedupeResponseHeader=Access-Control-Allow-Origin` 去重。若仍遇跨域，可检查网关是否已重启、或在该配置中补充其他前端域名。

### 3.5 部署方式（简要）

- **方式 A**：`npm run build` 后 `next start -p 3001`，Nginx 将商城域名反代到 `http://127.0.0.1:3001`。
- **方式 B**：`next build` 后使用 `output: 'export'` 静态导出，Nginx 直接托管 `out` 目录（此时无服务端拉数，需首页改为客户端 fetch 或预生成静态页）。

建议先用方式 A，保留服务端请求 product 接口能力。

## 4. 小结

- 商城首页以 **Next.js（gulimall-mall）** 实现，数据来自 **product 微服务**（分类等），通过 **网关** 访问 `/api/product/...`。
- 已搭好首页骨架：顶栏、左侧分类树（来自接口）、内容区占位；静态资源从 `首页资源` 拷贝到 `public` 并按需复用 GL.css 与结构即可逐步还原原稿。
