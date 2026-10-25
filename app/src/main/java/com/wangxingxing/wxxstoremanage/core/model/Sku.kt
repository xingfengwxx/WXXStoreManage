package com.wangxingxing.wxxstoremanage.core.model

data class Sku(
    val id: String,
    val code: String,
    val name: String,
    val unit: String,
    val imageUrl: String?,
    val isActive: Boolean,
)
