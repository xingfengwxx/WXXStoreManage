package com.wangxingxing.wxxstoremanage.core.model

data class StockSummary(
    val skuCount: Int,
    val onHand: Int,
    val locked: Int,
) {
    val available: Int get() = onHand - locked
}
