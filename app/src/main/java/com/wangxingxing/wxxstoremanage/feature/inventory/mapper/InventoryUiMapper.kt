package com.wangxingxing.wxxstoremanage.feature.inventory.mapper

import com.wangxingxing.wxxstoremanage.core.model.StockSummary
import com.wangxingxing.wxxstoremanage.feature.inventory.model.InventoryUiModel

internal fun StockSummary.mapToInventoryUiModel(): InventoryUiModel = InventoryUiModel(
    skuCount = skuCount,
    onHand = onHand,
    locked = locked,
    available = available,
)
