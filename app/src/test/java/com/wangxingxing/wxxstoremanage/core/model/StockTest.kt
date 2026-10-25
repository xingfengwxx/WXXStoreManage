/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 覆盖库存现存量、锁定量与可用量的计算语义。
 */
package com.wangxingxing.wxxstoremanage.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StockTest {

    @Test
    fun `available is on hand minus locked`() {
        val stock = Stock(skuId = "sku-1", warehouseId = "wh-1", onHand = 10, locked = 4)

        assertEquals(6, stock.available)
    }

    @Test
    fun `available is zero when everything is locked`() {
        val stock = Stock(skuId = "sku-1", warehouseId = "wh-1", onHand = 4, locked = 4)

        assertEquals(0, stock.available)
    }

    @Test
    fun `summary available is on hand minus locked`() {
        val summary = StockSummary(skuCount = 3, onHand = 12, locked = 5)

        assertEquals(7, summary.available)
    }
}
