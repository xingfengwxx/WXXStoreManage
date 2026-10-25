/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 将仓库领域模型列表映射为首页 UiModel。
 */
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
