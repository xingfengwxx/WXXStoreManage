# 会话持久化与乐观恢复

状态：已接受（2026-10-25）

登录成功后必须把会话凭据与当前用户保存下来，否则杀进程重开就要重新登录。决定持久化真实会话：服务端下发的 Cookie 与 `id / username / displayName / email / avatarUrl` 放进同一个 `StoredSession`，用 Android Keystore 的 AES-GCM 整体加密，密文写入独立的 DataStore Preferences 文件；不保存 `token`、`password` 与 `admin`。启动时直接恢复并进入应用，不额外调用服务端校验；当前唯一的受保护调用是 logout，未来任何受保护接口返回 `errorCode == -1001` 时走统一清理并回登录页。

Cookie 由数据层手动读取 `Set-Cookie`、登出时显式附加 `Cookie` 头，不使用 OkHttp `CookieJar`。根级 `SessionGate` 观察会话状态：`Loading` 显示启动占位，`LoggedOut` 显示登录页，`LoggedIn` 显示主壳；登录成功不发送导航 Effect，登出与会话失效由同一状态切换清空主壳与返回栈。

## 考虑过的选项

- 只保存本地身份、不保存 Cookie：没有凭据落盘，但无法调用服务端 logout，也无法访问未来的受保护接口。
- 仅内存保存、重启重登：最简单，但每次启动都要重新认证。
- 启动时调用受保护接口校验会话：能发现服务端过期，但需要借用无关业务接口，且离线或接口抖动会把已登录用户挡在门外。
- 会话明文存 SharedPreferences：实现最省事，但 WanAndroid 文档称登录 cookie 中含账号密码，明文落盘等同于泄露可重放凭据。

## 后果

- 会话凭据离开进程后只以 AES-GCM 密文存在；Keystore 密钥不随备份迁移，备份规则必须排除会话 DataStore 文件；解密失败按未登录处理并清除坏数据。
- 服务端会话可能已过期而客户端仍显示登录态，这是乐观恢复的已知代价，`-1001` 是唯一的纠正信号。
- `SessionStore` 是会话的唯一入口；feature 不接触 Cookie、DTO 或 Keystore。
- DataStore 的默认约定仍只适用于非敏感偏好；会话使用独立文件，避免与普通偏好混存。
