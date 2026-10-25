package com.wangxingxing.wxxstoremanage.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wangxingxing.wxxstoremanage.core.database.dao.SkuDao
import com.wangxingxing.wxxstoremanage.core.database.dao.StockDao
import com.wangxingxing.wxxstoremanage.core.database.dao.WarehouseDao
import com.wangxingxing.wxxstoremanage.core.database.entity.SkuEntity
import com.wangxingxing.wxxstoremanage.core.database.entity.StockEntity
import com.wangxingxing.wxxstoremanage.core.database.entity.WarehouseEntity

@Database(
    entities = [
        WarehouseEntity::class,
        SkuEntity::class,
        StockEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun warehouseDao(): WarehouseDao
    abstract fun skuDao(): SkuDao
    abstract fun stockDao(): StockDao
}
