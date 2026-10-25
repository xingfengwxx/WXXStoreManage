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
