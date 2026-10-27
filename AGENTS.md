# AGENTS.md

> 本文件是 AI 与人类协作者的交付约束。术语以 [GLOSSARY.md](GLOSSARY.md) 为准，架构取舍的原因见 [docs/adr](docs/adr/)。
> 基础架构已搭建：Hilt、Room、Retrofit/OkHttp、Navigation 3 与 Compose 已接入并通过构建；登录、会话门与个人页已接入 WanAndroid 临时认证源；实际实现范围与验证状态见第 10 节。禁止声称未执行的检查已通过。

## 1. 项目与边界

WXXStoreManage 是 Android 仓储管理 App：多仓库、SKU 库存、入库、出库、盘点，服务管理员与仓管员。

- 架构：单 `:app` 模块，MVVM + UiState；阅读路径固定为 `Route → Screen → UiState/Action/Effect → ViewModel → Repository`。
- MVP 范围：登录与角色、商品/SKU、仓库与库位、库存查询、入库单、出库单、盘点单、小票打印、清点发货。
- 角色权限：Admin 拥有全部操作权限，并可作废单据、审核盘点、维护基础资料；Operator 只能创建与执行入库、出库、盘点录入，不能作废或审核。UI 隐藏无权限操作，服务端仍必须校验。
- 单据状态：入库/出库为 `Draft → Pending → Completed / Cancelled`；盘点为 `Draft → Counting → PendingReview → Completed / Cancelled`。现存量只在 Completed 时变更（出库在 Pending 先占用锁定量），Completed 后不可编辑；作废不改变现存量，出库作废释放锁定量。
- 明确非目标：离线写队列、批次/序列号/保质期、单位换算、供应商/采购/报表、扫码、多语言、平板双栏、WorkManager 定时同步、动态取色。
- 当前阶段：登录链路完成，登录页支持记住账号密码（Keystore 加密保存，仅回填表单，不自动登录）。Home / Inventory 已打通 Room 只读路径；Documents 为无业务数据的入口页；Profile 展示当前登录用户并支持退出登录。认证使用 WanAndroid 开放 API 作为可替换的临时认证源，由 `AuthRepository` 隔离。单据、同步与业务写操作尚未实现。
- 遗留模块：小票打印与清点发货属于首期 MVP 的核心业务流程，旧项目已完成实现，后续以 Java/Kotlin/XML 源码并入方式迁移并少量适配；迁移前必须先完成 ADR（宿主、会话与返回栈、构建能力），边界与待决项见 [legacy/README.md](app/src/main/java/com/wangxingxing/wxxstoremanage/legacy/README.md)。

## 2. 技术栈与版本策略

- Kotlin、Jetpack Compose、Material 3、MVVM、StateFlow、Kotlin Coroutines、Hilt、Retrofit + OkHttp、Room、DataStore、Paging 3、Navigation 3、Coil。
- 已接入：Compose / Material 3、Hilt、Room、Retrofit + OkHttp、Navigation 3、kotlinx-serialization、Coroutines、DataStore、Coil（用户头像）、logging-interceptor（仅 debug）。Paging 3 尚未接入，接入前不得在交付中声称已使用。
- Hilt 与 Room 统一使用 KSP，禁止 kapt。
- 路由使用 kotlinx-serialization；Nav3 的 ViewModel 作用域使用 `lifecycle-viewmodel-navigation3`。
- Java 17；minSdk 24；compileSdk/targetSdk 37。
- Gradle daemon JVM 由 `gradle/gradle-daemon-jvm.properties` 固定为 Java 17，本地与 CI 的 `setup-java` 必须安装同一版本：该文件要求的版本高于机器可用 JDK 时，Gradle 会在任何任务开始前直接失败（`Cannot find a Java installation ...`），不会进入配置阶段；修改该文件需同步 `.github/workflows/ci.yml` 的 `java-version`。
- 保留 Java 源码与 XML 资源/layout 编译能力，禁止排除 `java`/`res` 源集或关闭资源编译；ViewBinding 按需开启，DataBinding 不引入。
- AGP 固定在 9.4.0（Android Studio 2026.2 支持上限）；截图测试因此使用 `com.android.compose.screenshot` 插件，AGP 9.5+ 的内置测试套件不可用。
- 所有版本只写在 `gradle/libs.versions.toml`。禁止硬编码版本、动态版本、`+` 与 SNAPSHOT。
- 新增依赖必须说明用途、替代方案与维护状态；优先 Jetpack 官方库。
- 引入依赖后必须实际编译验证，不以“应该可以”作为结论。

