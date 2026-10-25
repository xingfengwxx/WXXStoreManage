/**
 * author : 王星星
 * date : 2026-10-25 16:32
 * email : 1099420259@qq.com
 * description : 在真实 SQLite 内存库验证仓库与 SKU 的启用过滤、名称排序和 upsert 更新语义。
 */
package com.wangxingxing.wxxstoremanage.core.database.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.wangxingxing.wxxstoremanage.core.database.AppDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var warehouseDao: WarehouseDao
    private lateinit var skuDao: SkuDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
        ).build()
        warehouseDao = database.warehouseDao()
        skuDao = database.skuDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun warehouseStreamFiltersInactiveRowsAndOrdersByName() = runTest {
        warehouseDao.upsertAll(
            listOf(
                warehouse("wh-1", "B仓"),
                warehouse("wh-2", "A仓"),
                warehouse("wh-3", "停用仓", active = false),
            ),
        )

        warehouseDao.observeActiveWarehouses().test {
            assertEquals(listOf("wh-2", "wh-1"), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun skuStreamFiltersInactiveRowsAndOrdersByName() = runTest {
        skuDao.upsertAll(
            listOf(
                sku("sku-1", "B商品"),
                sku("sku-2", "A商品"),
                sku("sku-3", "停用商品", active = false),
            ),
        )

        skuDao.observeActiveSkus().test {
            assertEquals(listOf("sku-2", "sku-1"), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun warehouseUpsertUpdatesTheExistingRow() = runTest {
        warehouseDao.upsertAll(listOf(warehouse("wh-1", "旧名称")))
        warehouseDao.upsertAll(listOf(warehouse("wh-1", "新名称")))

        warehouseDao.observeActiveWarehouses().test {
            val rows = awaitItem()
            assertEquals(1, rows.size)
            assertEquals("新名称", rows.single().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun skuUpsertUpdatesTheExistingRow() = runTest {
        skuDao.upsertAll(listOf(sku("sku-1", "旧名称")))
        skuDao.upsertAll(listOf(sku("sku-1", "新名称")))

        skuDao.observeActiveSkus().test {
            val rows = awaitItem()
            assertEquals(1, rows.size)
            assertEquals("新名称", rows.single().name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
