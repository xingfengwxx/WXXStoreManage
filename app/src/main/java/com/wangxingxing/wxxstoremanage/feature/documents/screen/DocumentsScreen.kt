/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 单据入口纯 UI 页面，当前展示空状态；可独立 Preview，不读取 ViewModel。
 */
package com.wangxingxing.wxxstoremanage.feature.documents.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wangxingxing.wxxstoremanage.R
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import com.wangxingxing.wxxstoremanage.core.ui.EmptyState

@Composable
internal fun DocumentsScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.documents_title),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            style = MaterialTheme.typography.headlineMedium,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            EmptyState(
                title = stringResource(R.string.documents_empty_title),
                description = stringResource(R.string.documents_empty_description),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DocumentsScreenPreview() {
    WXXStoreManageTheme {
        DocumentsScreen()
    }
}
