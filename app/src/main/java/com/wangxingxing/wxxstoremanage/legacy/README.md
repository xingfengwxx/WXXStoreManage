# 遗留模块源码并入区

本目录是旧项目功能模块的源码隔离区。当前只保留边界与规划，不包含可执行代码、路由或依赖。

## 首期迁移模块

- 小票打印：首期 MVP 核心业务流程，旧项目已完成实现。
- 清点发货：首期 MVP 核心业务流程，旧项目已完成实现。

两个模块以源码并入方式迁移并做少量适配，不重新设计业务流程；迁移前仍必须先写 ADR 固定宿主、会话与构建能力，不得直接开始搬代码。业务术语与状态流转以旧项目实现为基准，与现有单据模型对齐后补入 `GLOSSARY.md`。

## 允许放入的内容

- 旧项目的 Java、Kotlin 源码与 XML 资源/layout，可原样保留 Activity、Fragment、自定义 View 等组件。
- 整包导入的文件保留原文件头、英文注释与命名；新增文件与实质修改的代码遵守仓库的 Kotlin/Java 规范（统一文件头、中文注释、Java 17）。
- 页面宿主、导航接入、会话与返回栈由迁移 ADR 决定，任何接入方式都不得绕过根级 `SessionGate`。

## 依赖与边界

- `legacy → core`、`app → legacy`；`feature` 与 `legacy` 互不依赖。
- 区外新代码仍为 Kotlin+Compose，不新增 Java Activity、Fragment 或自定义 View。
- 迁移开始前不建子包、不加依赖、不开 ViewBinding/DataBinding、不放开 kapt；需要时在迁移 ADR 中逐项决策。

## 迁移 ADR 必须回答

- 页面宿主：Activity、Fragment 还是 `AndroidView`；如何接入 Nav3 返回栈。
- 会话失效与角色权限：如何复用 `SessionGate` 与现有统一清理路径。
- 构建能力：是否启用 ViewBinding/DataBinding，是否允许 kapt，或必须迁移到 KSP。
- 资源与依赖：资源命名冲突、AppCompat/Material 等依赖、打印 SDK 的引入方式。
- 测试与术语：测试策略，以及是否需要更新 `GLOSSARY.md`。

## 编译能力门禁

`app/src/debug` 中的 Java/XML 探针与 `app/src/testDebug` 中的 Robolectric 测试共同验证手写 Java、layout 资源与 Kotlin→Java 调用链；不要删除或绕过。
