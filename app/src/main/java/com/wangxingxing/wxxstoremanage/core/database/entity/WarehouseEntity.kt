package com.wangxingxing.wxxstoremanage.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val isActive: Boolean,
    val updatedAt: Long,
)
