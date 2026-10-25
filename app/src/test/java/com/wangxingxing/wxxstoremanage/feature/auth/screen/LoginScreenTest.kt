/**
 * author : 王星星
 * date : 2026-10-25 16:32
 * email : 1099420259@qq.com
 * description : 用 Robolectric 在本地验证登录页的状态渲染、输入转发、提交错误与密码可见性切换。
 */
package com.wangxingxing.wxxstoremanage.feature.auth.screen

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.wangxingxing.wxxstoremanage.R
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginAction
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginField
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `renders login copy fields and submit button`() {
        setContent(LoginUiState())

        composeRule.onNodeWithText(string(R.string.login_subtitle)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.login_username_label)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.login_password_label)).assertIsDisplayed()
        composeRule.onNode(
            hasClickAction() and hasText(string(R.string.login_submit)),
        ).assertIsDisplayed()
    }

    @Test
    fun `blank fields show validation messages`() {
        setContent(
            LoginUiState(
                fieldErrors = mapOf(
                    LoginField.Username to AppErrorType.Validation,
                    LoginField.Password to AppErrorType.Validation,
                ),
            ),
        )

        composeRule.onNodeWithText(string(R.string.login_username_required)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.login_password_required)).assertIsDisplayed()
    }

    @Test
    fun `submit failure shows the mapped error message`() {
        setContent(
            LoginUiState(submitState = SubmitState.Error(AppErrorType.AuthenticationFailed)),
        )

        composeRule.onNodeWithText(string(R.string.error_authentication_failed)).assertIsDisplayed()
    }

    @Test
    fun `submitting disables the submit button and shows progress copy`() {
        setContent(
            LoginUiState(
                username = "demo",
                password = "secret",
                submitState = SubmitState.Submitting,
            ),
        )

        composeRule.onNodeWithText(string(R.string.login_submitting)).assertIsDisplayed()
        composeRule.onNode(
            hasClickAction() and hasText(string(R.string.login_submitting)),
        ).assertIsNotEnabled()
    }

    @Test
    fun `typing dispatches field change actions`() {
        val actions = mutableListOf<LoginAction>()
        var state by mutableStateOf(LoginUiState())
        composeRule.setContent {
            WXXStoreManageTheme {
                LoginScreen(
                    state = state,
                    onAction = { action ->
                        actions += action
                        state = when (action) {
                            is LoginAction.UsernameChanged -> state.copy(username = action.value)
                            is LoginAction.PasswordChanged -> state.copy(password = action.value)
                            else -> state
                        }
                    },
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.login_username_label)).performTextInput("demo")
        composeRule.onNodeWithText(string(R.string.login_password_label)).performTextInput("secret")

        assertTrue(actions.any { it is LoginAction.UsernameChanged && it.value == "demo" })
        assertTrue(actions.any { it is LoginAction.PasswordChanged && it.value == "secret" })
    }

    @Test
    fun `submit click dispatches submit action`() {
        val actions = mutableListOf<LoginAction>()
        setContent(
            state = LoginUiState(username = "demo", password = "secret"),
            onAction = { actions += it },
        )

        composeRule.onNode(
            hasClickAction() and hasText(string(R.string.login_submit)),
        ).performClick()

        assertEquals(listOf<LoginAction>(LoginAction.Submit), actions)
    }

    @Test
    fun `password visibility label follows state`() {
        var state by mutableStateOf(LoginUiState(passwordVisible = false))
        composeRule.setContent {
            WXXStoreManageTheme {
                LoginScreen(
                    state = state,
                    onAction = { action ->
                        if (action == LoginAction.PasswordVisibilityToggled) {
                            state = state.copy(passwordVisible = !state.passwordVisible)
                        }
                    },
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.login_password_show)).performClick()

        composeRule.onNodeWithText(string(R.string.login_password_hide)).assertIsDisplayed()
    }

    private fun setContent(
        state: LoginUiState,
        onAction: (LoginAction) -> Unit = {},
    ) {
        composeRule.setContent {
            WXXStoreManageTheme {
                LoginScreen(state = state, onAction = onAction)
            }
        }
    }

    private fun string(resId: Int): String = context.getString(resId)
}
