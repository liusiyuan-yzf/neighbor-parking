# Neighbor Parking（邻里车位）

Neighbor Parking 是一个面向小区场景的开源车位共享平台。车位拥有者可以发布空闲时段，附近车主可以预约免费或有偿车位。

> 当前状态：项目初始化阶段。已完成 uni-app 工程骨架与整体方案文档，业务界面将在视觉方向确认后实现。

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
│   └── parking-admin/     # 管理后台（待初始化）
├── server/                # 服务端（待初始化）
├── deploy/                # 本地及生产部署（待初始化）
└── docs/                  # 产品、架构和协作文档
```

## 本地开发

环境要求：Node.js 20+、pnpm 9+。

```bash
pnpm install
pnpm dev:h5
```

微信小程序开发：

```bash
pnpm dev:mp-weixin
```

将生成目录 `apps/parking-uniapp/dist/dev/mp-weixin` 导入微信开发者工具。

## 质量检查

```bash
pnpm type-check
pnpm build:h5
pnpm build:mp-weixin
```

## 参与贡献

请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 和 [SECURITY.md](SECURITY.md)。

## 许可证

本项目采用 [Apache License 2.0](LICENSE) 开源。
