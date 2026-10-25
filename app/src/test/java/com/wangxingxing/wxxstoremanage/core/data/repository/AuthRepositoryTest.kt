/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 用 MockWebServer 覆盖登录成功、业务错误码、缺失 Cookie、HTTP 失败、网络异常、记住凭据与登出清理。
 */
package com.wangxingxing.wxxstoremanage.core.data.repository

import app.cash.turbine.test
import com.wangxingxing.wxxstoremanage.core.common.AppError
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.data.credential.CredentialStore
import com.wangxingxing.wxxstoremanage.core.data.session.SessionStore
import com.wangxingxing.wxxstoremanage.core.data.session.StoredSession
import com.wangxingxing.wxxstoremanage.core.model.CredentialPreference
import com.wangxingxing.wxxstoremanage.core.model.RememberedCredentials
import com.wangxingxing.wxxstoremanage.core.network.api.WanAndroidAuthApi
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AuthRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var sessionStore: FakeSessionStore
    private lateinit var credentialStore: FakeCredentialStore
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        sessionStore = FakeSessionStore()
        credentialStore = FakeCredentialStore()
        repository = createRepository(server)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `login success persists cookie and maps user without token`() = runTest {
        server.enqueue(
            jsonResponse(
                body = SUCCESS_BODY,
                setCookie = "JSESSIONID=session-123; Path=/; HttpOnly",
            ),
        )

        val result = repository.login("demo", "pass", rememberCredentials = false)

        assertTrue(result is AppResult.Success)
        val user = (result as AppResult.Success).data
        assertEquals(7L, user.id)
        assertEquals("demo", user.username)
        assertEquals("演示用户", user.displayName)
        assertEquals("demo@example.com", user.email)
        assertEquals("https://example.com/avatar.png", user.avatarUrl)
        assertEquals("JSESSIONID=session-123", sessionStore.savedSession?.cookie)
        assertEquals(7L, sessionStore.savedSession?.userId)
        assertFalse(sessionStore.savedSession.toString().contains("secret-token"))
        assertFalse(sessionStore.savedSession.toString().contains("admin"))

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/user/login", request.url.encodedPath)
        assertEquals("username=demo&password=pass", request.body?.utf8())
    }

    @Test
    fun `login success remembers credentials when requested`() = runTest {
        server.enqueue(
            jsonResponse(
                body = SUCCESS_BODY,
                setCookie = "JSESSIONID=session-123; Path=/; HttpOnly",
            ),
        )

        val result = repository.login("demo", " pass ", rememberCredentials = true)

        assertTrue(result is AppResult.Success)
        assertEquals(RememberedCredentials("demo", " pass "), credentialStore.stored)
    }

    @Test
    fun `login success clears remembered credentials when not requested`() = runTest {
        credentialStore.stored = RememberedCredentials("old", "old-pass")
        server.enqueue(
            jsonResponse(
                body = SUCCESS_BODY,
                setCookie = "JSESSIONID=session-123; Path=/; HttpOnly",
            ),
        )

        val result = repository.login("demo", "pass", rememberCredentials = false)

        assertTrue(result is AppResult.Success)
        assertNull(credentialStore.stored)
        assertFalse(credentialStore.rememberEnabled)
        assertEquals(1, credentialStore.clearCount)
    }

    @Test
    fun `credential save failure does not fail login`() = runTest {
        credentialStore.failSave = true
        server.enqueue(
            jsonResponse(
                body = SUCCESS_BODY,
                setCookie = "JSESSIONID=session-123; Path=/; HttpOnly",
            ),
        )

        val result = repository.login("demo", "pass", rememberCredentials = true)

        assertTrue(result is AppResult.Success)
        assertEquals("JSESSIONID=session-123", sessionStore.savedSession?.cookie)
    }

    @Test
    fun `load credential preference returns stored value`() = runTest {
        credentialStore.stored = RememberedCredentials("demo", "pass")

        assertEquals(
            CredentialPreference(
                credentials = RememberedCredentials("demo", "pass"),
                rememberEnabled = true,
            ),
            repository.loadCredentialPreference(),
        )
    }

    @Test
    fun `load credential preference does not remember when store fails`() = runTest {
        credentialStore.failRead = true

        assertEquals(
            CredentialPreference(credentials = null, rememberEnabled = false),
            repository.loadCredentialPreference(),
        )
    }

    @Test
    fun `login maps -1 to authentication failed and saves nothing`() = runTest {
        server.enqueue(
            jsonResponse("""{"data":null,"errorCode":-1,"errorMsg":"账号密码不匹配！"}"""),
        )

        val error = repository.login("demo", "wrong", rememberCredentials = true).errorOrNull()

        assertEquals(AppErrorType.AuthenticationFailed, error?.type)
        assertEquals("-1", error?.serverCode)
        assertNull(sessionStore.savedSession)
        assertNull(credentialStore.stored)
    }

    @Test
    fun `login maps -1001 to session expired`() = runTest {
        server.enqueue(
            jsonResponse("""{"data":null,"errorCode":-1001,"errorMsg":"登录已失效"}"""),
        )

        val error = repository.login("demo", "pass", rememberCredentials = false).errorOrNull()

        assertEquals(AppErrorType.SessionExpired, error?.type)
        assertEquals("-1001", error?.serverCode)
        assertNull(sessionStore.savedSession)
    }

    @Test
    fun `login maps unknown business code to unknown with server code`() = runTest {
        server.enqueue(
            jsonResponse("""{"data":null,"errorCode":-2,"errorMsg":"未知业务错误"}"""),
        )

        val error = repository.login("demo", "pass", rememberCredentials = false).errorOrNull()

        assertEquals(AppErrorType.Unknown, error?.type)
        assertEquals("-2", error?.serverCode)
    }

    @Test
    fun `login rejects success response without session cookie`() = runTest {
        server.enqueue(jsonResponse(SUCCESS_BODY))

        val error = repository.login("demo", "pass", rememberCredentials = true).errorOrNull()

        assertEquals(AppErrorType.Unknown, error?.type)
        assertNull(sessionStore.savedSession)
        assertNull(credentialStore.stored)
    }

    @Test
    fun `login maps http 401 to authentication failed`() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .code(401)
                .addHeader("Content-Type", "application/json")
                .body("""{"errorCode":401,"errorMsg":"unauthorized"}""")
                .build(),
        )

        val error = repository.login("demo", "pass", rememberCredentials = false).errorOrNull()

        assertEquals(AppErrorType.AuthenticationFailed, error?.type)
    }

    @Test
    fun `login maps http 500 to service unavailable`() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .code(500)
                .addHeader("Content-Type", "application/json")
                .body("""{"errorCode":500,"errorMsg":"server"}""")
                .build(),
        )

        val error = repository.login("demo", "pass", rememberCredentials = false).errorOrNull()

        assertEquals(AppErrorType.ServiceUnavailable, error?.type)
    }

    @Test
    fun `login maps io failure to network unavailable`() = runTest {
        val offlineServer = MockWebServer()
        offlineServer.start()
        val offlineRepository = createRepository(offlineServer)
        offlineServer.close()

        val error = offlineRepository.login("demo", "pass", rememberCredentials = false)
            .errorOrNull()

        assertEquals(AppErrorType.NetworkUnavailable, error?.type)
    }

    @Test
    fun `logout sends stored cookie and clears local session when server fails`() = runTest {
        sessionStore.seed(storedSession())
        server.enqueue(
            MockResponse.Builder()
                .code(500)
                .addHeader("Content-Type", "application/json")
                .body("""{"errorCode":500,"errorMsg":"server"}""")
                .build(),
        )

        val result = repository.logout()

        assertTrue(result is AppResult.Success)
        assertEquals(1, sessionStore.clearCount)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/user/logout/json", request.url.encodedPath)
        assertEquals("JSESSIONID=abc", request.headers["Cookie"])
    }

    @Test
    fun `logout reports failure when local session cannot be cleared`() = runTest {
        sessionStore.seed(storedSession())
        sessionStore.failClear = true
        server.enqueue(jsonResponse("""{"data":null,"errorCode":0,"errorMsg":""}"""))

        val result = repository.logout()

        assertTrue(result is AppResult.Failure)
        assertEquals(AppErrorType.Unknown, (result as AppResult.Failure).error.type)
    }

    @Test
    fun `session exposes stored user`() = runTest {
        sessionStore.seed(storedSession())

        repository.session.test {
            val user = awaitItem()
            assertEquals("demo", user?.username)
            assertEquals("演示用户", user?.displayName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createRepository(server: MockWebServer): AuthRepositoryImpl {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return AuthRepositoryImpl(
            api = retrofit.create(WanAndroidAuthApi::class.java),
            sessionStore = sessionStore,
            credentialStore = credentialStore,
            logger = RecordingLogger(),
        )
    }

    private fun jsonResponse(body: String, setCookie: String? = null): MockResponse =
        MockResponse.Builder()
            .code(200)
            .addHeader("Content-Type", "application/json")
            .apply { setCookie?.let { addHeader("Set-Cookie", it) } }
            .body(body)
            .build()

    private fun storedSession(): StoredSession = StoredSession(
        cookie = "JSESSIONID=abc",
        userId = 7L,
        username = "demo",
        displayName = "演示用户",
    )

    private fun AppResult<*>.errorOrNull(): AppError? =
        (this as? AppResult.Failure)?.error

    private companion object {
        const val SUCCESS_BODY =
            """{"data":{"id":7,"username":"demo","nickname":"演示用户","email":"demo@example.com","icon":"https://example.com/avatar.png","token":"secret-token","admin":true},"errorCode":0,"errorMsg":""}"""
    }
}

private class FakeSessionStore : SessionStore {

    private val state = MutableStateFlow<StoredSession?>(null)

    override val session: Flow<StoredSession?> = state

    var savedSession: StoredSession? = null
    var clearCount = 0
    var failClear = false

    fun seed(session: StoredSession) {
        state.value = session
    }

    override suspend fun read(): StoredSession? = state.value

    override suspend fun save(session: StoredSession) {
        savedSession = session
        state.value = session
    }

    override suspend fun clear() {
        if (failClear) {
            throw IOException("clear failed")
        }
        clearCount++
        state.value = null
    }
}

private class FakeCredentialStore : CredentialStore {

    var stored: RememberedCredentials? = null
    var rememberEnabled = true
    var saveCount = 0
    var clearCount = 0
    var failSave = false
    var failRead = false

    override suspend fun read(): CredentialPreference {
        if (failRead) {
            throw IOException("read failed")
        }
        return CredentialPreference(credentials = stored, rememberEnabled = rememberEnabled)
    }

    override suspend fun save(credentials: RememberedCredentials) {
        if (failSave) {
            throw IOException("save failed")
        }
        saveCount++
        stored = credentials
        rememberEnabled = true
    }

    override suspend fun clear() {
        clearCount++
        stored = null
        rememberEnabled = false
    }
}

private class RecordingLogger : AppLogger {

    val events = mutableListOf<AppLogEvent>()

    override fun log(event: AppLogEvent) {
        events += event
    }

    override fun debug(section: String, message: String) = Unit
}
