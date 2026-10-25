package com.wangxingxing.wxxstoremanage.core.data.di

import com.wangxingxing.wxxstoremanage.core.data.repository.StockRepository
import com.wangxingxing.wxxstoremanage.core.data.repository.StockRepositoryImpl
import com.wangxingxing.wxxstoremanage.core.data.repository.WarehouseRepository
import com.wangxingxing.wxxstoremanage.core.data.repository.WarehouseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindWarehouseRepository(impl: WarehouseRepositoryImpl): WarehouseRepository

    @Binds
    @Singleton
    abstract fun bindStockRepository(impl: StockRepositoryImpl): StockRepository
}