## 3. 包结构与依赖方向

```text
com.wangxingxing.wxxstoremanage
├── app/                  Application、MainActivity、AppShell、entryProvider、DI 聚合
├── core/
│   ├── model/            领域模型
│   ├── data/             Repository 接口与实现、同步、mapper
│   ├── database/         Room Entity / DAO / Database
│   ├── network/          Retrofit、OkHttp、DTO、鉴权拦截器
│   ├── designsystem/     主题与无业务组件
│   ├── ui/               ContentState、RefreshState、错误文案映射、通用 UI
│   ├── navigation/       AppRoute、Navigator、NavigationState
│   └── common/           AppResult、AppError、AppLogger、Dispatcher 限定符
├── legacy/               遗留模块源码隔离区，规则见 legacy/README.md
└── feature/
    ├── auth/             登录
    ├── home/             首页
    ├── inventory/        库存查询与商品详情
    ├── documents/        单据入口与三类单据列表
    ├── inbound/          入库单
    ├── outbound/         出库单
    ├── stocktaking/      盘点单
    └── profile/          我的
```

- 依赖方向：`feature/* → core/*`；`core/data → core/database + core/network + core/model`；`app → feature/* + core/* + legacy`；`legacy → core/*`；feature 与 legacy 互不依赖。
- 禁止：feature 之间互相引用；core 反向依赖 feature；legacy 反向依赖 feature；UI 直接使用 DTO、Room Entity、Retrofit 或 DAO。
- 不设 UseCase 层。Repository 接口与实现都在 `core/data`，领域模型在 `core/model`。
- 每个 feature 内固定子包：`route/`、`screen/`、`viewmodel/`、`model/`、`mapper/`；路由键由 feature 声明，由 app 壳注册。
- 命名：`XxxRoute`、`XxxScreen`、`XxxViewModel`、`XxxUiState`、`XxxAction`、`XxxEffect`、`XxxUiModel`、`mapToXxxUiModel()`。
- 方法与业务字段命名表达业务语义：`refreshInventory()`、`submitInboundOrder()`、`loadSkuDetail()`；禁止 `getData()`、`request()`、`flag` 这类命名。
- 禁止 `Utils`、`CommonManager`、`BaseHelper` 这类模糊容器；确需复用时按职责命名并放入对应 core 包。
- 所有手写 Kotlin/Java 文件（含测试）必须在 `package` 声明前使用统一文件头；生成代码、Gradle 脚本与资源 XML 不适用：

```kotlin
/**
 * author : 王星星
 * date : yyyy-MM-dd HH:mm
 * email : 1099420259@qq.com
 * description : 文件职责、边界与需要特别注意的约束
 */
```

