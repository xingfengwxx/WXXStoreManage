/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 将库存汇总领域模型映射为库存页 UiModel。
 */
package com.wangxingxing.wxxstoremanage.feature.inventory.mapper

import com.wangxingxing.wxxstoremanage.core.model.StockSummary
import com.wangxingxing.wxxstoremanage.feature.inventory.model.InventoryUiModel

internal fun StockSummary.mapToInventoryUiModel(): InventoryUiModel = InventoryUiModel(
    skuCount = skuCount,
    onHand = onHand,
    locked = locked,
    available = available,
)
