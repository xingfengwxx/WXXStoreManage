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
