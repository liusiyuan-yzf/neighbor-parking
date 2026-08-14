# 邻里车位 uni-app 用户端

使用 Vue 3、TypeScript、Pinia 与 uni-app 开发，可构建 H5、微信小程序及 App。

## 运行

```bash
pnpm dev:h5
pnpm dev:mp-weixin
```

默认请求 `http://localhost:8080/api/v1`。可通过 `VITE_API_BASE_URL` 覆盖。

核心流程包括开发登录、附近车位搜索、隐私脱敏、车辆管理、免费预约、签到离场、评价投诉，以及车位主登记与时段发布。
