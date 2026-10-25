package com.wangxingxing.wxxstoremanage.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skus")
data class SkuEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val unit: String,
    val imageUrl: String?,
    val isActive: Boolean,
    val updatedAt: Long,
)
