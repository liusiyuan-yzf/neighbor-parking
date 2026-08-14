# Docker Compose 部署

该编排用于阶段 2 演示环境，包含 MySQL 8.4、Spring Boot 服务、uni-app H5 和管理后台。

```bash
cd deploy
cp .env.example .env
# 修改 .env 中的三个密码，不要提交 .env
docker compose up --build
```

启动后：

- H5：`http://localhost:5173`
- 管理后台：`http://localhost:5174`
- API / Swagger：`http://localhost:8080/swagger-ui.html`

编排显式启用 `demo` profile 以初始化演示数据。正式部署时必须移除 `demo`，接入真实认证，并为数据库、JWT 与跨域来源提供生产配置。
