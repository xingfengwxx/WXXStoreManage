/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 首页状态与 UiModel 定义，主内容使用 ContentState。
 */
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
