/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 库存表结构；以 skuId + warehouseId 为复合主键，外键级联删除并按仓库建索引。
 */
package com.wangxingxing.wxxstoremanage.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "stocks",
    primaryKeys = ["skuId", "warehouseId"],
    foreignKeys = [
        ForeignKey(
            entity = SkuEntity::class,
            parentColumns = ["id"],
            childColumns = ["skuId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WarehouseEntity::class,
            parentColumns = ["id"],
            childColumns = ["warehouseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("warehouseId")],
)
data class StockEntity(
    val skuId: String,
    val warehouseId: String,
    val onHand: Int,
    val locked: Int,
    val updatedAt: Long,
)
