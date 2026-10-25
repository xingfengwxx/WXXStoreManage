package com.wangxingxing.wxxstoremanage.feature.inventory.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wangxingxing.wxxstoremanage.core.navigation.AppRoute
import com.wangxingxing.wxxstoremanage.feature.inventory.screen.InventoryScreen
import com.wangxingxing.wxxstoremanage.feature.inventory.viewmodel.InventoryViewModel
import kotlinx.serialization.Serializable

@Serializable
data object Inventory : AppRoute

@Composable
fun InventoryRoute(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InventoryScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
