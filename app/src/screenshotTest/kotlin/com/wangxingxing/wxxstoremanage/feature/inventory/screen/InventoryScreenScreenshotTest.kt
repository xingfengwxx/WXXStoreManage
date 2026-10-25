/**
 * author : 王星星
 * date : 2026-10-25 16:52
 * email : 1099420259@qq.com
 * description : 库存页截图测试；用九种窗口尺寸覆盖汇总指标，并补空态、错误态与字体缩放。
 */
package com.wangxingxing.wxxstoremanage.feature.inventory.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.feature.inventory.model.InventoryUiModel
import com.wangxingxing.wxxstoremanage.feature.inventory.model.InventoryUiState
import com.wangxingxing.wxxstoremanage.screenshot.ScreenSizePreviews

private val sampleInventory = InventoryUiState(
    content = ContentState.Content(
        InventoryUiModel(
            skuCount = 12,
            onHand = 340,
            locked = 20,
            available = 320,
        ),
    ),
)

@PreviewTest
@ScreenSizePreviews
@Composable
fun InventoryScreenContent() {
    WXXStoreManageTheme {
        InventoryScreen(state = sampleInventory, onAction = {})
    }
}

@PreviewTest
@Preview(name = "400x500 large font", widthDp = 400, heightDp = 500, fontScale = 1.5f, showBackground = true)
@Composable
fun InventoryScreenLargeFont() {
    WXXStoreManageTheme {
        InventoryScreen(state = sampleInventory, onAction = {})
    }
}

@PreviewTest
@Preview(name = "400x500 empty", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun InventoryScreenEmpty() {
    WXXStoreManageTheme {
        InventoryScreen(state = InventoryUiState(ContentState.Empty), onAction = {})
    }
}

@PreviewTest
@Preview(name = "400x500 error", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun InventoryScreenError() {
    WXXStoreManageTheme {
        InventoryScreen(
            state = InventoryUiState(ContentState.Error(AppErrorType.NetworkUnavailable)),
            onAction = {},
        )
    }
}
