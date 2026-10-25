package com.wangxingxing.wxxstoremanage.feature.home.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wangxingxing.wxxstoremanage.core.navigation.AppRoute
import com.wangxingxing.wxxstoremanage.feature.home.screen.HomeScreen
import com.wangxingxing.wxxstoremanage.feature.home.viewmodel.HomeViewModel
import kotlinx.serialization.Serializable

@Serializable
data object Home : AppRoute

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
