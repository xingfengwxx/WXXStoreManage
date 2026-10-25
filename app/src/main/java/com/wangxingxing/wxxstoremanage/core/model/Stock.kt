package com.wangxingxing.wxxstoremanage.core.model

data class Stock(
    val skuId: String,
    val warehouseId: String,
    val onHand: Int,
    val locked: Int,
) {
    val available: Int get() = onHand - locked
}