- `date` 写入文件创建时的本地日期与 24 小时制时间，编辑或重命名不更新；新建文件使用当时时间。
- `description` 使用中文，说明该文件的职责与边界；只有存在非显而易见的约束时才补充“注意：…”，不得只重复类名或包名。
- 触碰到的本仓库旧 Kotlin 文件若缺少文件头，应在本次变更中补齐；整包导入的遗留文件保留原文件头，见下方规则。
- 手写 Java 遵守同一文件头、中文注释与 Java 17；空安全使用 androidx.annotation；禁止 Java 侧 Hilt/Dagger 注解与 kapt，测试用 Kotlin。
- 整包导入的遗留文件保留原文件头与英文注释；新增与实质修改遵守本仓库规范，不追溯补头、不批量翻译注释；legacy 区边界与迁移 ADR 待决项见 [legacy/README.md](app/src/main/java/com/wangxingxing/wxxstoremanage/legacy/README.md)。
- 代码注释统一使用中文，只写“为什么”和非显而易见的约束，不复述代码：
  - 必须注释：业务规则与状态流转前提、协程取消与重试等易错约束、跨层隐式约定、SQL 口径、迁移与安全限制。
  - 公共 API 的契约（参数含义、返回值、异常）需要说明时使用 KDoc；其余使用 `//` 放在被说明代码的上方。
  - 禁止逐行注释、禁止只复述代码行为的注释、禁止在本仓库新写英文行内注释（整包导入的遗留文件除外）；注释随代码同步修改，过期注释按缺陷处理。

## 4. 标准阅读路径与页面模板

每个页面必须能沿同一条路径读完：

1. `XxxRoute`：获取 ViewModel、收集 state、消费 effect、连接导航。
2. `XxxScreen`：`internal`、纯 UI、可 Preview，只渲染 state 并转发事件。
3. `XxxUiState / XxxAction / XxxEffect`：页面契约。
4. `XxxViewModel`：状态归约、调用 Repository。
5. `Repository`：数据来源与错误映射。

**示例 A：列表页 `InventoryList`（Route + Screen）**

```kotlin
@Composable
fun InventoryListRoute(
    navigator: Navigator,
    viewModel: InventoryListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InventoryListScreen(
        state = state,
        onAction = viewModel::onAction,
        onSkuClick = { skuId -> navigator.openSkuDetail(skuId) },
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
internal fun InventoryListScreen(
    state: InventoryListUiState,
    onAction: (InventoryListAction) -> Unit,
    onSkuClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        when (val content = state.content) {
            ContentState.Loading -> LoadingIndicator(Modifier.padding(padding))
            ContentState.Empty -> EmptyState(Modifier.padding(padding))
            is ContentState.Error -> ErrorState(content.type, onRetry = { onAction(InventoryListAction.Retry) })
            is ContentState.Content -> InventoryList(content.value, onSkuClick, Modifier.padding(padding))
        }
    }
}
```

- Route 是唯一允许接触 Navigator 与 Effect 的层。
- 无业务数据与交互的纯入口页可以只有 `Route + Screen`；一旦接入数据或交互，必须补齐 UiState / Action / ViewModel / Repository，不得以静态文案长期替代。
- Screen 不读取 ViewModel、不持有 Navigator/Context、不启动网络或数据库。
- 用户点击导致的导航用回调；异步完成后的导航用 Effect。
- 交互统一走 Action，避免堆叠含义相近的 lambda；导航与纯展示回调除外。
- Preview 使用显式假数据；禁止 Hilt、网络、数据库与真实 SDK。

## 5. UiState、Action 与 Effect

- 每个页面一个 `XxxUiState` data class；状态变更集中在少量命名明确的方法中，禁止在各处散落 `_uiState.update`。
- 首屏主内容用 `ContentState<T>`：

```kotlin
sealed interface ContentState<out T> {
    data object Loading : ContentState<Nothing>
    data object Empty : ContentState<Nothing>
    data class Content<T>(val value: T) : ContentState<T>
    data class Error(val type: AppErrorType) : ContentState<Nothing>
}
```

- 仅在页面确实支持刷新或分页时增加正交状态：
  - `RefreshState`：`Idle / Refreshing / Failed(AppErrorType)`
  - `AppendState`：`Idle / Loading / Failed(AppErrorType) / End`
  - 使用 Paging 3 的列表直接使用官方 `LoadState`，不再自造 `AppendState`。
