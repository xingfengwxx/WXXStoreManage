/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : Room 数据库定义，包含仓库、SKU 与库存三张表；结构变更必须提供 migration，禁止破坏性重建。
 */
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
