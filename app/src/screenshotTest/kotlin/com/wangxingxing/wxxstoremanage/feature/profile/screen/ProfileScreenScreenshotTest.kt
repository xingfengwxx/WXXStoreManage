/**
 * author : 王星星
 * date : 2026-10-25 16:56
 * email : 1099420259@qq.com
 * description : 我的页截图测试；九种窗口尺寸覆盖用户信息，并补空态、错误态、退出中与字体缩放。
 * 注意：头像固定传 null，避免截图测试访问网络；退出确认弹窗属于独立窗口，不纳入基准图。
 */
package com.wangxingxing.wxxstoremanage.feature.profile.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState
import com.wangxingxing.wxxstoremanage.feature.profile.model.ProfileUiModel
import com.wangxingxing.wxxstoremanage.feature.profile.model.ProfileUiState
import com.wangxingxing.wxxstoremanage.screenshot.ScreenSizePreviews

private val sampleProfile = ProfileUiState(
    content = ContentState.Content(
        ProfileUiModel(
            displayName = "示例用户",
            username = "demo",
            email = "demo@example.com",
            avatarUrl = null,
            userId = 10086L,
        ),
    ),
)

@PreviewTest
@ScreenSizePreviews
@Composable
fun ProfileScreenContent() {
    WXXStoreManageTheme {
        ProfileScreen(state = sampleProfile, onAction = {})
    }
}

@PreviewTest
@Preview(name = "400x500 large font", widthDp = 400, heightDp = 500, fontScale = 1.5f, showBackground = true)
@Composable
fun ProfileScreenLargeFont() {
    WXXStoreManageTheme {
        ProfileScreen(state = sampleProfile, onAction = {})
    }
}

@PreviewTest
@Preview(name = "400x500 empty", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun ProfileScreenEmpty() {
    WXXStoreManageTheme {
        ProfileScreen(state = ProfileUiState(ContentState.Empty), onAction = {})
    }
}

@PreviewTest
@Preview(name = "400x500 error", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun ProfileScreenError() {
    WXXStoreManageTheme {
        ProfileScreen(
            state = ProfileUiState(ContentState.Error(AppErrorType.NetworkUnavailable)),
            onAction = {},
        )
    }
}

@PreviewTest
@Preview(name = "400x500 logging out", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun ProfileScreenLoggingOut() {
    WXXStoreManageTheme {
        ProfileScreen(
            state = sampleProfile.copy(logoutState = SubmitState.Submitting),
            onAction = {},
        )
    }
}