- 写操作用独立 `SubmitState`：`Idle / Submitting / Success / Error(AppErrorType)`。
- 表单用显式字段 + `fieldErrors: Map<Field, AppErrorType>`。
- 禁止：`isLoading` / `isEmpty` / `isError` 布尔组合；`errorMessage: String`；把 `Throwable`、服务端原文或 `Context` 放进 UiState。
- 同一事件不得既写入 State 又发送 Effect。State 承载可持续观察的结果，Effect 只承载一次性行为。
- Effect 使用 `Channel<XxxEffect>(BUFFERED).receiveAsFlow()`，只在 Route 中消费：

```kotlin
LaunchedEffect(viewModel) {
    viewModel.effects.collect { effect ->
        when (effect) {
            InboundOrderDetailEffect.Saved -> navigator.goBack()
        }
    }
}
```

- 禁止用 Boolean 状态模拟一次性事件，禁止在组合过程中弹 Toast、导航或启动请求。

**示例 B：写操作页 `InboundOrderDetail`（Submit + Effect）**。Route 与示例 A 同构；Screen 依据 `SubmitState` 禁用重复提交，提交成功后 ViewModel 发送 `Saved`，由 Route 执行 `goBack()`。

```kotlin
data class InboundOrderDetailUiState(
    val content: ContentState<InboundOrderDetailUiModel> = ContentState.Loading,
    val submitState: SubmitState = SubmitState.Idle,
)

sealed interface InboundOrderDetailAction {
    data object Submit : InboundOrderDetailAction
    data object Retry : InboundOrderDetailAction
}

sealed interface InboundOrderDetailEffect {
    data object Saved : InboundOrderDetailEffect
}
```

## 6. ViewModel 与协程

- 对外只暴露 `StateFlow<XxxUiState>` 与 `Flow<XxxEffect>`；不得暴露 `MutableStateFlow`。
- ViewModel 不持有 Navigator、Context、View、Composable；导航结果通过 Effect 表达。
- 所有协程归属 `viewModelScope` 或注入的受控作用域；禁止 `GlobalScope` 与无归属 `CoroutineScope`。
- 不捕获、不吞掉 `CancellationException`；需要清理时先 `rethrow`。
- 调度器通过 `@IoDispatcher` / `@DefaultDispatcher` 注入，禁止在 UI 或 ViewModel 散落 `Dispatchers.IO`。
- 重复请求策略必须显式：取消前一次（保留 Job 并 `cancel`）、忽略进行中请求、或并行执行，三者选一并在代码中可见。
- 一个 Action 只触发一个业务流程；不得在一个 Action 中悄悄串联无关请求。
- 首次加载由 ViewModel `init` 触发；刷新、重试、提交只能由 Action 触发。每个 NavEntry 有独立 ViewModel 实例，返回重进不复用旧页面状态。
- 写操作不得做乐观库存更新；以服务端确认后的 Room 数据为准。

## 7. 数据、错误与日志

