/**
 * author : 王星星
 * date : 2026-10-25 15:34
 * email : 1099420259@qq.com
 * description : 覆盖个人页会话观察四态、重试、退出确认与退出结果，以及重复确认抑制。
 */
package com.wangxingxing.wxxstoremanage.feature.profile.viewmodel

import app.cash.turbine.test
import com.wangxingxing.wxxstoremanage.core.common.AppError
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepository
import com.wangxingxing.wxxstoremanage.core.data.session.SessionCleaner
import com.wangxingxing.wxxstoremanage.core.model.CredentialPreference
import com.wangxingxing.wxxstoremanage.core.model.CurrentUser
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState
import com.wangxingxing.wxxstoremanage.feature.profile.model.ProfileAction
import com.wangxingxing.wxxstoremanage.feature.profile.model.ProfileUiModel
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
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
class ProfileViewModelTest {

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
    fun `emits Loading then Empty when session is absent`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)
            advanceUntilIdle()
            assertEquals(ContentState.Empty, awaitItem().content)
        }
    }

    @Test
    fun `emits Content with mapped current user`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            repository = FakeAuthRepository(MutableStateFlow(demoUser)),
        )

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)
            advanceUntilIdle()
            val content = awaitItem().content as ContentState.Content
            assertEquals(
                ProfileUiModel(
                    displayName = "演示用户",
                    username = "demo",
                    email = "demo@example.com",
                    avatarUrl = null,
                    userId = 7L,
                ),
                content.value,
            )
        }
    }

    @Test
    fun `emits Error and logs failure when session flow fails`() = runTest(dispatcher) {
        val logger = RecordingLogger()
        val viewModel = createViewModel(
            repository = FakeAuthRepository(failuresRemaining = 1),
            logger = logger,
        )

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)
            advanceUntilIdle()
            assertEquals(
                ContentState.Error(AppErrorType.NetworkUnavailable),
                awaitItem().content,
            )
        }
        assertEquals(AppLogPhase.Failure, logger.events.single().phase)
    }

    @Test
    fun `Retry reloads content after an error`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            repository = FakeAuthRepository(
                sessionState = MutableStateFlow(demoUser),
                failuresRemaining = 1,
            ),
        )

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)
            advanceUntilIdle()
            assertEquals(
                ContentState.Error(AppErrorType.NetworkUnavailable),
                awaitItem().content,
            )

            viewModel.onAction(ProfileAction.Retry)
            assertEquals(ContentState.Loading, awaitItem().content)
            advanceUntilIdle()
            assertTrue(awaitItem().content is ContentState.Content)
        }
    }

    @Test
    fun `logout request shows confirmation and dismiss hides it`() = runTest(dispatcher) {
        val cleaner = FakeSessionCleaner()
        val viewModel = createViewModel(sessionCleaner = cleaner)
        advanceUntilIdle()

        viewModel.onAction(ProfileAction.RequestLogout)
        assertTrue(viewModel.uiState.value.showLogoutConfirmation)
        assertEquals(SubmitState.Idle, viewModel.uiState.value.logoutState)

        viewModel.onAction(ProfileAction.DismissLogout)
        assertFalse(viewModel.uiState.value.showLogoutConfirmation)
        assertEquals(0, cleaner.calls.get())
    }

    @Test
    fun `confirm logout reports success after cleaner completes`() = runTest(dispatcher) {
        val cleaner = FakeSessionCleaner()
        val viewModel = createViewModel(sessionCleaner = cleaner)
        advanceUntilIdle()

        viewModel.onAction(ProfileAction.RequestLogout)
        viewModel.onAction(ProfileAction.ConfirmLogout)
        assertFalse(viewModel.uiState.value.showLogoutConfirmation)
        assertEquals(SubmitState.Submitting, viewModel.uiState.value.logoutState)

        advanceUntilIdle()
        assertEquals(SubmitState.Success, viewModel.uiState.value.logoutState)
        assertEquals(1, cleaner.calls.get())
    }

    @Test
    fun `confirm logout exposes cleaner failure`() = runTest(dispatcher) {
        val cleaner = FakeSessionCleaner(
            result = AppResult.Failure(AppError(AppErrorType.Unknown)),
        )
        val viewModel = createViewModel(sessionCleaner = cleaner)
        advanceUntilIdle()

        viewModel.onAction(ProfileAction.RequestLogout)
        viewModel.onAction(ProfileAction.ConfirmLogout)
        advanceUntilIdle()

        assertEquals(
            SubmitState.Error(AppErrorType.Unknown),
            viewModel.uiState.value.logoutState,
        )
        assertEquals(1, cleaner.calls.get())
    }

    @Test
    fun `duplicate confirm is ignored while cleaning`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val cleaner = FakeSessionCleaner(gate = gate)
        val viewModel = createViewModel(sessionCleaner = cleaner)
        advanceUntilIdle()

        viewModel.onAction(ProfileAction.RequestLogout)
        viewModel.onAction(ProfileAction.ConfirmLogout)
        runCurrent()
        assertEquals(1, cleaner.calls.get())

        viewModel.onAction(ProfileAction.ConfirmLogout)
        runCurrent()
        assertEquals(1, cleaner.calls.get())

        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(SubmitState.Success, viewModel.uiState.value.logoutState)
    }

    private fun createViewModel(
        repository: FakeAuthRepository = FakeAuthRepository(),
        sessionCleaner: FakeSessionCleaner = FakeSessionCleaner(),
        logger: RecordingLogger = RecordingLogger(),
    ): ProfileViewModel = ProfileViewModel(repository, sessionCleaner, logger)
}

private class FakeAuthRepository(
    private val sessionState: MutableStateFlow<CurrentUser?> = MutableStateFlow(null),
    private var failuresRemaining: Int = 0,
) : AuthRepository {

    override val session: Flow<CurrentUser?> = flow {
        if (failuresRemaining > 0) {
            failuresRemaining--
            throw IOException("offline")
        }
        emitAll(sessionState)
    }

    override suspend fun loadCredentialPreference(): CredentialPreference =
        CredentialPreference(credentials = null, rememberEnabled = true)

    override suspend fun login(
        username: String,
        password: String,
        rememberCredentials: Boolean,
    ): AppResult<CurrentUser> = AppResult.Success(demoUser)

    override suspend fun logout(): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeSessionCleaner(
    private val result: AppResult<Unit> = AppResult.Success(Unit),
    private val gate: CompletableDeferred<Unit>? = null,
) : SessionCleaner {

    val calls = AtomicInteger(0)

    override suspend fun endSession(): AppResult<Unit> {
        calls.incrementAndGet()
        gate?.await()
        return result
    }
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
