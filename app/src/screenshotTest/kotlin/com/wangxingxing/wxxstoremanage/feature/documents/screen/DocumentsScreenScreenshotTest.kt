/**
 * author : 王星星
 * date : 2026-10-25 16:56
 * email : 1099420259@qq.com
 * description : 单据入口页截图测试；九种窗口尺寸覆盖空态，并补 1.5 倍字体缩放。
 */
package com.wangxingxing.wxxstoremanage.feature.documents.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import com.wangxingxing.wxxstoremanage.screenshot.ScreenSizePreviews

@PreviewTest
@ScreenSizePreviews
@Composable
fun DocumentsScreenEmpty() {
    WXXStoreManageTheme {
        DocumentsScreen()
    }
}

@PreviewTest
@Preview(name = "400x500 large font", widthDp = 400, heightDp = 500, fontScale = 1.5f, showBackground = true)
@Composable
fun DocumentsScreenLargeFont() {
    WXXStoreManageTheme {
        DocumentsScreen()
    }
}