- 读路径以 Room 为唯一数据源；网络同步只写 Room，页面观察 Repository 暴露的 `Flow`。
- 写路径在线优先：入库、出库、盘点提交必须等待服务端确认；MVP 不实现离线写队列。
- 同步时机固定为启动、进入页面（stale-while-revalidate）与手动刷新；不接 WorkManager 定时同步；切换仓库即切换查询域；分页同步按 `updatedAt` 增量拉取。
- 同步失败保留旧数据，页面用 `RefreshState.Failed` 展示，不清空缓存。
- 库存唯一键为 `skuId + warehouseId`，数量为整数；库位只展示，不参与库存聚合。
- Paging 3 仅用于库存列表与三类单据列表：`RemoteMediator` 将服务端分页写入 Room，列表使用 `Room PagingSource`；商品选择器、仓库列表等基础资料用普通 `Flow`；详情页不用 Paging。
- 写返回 `AppResult<T>`：`Success(data)` / `Failure(AppError)`；读返回 `Flow<T>`。异常不得穿透到 ViewModel。
- DTO、Room Entity、领域模型、UiModel 各自独立，由 mapper 转换；DTO/Entity 不进入 feature 层。
- `AppErrorType`：`NetworkUnavailable / SessionExpired / AuthenticationFailed / ServiceUnavailable / Validation / Conflict / NotFound / Unknown`。
- `AuthenticationFailed` 只表示登录凭据未被接受；`SessionExpired` 只表示已有会话失效，两者不得混用。
- `AppError(type, cause, serverCode?)` 在数据层映射；UI 用 `AppErrorType` 映射 string resource，绝不直接展示 `throwable.message`。
- 映射约定：`IOException` → `NetworkUnavailable`；HTTP 401 → `SessionExpired`；404 → `NotFound`；409 → `Conflict`；422 → `Validation`；5xx → `ServiceUnavailable`；其余 → `Unknown`。
- 日志统一使用注入的 `AppLogger`（不引入 Timber），字段至少包含 `feature`、`action`、`phase`（start/success/failure/cancel）、上下文与耗时；异常日志保留 throwable。
- 禁止裸 `Log.e(tag, "error")`。
- 401 由 OkHttp `Authenticator` 单飞刷新；登出与刷新失败走同一清理路径：清会话、清 Room 业务数据、清返回栈并回到登录页，不在各 ViewModel 重复判断。
- Room 必须导出 schema；release 禁止 `fallbackToDestructiveMigration`；结构变更必须提供 migration。
- DataStore 默认存非敏感偏好（当前仓库、主题、筛选条件、最后同步时间）。会话与记住的登录凭据各自使用独立 DataStore 并以 Keystore 密文存储，两者的备份与设备迁移规则必须排除对应文件。

## 8. 导航

- 使用 Navigation 3；路由键 `@Serializable` 并实现 `AppRoute : NavKey`，由 feature 声明。
- 带参数的页面由 Route 从 NavKey 读取参数，通过 Hilt assisted injection（`@AssistedInject` + `hiltViewModel(creationCallback = ...)`）创建 ViewModel；禁止用全局单例或猜测 `SavedStateHandle` 传参。
- 底部四个一级页：首页、库存、单据、我的，每页各自持有返回栈。`feature/documents` 是单据入口，负责三类单据列表并跳转到对应 detail feature。
- `Navigator` 只提供 `navigate(AppRoute)`、`goBack()`、`replaceTop(AppRoute)`；feature 以扩展函数提供语义化入口，如 `Navigator.openSkuDetail(skuId)`。
- app 壳通过 `entryProvider` 注册路由；`NavDisplay` 接入 `rememberViewModelStoreNavEntryDecorator` 与 `onBack = navigator::goBack`。
- 页面之间只传业务 ID 等可序列化值，不传对象；数据变更由 Room 回推。
- 仅选择 SKU、选择库位等瞬时结果使用 NavEntry 作用域的结果通道，不建全局事件总线。
- 会话失效时清空返回栈并导航到登录页；登录成功回到起始一级页。
- 认证边界由根级 `SessionGate` 按会话状态切换：`Loading` 显示启动占位，`LoggedOut` 显示登录页，`LoggedIn` 显示主壳。登录页不进入 Nav3 返回栈；登录成功不发送导航 Effect，这是对第 4 节「异步导航用 Effect」的例外，因为会话是可持续观察的状态，且登录、登出、会话失效必须共用同一切换路径。

## 9. Compose、Preview 与可访问性

