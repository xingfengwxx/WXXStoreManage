/**
 * author : 王星星
 * date : 2026-10-25 15:34
 * email : 1099420259@qq.com
 * description : 验证退出顺序、失败时不清理业务数据，以及并发退出由 Mutex 串行化。
 */
package com.wangxingxing.wxxstoremanage.core.data.session

import com.wangxingxing.wxxstoremanage.core.common.AppError
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepository
import com.wangxingxing.wxxstoremanage.core.model.CredentialPreference
import com.wangxingxing.wxxstoremanage.core.model.CurrentUser
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSessionCleanerTest {

    @Test
    fun `successful logout clears business data`() = runTest {
        val authRepository = FakeAuthRepository()
        val businessCleaner = FakeBusinessDataCleaner()
        val sessionCleaner = DefaultSessionCleaner(authRepository, businessCleaner)

        val result = sessionCleaner.endSession()

        assertTrue(result is AppResult.Success)
        assertEquals(1, authRepository.logoutCalls.get())
        assertEquals(1, businessCleaner.clearCalls.get())
    }

    @Test
    fun `failed logout keeps business data and preserves error`() = runTest {
        val failure = AppError(AppErrorType.NetworkUnavailable)
        val authRepository = FakeAuthRepository(
            logoutResult = AppResult.Failure(failure),
        )
        val businessCleaner = FakeBusinessDataCleaner()
        val sessionCleaner = DefaultSessionCleaner(authRepository, businessCleaner)

        val result = sessionCleaner.endSession()

        assertTrue(result is AppResult.Failure)
        assertSame(failure, (result as AppResult.Failure).error)
        assertEquals(0, businessCleaner.clearCalls.get())
    }

    @Test
    fun `overlapping end session calls run serially`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val authRepository = FakeAuthRepository(gate = gate)
        val businessCleaner = FakeBusinessDataCleaner()
        val sessionCleaner = DefaultSessionCleaner(authRepository, businessCleaner)

        val first = async { sessionCleaner.endSession() }
        runCurrent()
        assertEquals(1, authRepository.logoutCalls.get())

        val second = async { sessionCleaner.endSession() }
        runCurrent()
        assertEquals(1, authRepository.logoutCalls.get())

        gate.complete(Unit)
        advanceUntilIdle()

        assertTrue(first.await() is AppResult.Success)
        assertTrue(second.await() is AppResult.Success)
        assertEquals(2, authRepository.logoutCalls.get())
        assertEquals(2, businessCleaner.clearCalls.get())
    }
}

private class FakeAuthRepository(
    private val logoutResult: AppResult<Unit> = AppResult.Success(Unit),
    private val gate: CompletableDeferred<Unit>? = null,
) : AuthRepository {

    override val session: Flow<CurrentUser?> = MutableStateFlow(null)

    val logoutCalls = AtomicInteger(0)

    override suspend fun loadCredentialPreference(): CredentialPreference =
        CredentialPreference(credentials = null, rememberEnabled = true)

    override suspend fun login(
        username: String,
        password: String,
        rememberCredentials: Boolean,
    ): AppResult<CurrentUser> = AppResult.Failure(AppError(AppErrorType.Unknown))

    override suspend fun logout(): AppResult<Unit> {
        logoutCalls.incrementAndGet()
        gate?.await()
        return logoutResult
    }
}

private class FakeBusinessDataCleaner : BusinessDataCleaner {

    val clearCalls = AtomicInteger(0)

    override suspend fun clear() {
        clearCalls.incrementAndGet()
    }
}
