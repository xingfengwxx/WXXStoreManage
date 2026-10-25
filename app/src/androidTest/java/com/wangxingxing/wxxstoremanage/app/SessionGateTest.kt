/**
 * author : 王星星
 * date : 2026-10-25 16:32
 * email : 1099420259@qq.com
 * description : 通过 Hilt 替换数据层后验证 SessionGate 的登录页切换、登录失败提示与会话恢复主壳。
 */
package com.wangxingxing.wxxstoremanage.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.wangxingxing.wxxstoremanage.R
import com.wangxingxing.wxxstoremanage.core.common.AppError
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.data.di.DataModule
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepository
import com.wangxingxing.wxxstoremanage.core.data.repository.StockRepository
import com.wangxingxing.wxxstoremanage.core.data.repository.WarehouseRepository
import com.wangxingxing.wxxstoremanage.core.data.session.SessionCleaner
import com.wangxingxing.wxxstoremanage.core.model.CurrentUser
import com.wangxingxing.wxxstoremanage.core.model.StockSummary
import com.wangxingxing.wxxstoremanage.core.model.Warehouse
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UninstallModules(DataModule::class)
@HiltAndroidTest
class SessionGateTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val fakeAuthRepository = FakeAuthRepository()

    @BindValue
    @JvmField
    val authRepository: AuthRepository = fakeAuthRepository

    @BindValue
    @JvmField
    val warehouseRepository: WarehouseRepository = FakeWarehouseRepository()

    @BindValue
    @JvmField
    val stockRepository: StockRepository = FakeStockRepository()

    @BindValue
    @JvmField
    val sessionCleaner: SessionCleaner = FakeSessionCleaner()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun noSessionRendersTheLoginScreen() {
        composeRule.onNodeWithText(string(R.string.login_subtitle)).assertIsDisplayed()
    }

    @Test
    fun failedLoginStaysOnTheLoginScreenWithMappedError() {
        fakeAuthRepository.loginResult = AppResult.Failure(
            AppError(AppErrorType.AuthenticationFailed),
        )

        composeRule.onNodeWithText(string(R.string.login_username_label)).performTextInput("demo")
        composeRule.onNodeWithText(string(R.string.login_password_label)).performTextInput("wrong")
        composeRule.onNode(
            hasClickAction() and hasText(string(R.string.login_submit)),
        ).performClick()

        composeRule.onNodeWithText(string(R.string.error_authentication_failed)).assertIsDisplayed()
    }

    @Test
    fun sessionChangeReplacesTheLoginScreenWithTheMainShell() {
        composeRule.runOnIdle { fakeAuthRepository.signIn(demoUser) }

        composeRule.onNodeWithText(string(R.string.home_empty_title)).assertIsDisplayed()
    }

    private fun string(resId: Int): String = composeRule.activity.getString(resId)
}

private class FakeAuthRepository : AuthRepository {

    private val sessionState = MutableStateFlow<CurrentUser?>(null)

    override val session: Flow<CurrentUser?> = sessionState

    var loginResult: AppResult<CurrentUser> = AppResult.Failure(AppError(AppErrorType.Unknown))

    override suspend fun login(username: String, password: String): AppResult<CurrentUser> {
        val result = loginResult
        if (result is AppResult.Success) {
            sessionState.value = result.data
        }
        return result
    }

    override suspend fun logout(): AppResult<Unit> = AppResult.Success(Unit)

    fun signIn(user: CurrentUser) {
        sessionState.value = user
    }
}

private class FakeWarehouseRepository : WarehouseRepository {

    private val warehouses = MutableStateFlow<List<Warehouse>>(emptyList())

    override fun observeActiveWarehouses(): Flow<List<Warehouse>> = warehouses
}

private class FakeStockRepository : StockRepository {

    private val summary = MutableStateFlow(StockSummary(skuCount = 0, onHand = 0, locked = 0))

    override fun observeSummary(): Flow<StockSummary> = summary
}

private class FakeSessionCleaner : SessionCleaner {

    override suspend fun endSession(): AppResult<Unit> = AppResult.Success(Unit)
}

private val demoUser = CurrentUser(
    id = 7L,
    username = "demo",
    displayName = "演示用户",
    email = "demo@example.com",
    avatarUrl = null,
)
