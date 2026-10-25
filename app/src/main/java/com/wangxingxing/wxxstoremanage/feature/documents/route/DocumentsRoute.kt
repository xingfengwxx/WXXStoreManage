package com.wangxingxing.wxxstoremanage.feature.documents.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wangxingxing.wxxstoremanage.core.navigation.AppRoute
import com.wangxingxing.wxxstoremanage.feature.documents.screen.DocumentsScreen
import kotlinx.serialization.Serializable

@Serializable
data object Documents : AppRoute

@Composable
fun DocumentsRoute(modifier: Modifier = Modifier) {
    DocumentsScreen(modifier = modifier)
}
