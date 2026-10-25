/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 仓库领域模型，是库存归属与查询的第一维度。
 */
package com.wangxingxing.wxxstoremanage.core.model

data class Warehouse(
    val id: String,
    val code: String,
    val name: String,
    val isActive: Boolean,
)
