# GrainMart — storefront (Next.js)

商城 C 端首页，数据由 **product 微服务** 提供，经网关访问。

## 开发

```bash
npm install
npm run dev
```

打开 [http://localhost:3001](http://localhost:3001)。登录/注册为 Next 页面：`/login`、`/register`（旧静态页 `/auth/login/index.html` 会 302 到 `/login`）。需先启动网关（88）与 gulimall-product。

## 环境变量

可选 `.env.local`：

```env
NEXT_PUBLIC_API_BASE=http://localhost:88
```

## 静态资源

原版首页静态资源在：`资料源码/代码/html/首页资源`。可将 `index/img` 拷到 `public/static/index/img`，并在组件中引用 `/static/index/img/xxx.png`；样式可参考 `index/css/GL.css` 逐步迁移到 CSS Modules。

详细实施说明见项目根目录 `docs/MALL_HOMEPAGE_NEXTJS.md`。
