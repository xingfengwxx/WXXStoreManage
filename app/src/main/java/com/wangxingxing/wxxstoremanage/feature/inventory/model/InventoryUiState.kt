/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 库存页状态与 UiModel 定义，主内容使用 ContentState。
 */
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
