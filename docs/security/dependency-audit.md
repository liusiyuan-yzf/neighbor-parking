# 依赖安全审计基线

- 审计日期：2026-08-14
- 命令：`pnpm audit --prod`
- 结果：0 critical、2 high、13 moderate、3 low

## 已完成的修复

- 锁定 Vue 与 `@dcloudio/types` 的兼容版本，消除 peer 依赖漂移。
- 覆盖 `@intlify`、`adm-zip`、`jpeg-js`、PostCSS 和 `ws` 等可兼容的传递依赖补丁。
- 漏洞数量从 30 个降至 18 个，高危项从 9 个降至 2 个。

## 暂时保留的上游风险

剩余高危项来自 Vite 5.2.8。当前 `@dcloudio/vite-plugin-uni` 官方包把 peer 版本精确限定为 Vite 5.2.8；强制升级会脱离官方支持组合，因此本次没有使用不兼容覆盖。

其余中低风险主要来自 Vite 关联的 Esbuild、旧版 Phin，以及尚未发布审计报告所要求补丁版本的 Babel 7。这些包属于构建工具链，不会作为车位平台运行时代码直接部署，但仍应持续跟踪。

## 后续处理

- 每次升级 uni-app 编译器后重新运行完整审计和双端构建。
- DCloud 官方放宽或更新 Vite 版本后，优先升级并移除本说明中的 Vite 风险。
- CI 上线时保存审计结果；出现 critical 漏洞时阻止发布。
