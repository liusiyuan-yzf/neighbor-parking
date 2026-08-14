# Neighbor Parking（邻里车位）

Neighbor Parking 是一个面向小区场景的开源车位共享平台。车位拥有者可以发布空闲时段，附近车主可以预约共享车位。

> 当前状态：阶段 1 方案设计与阶段 2 免费共享 MVP 已实现。收费、支付、退款与结算不在本阶段范围内。

## MVP 路线

1. 微信小程序优先，先验证免费共享场景。
2. 增加收费预约、支付、退款与结算。
3. 发布 H5、Android、iOS，并按需对接物业和智能道闸。

完整产品与技术方案见 [docs/product/overall-solution.md](docs/product/overall-solution.md)。

## 仓库结构

```text
neighbor-parking/
├── apps/
│   ├── parking-uniapp/    # uni-app 用户端
│   └── parking-admin/     # Vue 3 物业/平台管理后台
├── server/                # Java 8 + Spring Boot 2.7 REST 服务
├── deploy/                # Docker Compose 与 Nginx 配置
└── docs/                  # 产品、架构和协作文档
```

## 本地开发

环境要求：Node.js 20+、pnpm 9+、Java 8。服务端默认使用 H2 内存数据库并初始化三类演示身份。

```bash
pnpm install
cd server
./mvnw spring-boot:run
```

另开终端运行 H5：

```bash
pnpm dev:h5
```

打开 `http://localhost:5173`，选择演示身份登录。管理后台使用：

```bash
pnpm dev:admin
```

打开 `http://localhost:5174`。服务端 Swagger UI 位于 `http://localhost:8080/swagger-ui.html`。

微信小程序开发：

```bash
pnpm dev:mp-weixin
```

将生成目录 `apps/parking-uniapp/dist/dev/mp-weixin` 导入微信开发者工具。

## 质量检查

```bash
pnpm type-check
pnpm format:check
pnpm build:h5
pnpm build:mp-weixin
pnpm build:admin
pnpm validate:compose
pnpm audit --audit-level high
cd server && ./mvnw test
```

## 演示身份

- 用户 `1`：租用人“小林”，可搜索、预约、签到、完成、评价和投诉。
- 用户 `2`：车位主“王阿姨”，可登记车位并发布空闲时段。
- 用户 `3`：物业管理员，可审核车位、管理小区、处理投诉和查看审计日志。

开发登录只在 `local` 或显式 `demo` profile 开启，生产环境禁止启用。

## 文档

- [阶段 1、2 验收清单](docs/product/stage-1-2-acceptance.md)
- [免费共享 MVP PRD](docs/product/mvp-prd.md)
- [系统架构](docs/architecture/system-architecture.md)
- [状态机](docs/architecture/state-machines.md)
- [数据库设计](docs/database/schema.md)
- [OpenAPI](docs/api/openapi.yaml)
- [Docker 部署](deploy/README.md)

## 参与贡献

请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 和 [SECURITY.md](SECURITY.md)。

## 许可证

本项目采用 [Apache License 2.0](LICENSE) 开源。
