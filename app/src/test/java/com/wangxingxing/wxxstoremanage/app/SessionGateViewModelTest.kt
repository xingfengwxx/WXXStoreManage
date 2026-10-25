/**
 * author : 王星星
 * date : 2026-10-25 15:34
 * email : 1099420259@qq.com
 * description : 验证门状态从 Loading 进入 LoggedOut 或 LoggedIn，会话流异常按未登录处理。
 */
package com.wangxingxing.wxxstoremanage.app

import app.cash.turbine.test
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepository
import com.wangxingxing.wxxstoremanage.core.model.CurrentUser
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionGateViewModelTest {

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
    fun `starts Loading then follows session changes`() = runTest(dispatcher) {
        val sessionState = MutableStateFlow<CurrentUser?>(null)
        val viewModel = SessionGateViewModel(
            FakeAuthRepository(session = sessionState),
            RecordingLogger(),
        )

        viewModel.sessionState.test {
            assertEquals(SessionState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(SessionState.LoggedOut, awaitItem())

            sessionState.value = demoUser
            advanceUntilIdle()
            assertEquals(SessionState.LoggedIn, awaitItem())

            sessionState.value = null
            advanceUntilIdle()
            assertEquals(SessionState.LoggedOut, awaitItem())
        }
    }

    @Test
    fun `session flow failure falls back to LoggedOut and logs`() = runTest(dispatcher) {
        val logger = RecordingLogger()
        val viewModel = SessionGateViewModel(
            FakeAuthRepository(
                session = flow { throw IOException("broken session store") },
            ),
            logger,
        )

        viewModel.sessionState.test {
            assertEquals(SessionState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(SessionState.LoggedOut, awaitItem())
        }
        assertEquals(AppLogPhase.Failure, logger.events.single().phase)
    }
}

private class FakeAuthRepository(
    override val session: Flow<CurrentUser?>,
) : AuthRepository {

    override suspend fun login(username: String, password: String): AppResult<CurrentUser> =
        AppResult.Success(demoUser)

    override suspend fun logout(): AppResult<Unit> = AppResult.Success(Unit)
}

private val demoUser = CurrentUser(
    id = 7L,
    username = "demo",
    displayName = "演示用户",
    email = null,
    avatarUrl = null,
)

private class RecordingLogger : AppLogger {

    val events = mutableListOf<AppLogEvent>()

    override fun log(event: AppLogEvent) {
        events += event
    }

    override fun debug(section: String, message: String) = Unit
}
