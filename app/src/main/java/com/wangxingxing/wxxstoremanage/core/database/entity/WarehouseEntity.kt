/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 仓库表结构，与领域模型保持隔离，由 mapper 转换。
 */
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
