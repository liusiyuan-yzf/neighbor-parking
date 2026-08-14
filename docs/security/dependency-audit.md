# 依赖安全审计基线

- 审计日期：2026-08-14
- 命令：`pnpm audit --audit-level high`
- 结果：0 critical、0 high、6 moderate、6 low

## 已完成的修复

- 锁定 Vue 与 `@dcloudio/types` 的兼容版本，消除 peer 依赖漂移。
- 将 Vite 升级并锁定到 `6.4.3`，将 Playwright 升级到修复版本。
- 覆盖 `path-to-regexp`、`adm-zip`、`jpeg-js`、PostCSS 和 `ws` 等兼容补丁。
- 高危和严重漏洞均降为 0，CI 在重新出现高危漏洞时失败。

## 暂时保留的上游风险

剩余中低风险来自 uni-app 工具链间接依赖的 Esbuild、Phin、Vue 2 模板编译器和 `qs`。这些包用于多端编译、图片处理或本地开发服务，不会进入 Spring Boot 服务端或管理后台静态运行包。对可能破坏 DCloud 编译兼容性的跨主版本覆盖暂不强制，继续由 Dependabot 与 CI 跟踪。

## 后续处理

- 每次升级 uni-app 编译器后重新运行完整审计和双端构建。
- DCloud 发布包含相应传递依赖修复的编译器后优先升级。
- CI 保存审计门禁；出现 high 或 critical 漏洞时阻止发布。
