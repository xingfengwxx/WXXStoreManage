## 变更摘要

<!-- 1-3 句：为什么改、改了什么 -->

## 假设与影响范围

- 关键假设：
- 影响范围（页面 / 数据 / 导航 / 权限）：

## 状态覆盖

- [ ] Loading
- [ ] Content
- [ ] Empty
- [ ] Error
- [ ] Refresh / Retry
- [ ] Submit（如适用）

<!-- 新增或修改 UiState 时，逐项说明行为；不适用则写“不适用”及原因。 -->

## 并发与取消

<!-- 重复请求策略：取消前一次 / 忽略进行中 / 并行；协程归属与取消路径。 -->

## 错误与日志

<!-- AppErrorType 映射、用户提示来源、日志字段；确认无 token / 密码 / 个人信息。 -->

## 验证

- 执行命令与结果：
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:lintDebug`
  - `./gradlew :app:testDebugUnitTest`

## 未执行检查及原因

<!-- ktlint / detekt / CI / screenshot test 尚未接入；如有其他未执行项，逐条说明。 -->