- 参数顺序：必需数据 → 事件回调 → `modifier` → 可选视觉参数；`modifier` 默认 `Modifier` 且根节点必须实际应用。
- 通用组件状态提升，不读取 ViewModel；业务逻辑不写进 Composable。
- `LaunchedEffect` / `DisposableEffect` 使用稳定且语义正确的 key；副作用不得出现在组合过程中。
- 不为“优化”随意添加 `remember`、`derivedStateOf`、`@Stable`、`@Immutable`；`@Immutable` 只能用于真正不可变、无可变集合的类型。
- 列表项必须提供稳定 key。
- 所有用户可见文案走 string resource，默认 `values` 为中文；禁止硬编码中文或英文文案。
- Material 3 使用固定品牌色与深色主题，不启用 dynamic color；保持 edge-to-edge，正确处理系统栏与 IME inset。
- 360dp 手机优先；不硬编码宽度，不做平板双栏；触摸目标不小于 48dp，提供 contentDescription，支持字体缩放。
- Coil 只用于商品图与用户头像，统一占位图与失败图，并按显示尺寸限制请求。

## 10. 测试与验证

- 已接入：JUnit4 + `kotlinx-coroutines-test` + Turbine + MockWebServer（本地）、Robolectric + Compose UI Test（本地 UI）、Hilt Android Testing + Room Testing（设备）、Jacoco（本地覆盖率）、Compose Preview Screenshot Testing（`com.android.compose.screenshot` 插件）、GitHub Actions CI。
- 已覆盖：错误映射、Stock 语义、Home / Inventory / Login / Profile / SessionGate ViewModel 的状态与重试、AuthRepository 登录与登出、记住凭据的保存/清除/失败降级与登录页回填、DefaultSessionCleaner 串行清理、Logcat 超长日志的 UTF-8 字节分片；LoginScreen 的 Robolectric 渲染与交互；设备侧 StockDao / CatalogDao 的 SQL 口径、外键级联与 upsert、AppDatabase v1 schema 校验、凭据密文落盘与清除、SessionGate 的登录失败与会话切换（Hilt `@BindValue` 替换数据层）；Login / Home / Inventory / Documents / Profile 五个页面在九种窗口尺寸、空态、错误态与 1.5 倍字体下的截图基准；Java/XML 兼容探针（debug 手写 Java 类 + layout，由 Robolectric 测试调用并 inflate）。
- 未接入：ktlint、detekt。
- 测试分层：
  - `src/test`（含仅 debug 变体的 `src/testDebug`）：纯 JVM 单元测试与 Robolectric Compose 测试；Robolectric 的 `android-all-instrumented` 由 `:app:syncRobolectricDependencies` 预先同步到 `app/build/robolectric-deps`，测试进程不联网下载。
  - `src/androidTest`：真实 SQLite 的 Room DAO / migration 测试与 Hilt 注入的 Compose 设备测试；测试方法名禁止含空格，DEX 038 不接受该类名。
  - `src/screenshotTest`：Layoutlib 渲染的 Compose 截图测试，基准图存放在 `app/src/screenshotTestDebug/reference/`；Preview 名称不要包含小数点，否则基准图文件名会退化为哈希值。
