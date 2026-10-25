/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 库存领域模型；可用量由现存量减锁定量计算，不单独持久化。
 */
package com.wangxingxing.wxxstoremanage.core.model

data class Stock(
    val skuId: String,
    val warehouseId: String,
    val onHand: Int,
    val locked: Int,
) {
    val available: Int get() = onHand - locked
}
