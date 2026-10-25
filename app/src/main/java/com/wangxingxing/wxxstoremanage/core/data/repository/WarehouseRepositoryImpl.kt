/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 从 WarehouseDao 读取启用仓库并映射为领域模型，读取在 IO 调度器执行。
 */
package com.wangxingxing.wxxstoremanage.core.data.repository

import com.wangxingxing.wxxstoremanage.core.common.di.IoDispatcher
import com.wangxingxing.wxxstoremanage.core.data.mapper.toDomain
import com.wangxingxing.wxxstoremanage.core.database.dao.WarehouseDao
import com.wangxingxing.wxxstoremanage.core.model.Warehouse
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class WarehouseRepositoryImpl @Inject constructor(
    private val warehouseDao: WarehouseDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : WarehouseRepository {

    override fun observeActiveWarehouses(): Flow<List<Warehouse>> =
        warehouseDao.observeActiveWarehouses()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)
}
