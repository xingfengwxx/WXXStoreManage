/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 提供 Room 数据库及 DAO 单例。
 */
package com.wangxingxing.wxxstoremanage.core.database.di

import android.content.Context
import androidx.room.Room
import com.wangxingxing.wxxstoremanage.core.database.AppDatabase
import com.wangxingxing.wxxstoremanage.core.database.dao.SkuDao
import com.wangxingxing.wxxstoremanage.core.database.dao.StockDao
import com.wangxingxing.wxxstoremanage.core.database.dao.WarehouseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wxx-store-manage.db").build()

    @Provides
    fun provideWarehouseDao(database: AppDatabase): WarehouseDao = database.warehouseDao()

    @Provides
    fun provideSkuDao(database: AppDatabase): SkuDao = database.skuDao()

    @Provides
    fun provideStockDao(database: AppDatabase): StockDao = database.stockDao()
}
