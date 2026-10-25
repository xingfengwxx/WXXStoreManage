package com.wangxingxing.wxxstoremanage.feature.home.mapper

import com.wangxingxing.wxxstoremanage.core.model.Warehouse
import com.wangxingxing.wxxstoremanage.feature.home.model.HomeUiModel
import com.wangxingxing.wxxstoremanage.feature.home.model.WarehouseUiModel

internal fun List<Warehouse>.mapToHomeUiModel(): HomeUiModel = HomeUiModel(
    warehouses = map { warehouse ->
        WarehouseUiModel(
            id = warehouse.id,
            name = warehouse.name,
            code = warehouse.code,
        )
    },
)
