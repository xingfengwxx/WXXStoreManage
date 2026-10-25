/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 将 Room Entity 与聚合行映射为领域模型；转换细节仅保留在数据层。
 */
package com.wangxingxing.wxxstoremanage.core.data.mapper

import com.wangxingxing.wxxstoremanage.core.database.dao.StockSummaryRow
import com.wangxingxing.wxxstoremanage.core.database.entity.SkuEntity
import com.wangxingxing.wxxstoremanage.core.database.entity.StockEntity
import com.wangxingxing.wxxstoremanage.core.database.entity.WarehouseEntity
import com.wangxingxing.wxxstoremanage.core.model.Sku
import com.wangxingxing.wxxstoremanage.core.model.Stock
import com.wangxingxing.wxxstoremanage.core.model.StockSummary
import com.wangxingxing.wxxstoremanage.core.model.Warehouse

internal fun WarehouseEntity.toDomain(): Warehouse = Warehouse(
    id = id,
    code = code,
    name = name,
    isActive = isActive,
)

internal fun SkuEntity.toDomain(): Sku = Sku(
    id = id,
    code = code,
    name = name,
    unit = unit,
    imageUrl = imageUrl,
    isActive = isActive,
)

internal fun StockEntity.toDomain(): Stock = Stock(
    skuId = skuId,
    warehouseId = warehouseId,
    onHand = onHand,
    locked = locked,
)

internal fun StockSummaryRow.toDomain(): StockSummary = StockSummary(
    skuCount = skuCount,
    onHand = onHand,
    locked = locked,
)
