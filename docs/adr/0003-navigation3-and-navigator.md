# Navigation 3 + 统一 Navigator

状态：已接受（2026-10-25）

页面需要类型安全的参数、多一级返回栈与按页面作用域的 ViewModel。Navigation 3 以 `NavKey` 与 `NavDisplay` 为中心，适合 Compose 单 Activity；但它的返回栈是可变状态，如果让 ViewModel 直接操作，会破坏可测试性与单一导航入口。因此决定使用 Navigation 3，路由键为 `@Serializable` 且实现 `AppRoute : NavKey`，由各 feature 声明；底部四个一级页各自持有返回栈；`Navigator` 只暴露 `navigate`、`goBack`、`replaceTop`，feature 通过扩展函数提供语义化入口；ViewModel 不持有 Navigator，Route 负责收集 Effect 与转发导航回调；ViewModel 通过 `rememberViewModelStoreNavEntryDecorator` 按 NavEntry 作用域。

## 考虑过的选项

- Navigation 2 / 字符串路由：生态成熟，但类型安全与 Compose 集成不如 Nav3。
- 全局单例 Navigator / ViewModel 直接导航：调用方便，但导航来源分散，页面难以独立测试。
- 单返回栈：实现更简单，但切换一级页会丢失各页历史，与底部导航的用户预期不符。

## 后果

- 需要 Kotlin serialization 插件；路由参数只能传业务 ID 与可序列化值。
- 页面间数据变更依赖 Room 回推，只有选择 SKU、选择库位等瞬时结果使用 NavEntry 作用域的结果通道。
- 导航逻辑集中在 `core/navigation` 与 app 壳，Screen 保持无副作用。
