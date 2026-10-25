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
