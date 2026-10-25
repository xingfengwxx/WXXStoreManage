/**
 * author : 王星星
 * date : 2026-10-25 17:10
 * email : 1099420259@qq.com
 * description : 登录页截图测试；用九种窗口尺寸覆盖手机、折叠屏与外接显示，并补字体缩放和失败状态。
 */
package com.wangxingxing.wxxstoremanage.feature.auth.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginField
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginUiState
import com.wangxingxing.wxxstoremanage.screenshot.ScreenSizePreviews

@PreviewTest
@ScreenSizePreviews
@Composable
fun LoginScreenEmpty() {
    WXXStoreManageTheme {
        LoginScreen(
            state = LoginUiState(),
            onAction = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "400x500 large font",
    widthDp = 400,
    heightDp = 500,
    fontScale = 1.5f,
    showBackground = true,
)
@Composable
fun LoginScreenLargeFont() {
    WXXStoreManageTheme {
        LoginScreen(
            state = LoginUiState(username = "demo", password = "123456"),
            onAction = {},
        )
    }
}

@PreviewTest
@Preview(name = "400x500 login failed", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun LoginScreenAuthenticationFailed() {
    WXXStoreManageTheme {
        LoginScreen(
            state = LoginUiState(
                username = "demo",
                password = "wrong-password",
                submitState = SubmitState.Error(AppErrorType.AuthenticationFailed),
            ),
            onAction = {},
        )
    }
}

@PreviewTest
@Preview(name = "400x500 field errors", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun LoginScreenFieldErrors() {
    WXXStoreManageTheme {
        LoginScreen(
            state = LoginUiState(
                fieldErrors = mapOf(
                    LoginField.Username to AppErrorType.Validation,
                    LoginField.Password to AppErrorType.Validation,
                ),
            ),
            onAction = {},
        )
    }
}

@PreviewTest
@Preview(name = "400x500 submitting", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun LoginScreenSubmitting() {
    WXXStoreManageTheme {
        LoginScreen(
            state = LoginUiState(
                username = "demo",
                password = "123456",
                submitState = SubmitState.Submitting,
            ),
            onAction = {},
        )
    }
}
