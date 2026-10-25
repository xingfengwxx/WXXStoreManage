/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 单 Activity 入口，启用 edge-to-edge 并挂载 Compose 主题与应用壳。
 */
package com.wangxingxing.wxxstoremanage.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WXXStoreManageTheme {
                AppShell(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
