/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 库存汇总领域模型；可用量同样由现存量减锁定量计算。
 */
package com.wangxingxing.wxxstoremanage.core.model

data class StockSummary(
    val skuCount: Int,
    val onHand: Int,
    val locked: Int,
) {
    val available: Int get() = onHand - locked
}
