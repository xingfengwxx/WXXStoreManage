package com.wangxingxing.wxxstoremanage.feature.inventory.model

import com.wangxingxing.wxxstoremanage.core.ui.ContentState

data class InventoryUiState(
    val content: ContentState<InventoryUiModel> = ContentState.Loading,
)

data class InventoryUiModel(
    val skuCount: Int,
    val onHand: Int,
    val locked: Int,
    val available: Int,
)
