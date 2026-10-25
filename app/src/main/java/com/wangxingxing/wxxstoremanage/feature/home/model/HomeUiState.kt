package com.wangxingxing.wxxstoremanage.feature.home.model

import com.wangxingxing.wxxstoremanage.core.ui.ContentState

data class HomeUiState(
    val content: ContentState<HomeUiModel> = ContentState.Loading,
)

data class HomeUiModel(
    val warehouses: List<WarehouseUiModel>,
)

data class WarehouseUiModel(
    val id: String,
    val name: String,
    val code: String,
)
