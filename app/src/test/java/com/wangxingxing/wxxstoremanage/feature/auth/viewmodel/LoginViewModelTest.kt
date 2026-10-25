/**
 * author : 王星星
 * date : 2026-10-25 15:34
 * email : 1099420259@qq.com
 * description : 覆盖登录页凭据回填、校验、用户名去空白、提交成功与失败、重复提交抑制及错误清理。
 */
package com.wangxingxing.wxxstoremanage.feature.auth.viewmodel

import com.wangxingxing.wxxstoremanage.core.common.AppError
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepository
import com.wangxingxing.wxxstoremanage.core.model.CredentialPreference
import com.wangxingxing.wxxstoremanage.core.model.CurrentUser
import com.wangxingxing.wxxstoremanage.core.model.RememberedCredentials
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginAction
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginField
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `remembered credentials prefill the form on start`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(
            rememberedCredentials = RememberedCredentials("saved", "saved-pass"),
        )
        val viewModel = LoginViewModel(repository, RecordingLogger())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("saved", state.username)
        assertEquals("saved-pass", state.password)
        assertTrue(state.rememberCredentials)
    }

    @Test
    fun `remembered credentials do not overwrite user input`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(
            rememberedCredentials = RememberedCredentials("saved", "saved-pass"),
        )
        val viewModel = LoginViewModel(repository, RecordingLogger())

        viewModel.onAction(LoginAction.UsernameChanged("typed"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("typed", state.username)
        assertEquals("", state.password)
    }

    @Test
    fun `saved opt-out keeps the checkbox unchecked and skips prefill`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(rememberEnabled = false)
        val viewModel = LoginViewModel(repository, RecordingLogger())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.rememberCredentials)
        assertEquals("", state.username)
        assertEquals("", state.password)
    }

    @Test
    fun `remember credentials toggle is forwarded to login`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository, RecordingLogger())
        advanceUntilIdle()

        viewModel.onAction(LoginAction.UsernameChanged("demo"))
        viewModel.onAction(LoginAction.PasswordChanged("pass"))
        viewModel.onAction(LoginAction.RememberCredentialsChanged(false))
        viewModel.onAction(LoginAction.Submit)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.rememberCredentials)
        assertEquals(false, repository.lastRememberCredentials)
        assertEquals(SubmitState.Success, viewModel.uiState.value.submitState)
    }

    @Test
    fun `blank fields produce validation errors without calling repository`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository, RecordingLogger())

        viewModel.onAction(LoginAction.UsernameChanged("   "))
        viewModel.onAction(LoginAction.Submit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppErrorType.Validation, state.fieldErrors[LoginField.Username])
        assertEquals(AppErrorType.Validation, state.fieldErrors[LoginField.Password])
        assertEquals(SubmitState.Idle, state.submitState)
        assertEquals(0, repository.loginCalls.get())
    }

    @Test
    fun `submit trims username and keeps password unchanged`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository, RecordingLogger())

        viewModel.onAction(LoginAction.UsernameChanged("  demo  "))
        viewModel.onAction(LoginAction.PasswordChanged(" secret "))
        viewModel.onAction(LoginAction.Submit)
        advanceUntilIdle()

        assertEquals("demo", repository.lastUsername)
        assertEquals(" secret ", repository.lastPassword)
        assertEquals("demo", viewModel.uiState.value.username)
        assertEquals(SubmitState.Success, viewModel.uiState.value.submitState)
    }

    @Test
    fun `submit failure exposes error state and logs failure`() = runTest(dispatcher) {
        val logger = RecordingLogger()
        val repository = FakeAuthRepository(
            loginResult = AppResult.Failure(
                AppError(type = AppErrorType.AuthenticationFailed, serverCode = "-1"),
            ),
        )
        val viewModel = LoginViewModel(repository, logger)

        viewModel.onAction(LoginAction.UsernameChanged("demo"))
        viewModel.onAction(LoginAction.PasswordChanged("wrong"))
        viewModel.onAction(LoginAction.Submit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SubmitState.Error(AppErrorType.AuthenticationFailed), state.submitState)
        assertEquals(AppLogPhase.Failure, logger.events.last().phase)
        assertEquals("AuthenticationFailed", logger.events.last().context["errorType"])
        assertEquals("-1", logger.events.last().context["serverCode"])
    }

    @Test
    fun `duplicate submit is ignored while request is in flight`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeAuthRepository(loginGate = gate)
        val viewModel = LoginViewModel(repository, RecordingLogger())

        viewModel.onAction(LoginAction.UsernameChanged("demo"))
        viewModel.onAction(LoginAction.PasswordChanged("pass"))
        viewModel.onAction(LoginAction.Submit)
        runCurrent()
        assertEquals(1, repository.loginCalls.get())
        assertEquals(SubmitState.Submitting, viewModel.uiState.value.submitState)

        viewModel.onAction(LoginAction.Submit)
        runCurrent()
        assertEquals(1, repository.loginCalls.get())

        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(SubmitState.Success, viewModel.uiState.value.submitState)
    }

    @Test
    fun `editing a field clears its error and submit error`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(
            loginResult = AppResult.Failure(AppError(AppErrorType.AuthenticationFailed)),
        )
        val viewModel = LoginViewModel(repository, RecordingLogger())

        viewModel.onAction(LoginAction.Submit)
        advanceUntilIdle()
        assertEquals(
            AppErrorType.Validation,
            viewModel.uiState.value.fieldErrors[LoginField.Username],
        )
        assertEquals(
            AppErrorType.Validation,
            viewModel.uiState.value.fieldErrors[LoginField.Password],
        )

        viewModel.onAction(LoginAction.PasswordChanged("wrong"))
        assertFalse(viewModel.uiState.value.fieldErrors.containsKey(LoginField.Password))
        assertTrue(viewModel.uiState.value.fieldErrors.containsKey(LoginField.Username))

        viewModel.onAction(LoginAction.UsernameChanged("demo"))
        assertFalse(viewModel.uiState.value.fieldErrors.containsKey(LoginField.Username))

        viewModel.onAction(LoginAction.Submit)
        advanceUntilIdle()
        assertEquals(
            SubmitState.Error(AppErrorType.AuthenticationFailed),
            viewModel.uiState.value.submitState,
        )

        viewModel.onAction(LoginAction.UsernameChanged("demo2"))
        assertEquals(SubmitState.Idle, viewModel.uiState.value.submitState)
    }

    @Test
    fun `password visibility toggles`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(FakeAuthRepository(), RecordingLogger())

        viewModel.onAction(LoginAction.PasswordVisibilityToggled)
        assertTrue(viewModel.uiState.value.passwordVisible)

        viewModel.onAction(LoginAction.PasswordVisibilityToggled)
        assertFalse(viewModel.uiState.value.passwordVisible)
    }
}

