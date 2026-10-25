/**
 * author : 王星星
 * date : 2026-10-25 16:52
 * email : 1099420259@qq.com
 * description : 首页截图测试；用九种窗口尺寸覆盖仓库列表，并补空态、错误态与字体缩放。
 */
package com.wangxingxing.wxxstoremanage.feature.home.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.feature.home.model.HomeUiModel
import com.wangxingxing.wxxstoremanage.feature.home.model.HomeUiState
import com.wangxingxing.wxxstoremanage.feature.home.model.WarehouseUiModel
import com.wangxingxing.wxxstoremanage.screenshot.ScreenSizePreviews

private val sampleWarehouses = HomeUiState(
    content = ContentState.Content(
        HomeUiModel(
            warehouses = listOf(
                WarehouseUiModel(id = "1", name = "示例中心仓", code = "WH-001"),
                WarehouseUiModel(id = "2", name = "示例分仓", code = "WH-002"),
            ),
        ),
    ),
)

@PreviewTest
@ScreenSizePreviews
@Composable
fun HomeScreenContent() {
    WXXStoreManageTheme {
        HomeScreen(state = sampleWarehouses, onAction = {})
    }
}

@PreviewTest
@Preview(name = "400x500 large font", widthDp = 400, heightDp = 500, fontScale = 1.5f, showBackground = true)
@Composable
fun HomeScreenLargeFont() {
    WXXStoreManageTheme {
        HomeScreen(state = sampleWarehouses, onAction = {})
    }
}

@PreviewTest
@Preview(name = "400x500 empty", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun HomeScreenEmpty() {
    WXXStoreManageTheme {
        HomeScreen(state = HomeUiState(ContentState.Empty), onAction = {})
    }
}

@PreviewTest
@Preview(name = "400x500 error", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun HomeScreenError() {
    WXXStoreManageTheme {
        HomeScreen(
            state = HomeUiState(ContentState.Error(AppErrorType.ServiceUnavailable)),
            onAction = {},
        )
    }
}
