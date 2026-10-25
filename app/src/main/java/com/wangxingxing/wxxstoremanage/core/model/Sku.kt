/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : SKU 领域模型，表示库存管理的最小单元。
 */
package com.wangxingxing.wxxstoremanage.core.model

data class Sku(
    val id: String,
    val code: String,
    val name: String,
    val unit: String,
    val imageUrl: String?,
    val isActive: Boolean,
)