private class FakeAuthRepository(
    private val loginResult: AppResult<CurrentUser> = AppResult.Success(demoUser),
    private val loginGate: CompletableDeferred<Unit>? = null,
    private val rememberedCredentials: RememberedCredentials? = null,
    private val rememberEnabled: Boolean = true,
) : AuthRepository {

    private val sessionState = MutableStateFlow<CurrentUser?>(null)

    override val session: Flow<CurrentUser?> = sessionState

    val loginCalls = AtomicInteger(0)
    var lastUsername: String? = null
    var lastPassword: String? = null
    var lastRememberCredentials: Boolean? = null

    override suspend fun loadCredentialPreference(): CredentialPreference =
        CredentialPreference(
            credentials = rememberedCredentials,
            rememberEnabled = rememberEnabled,
        )

    override suspend fun login(
        username: String,
        password: String,
        rememberCredentials: Boolean,
    ): AppResult<CurrentUser> {
        loginCalls.incrementAndGet()
        lastUsername = username
        lastPassword = password
        lastRememberCredentials = rememberCredentials
        loginGate?.await()
        return loginResult
    }

    override suspend fun logout(): AppResult<Unit> = AppResult.Success(Unit)
}

private val demoUser = CurrentUser(
    id = 7L,
    username = "demo",
    displayName = "演示用户",
    email = "demo@example.com",
    avatarUrl = null,
)

private class RecordingLogger : AppLogger {

    val events = mutableListOf<AppLogEvent>()

    override fun log(event: AppLogEvent) {
        events += event
    }

    override fun debug(section: String, message: String) = Unit
}