- 必须测试：ViewModel 状态转换（Loading → Content/Empty/Error、Submit 成功/失败、重试）、mapper、错误映射、DAO 与 migration、同步逻辑。
- Compose UI 测试只覆盖关键路径：登录、入库提交、盘点审核。
- 截图测试使用 `com.android.compose.screenshot` 插件，需同时开启 `gradle.properties` 与模块 `experimentalProperties` 中的 `android.experimental.enableScreenshotTest`，插件转正后移除这两个开关；新增或修改 Screen 的视觉行为后必须重新生成并提交基准图。
- 截图基准只在 macOS 上生成与校验，不纳入 Linux CI：Layoutlib 的字体与光栅化跨平台不一致，混跑会产生大量误报；如需在 CI 校验，必须改用同一平台（如固定的 Linux 容器）重新生成全部基准图。
- 当前可用命令（2026-10-26 复跑构建、单元测试、Lint 与 release 单测编译；截图、设备测试与覆盖率命令为 2026-10-25 记录）：
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:lintDebug`
  - `./gradlew :app:testDebugUnitTest`（71 个用例）
  - `./gradlew :app:compileReleaseUnitTestKotlin`（验证 release 单测编译不受 debug 专属测试影响）
  - `./gradlew :app:validateDebugScreenshotTest`（60 张基准图，本地 Layoutlib）
  - `./gradlew :app:updateDebugScreenshotTest`（有意变更 UI 后更新基准图）
  - `./gradlew :app:connectedDebugAndroidTest`（16 个用例，需连接设备或模拟器）
  - `./gradlew :app:jacocoTestReport`（覆盖率报告：`app/build/reports/jacoco/jacocoTestReport`）
- CI 配置为 `.github/workflows/ci.yml`：构建、Lint、单元测试与覆盖率在 Ubuntu 上执行，connectedDebugAndroidTest 在 CI 模拟器上执行；截图测试仅本地执行。该工作流已落盘但尚未在 GitHub Actions 实际运行，首次推送后需确认结果。新增门禁后必须在此更新命令与状态。
- 交付必须给出实际执行过的命令与结果；未执行的检查写明原因，不得虚构通过。

## 11. AI-Generated Code Requirements

- 分析代码流程或定位代码时优先调用 CodeGraph MCP 的 `codegraph_explore`，一次取得相关符号源码与调用链后不再重复 Read；仅当索引未覆盖（资源 XML、Gradle 脚本、未索引文件）或返回信息不足时退回 Grep/Read，并在交付中说明原因。
- 实现前列出关键假设、影响范围与可验证的完成条件。
- 每一行变更都能追溯到当前需求；不进行无关重构或格式化。
- 优先复用现有模式；新增抽象前说明它消除的实际重复或复杂度。
- 不通过 `catch Exception`、空实现、默认成功、硬编码延迟或忽略返回值掩盖问题。
- 不新增无责任人的 TODO、占位实现、演示数据或永远不会触发的兜底分支。
- 不使用 `@Suppress` 消除尚未理解的警告；先定位原因。
- 修改状态模型时，列出 Loading、Content、Empty、Error、Refresh、Submit、Retry 的行为。
- 修改协程代码时，说明生命周期、取消方式、并发策略与异常传播路径。
- 修改 Compose UI 时，保证 Screen 可独立 Preview，并优先将业务逻辑移出 Composable。
- 交付报告包含：修改文件、行为变化、执行过的验证、未执行检查及原因。
- 不得声称已通过尚未接入的检查或测试。
- AI 新建或修改 Kotlin/Java 文件时必须遵守第 3 节的文件头与中文注释规则；不得为凑数逐行注释。
- 术语变更同步更新 `GLOSSARY.md`；产生难回退决策时新增 ADR，而不是把原因埋进代码注释。

## 12. 文档维护

- 本文件控制在 300 行以内，只放约束与阅读路径；术语放 [GLOSSARY.md](GLOSSARY.md)，原因放 [docs/adr](docs/adr/)。
- 新增 feature 时：建包 → 声明路由 → app 壳注册 → 补测试 → 必要时更新术语表。
- ADR 只在“难回退、缺少上下文会令人意外、存在真实取舍”三者同时成立时新增。
- 提交前按 [.github/pull_request_template.md](.github/pull_request_template.md) 填写状态覆盖、并发策略、日志安全与验证证据。
- 文档与代码冲突时，先确认哪个是预期行为，再同步修改另一方；不要默默遵守过期文档。
- 需求链接分析与任务拆解使用用户级 `requirement-to-plan` skill：先抓取并批量澄清，确认后输出 `docs/requirements/<doc-id>/analysis.md` 与 `tasks.md`。
- 需求原文快照只存本地缓存，不提交仓库；分析结论中的术语与范围变更仍按本文件与 [GLOSSARY.md](GLOSSARY.md) 的规则确认。
