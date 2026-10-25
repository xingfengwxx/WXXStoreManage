package com.wangxingxing.wxxstoremanage.feature.profile.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wangxingxing.wxxstoremanage.core.navigation.AppRoute
import com.wangxingxing.wxxstoremanage.feature.profile.screen.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
data object Profile : AppRoute

@Composable
fun ProfileRoute(modifier: Modifier = Modifier) {
    ProfileScreen(modifier = modifier)
}
