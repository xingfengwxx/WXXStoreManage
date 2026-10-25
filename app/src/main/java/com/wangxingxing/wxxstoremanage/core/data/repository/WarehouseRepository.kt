/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 仓库查询的只读仓库接口，向页面暴露启用仓库 Flow。
 */
package com.wangxingxing.wxxstoremanage.core.data.repository

import com.wangxingxing.wxxstoremanage.core.model.Warehouse
import kotlinx.coroutines.flow.Flow

interface WarehouseRepository {
    fun observeActiveWarehouses(): Flow<List<Warehouse>>
}
