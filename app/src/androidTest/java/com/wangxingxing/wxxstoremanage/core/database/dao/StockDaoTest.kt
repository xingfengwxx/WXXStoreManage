/**
 * author : 王星星
 * date : 2026-10-25 16:32
 * email : 1099420259@qq.com
 * description : 在真实 SQLite 内存库验证库存汇总口径、仓库过滤排序、复合主键 upsert 与外键级联。
 */
package com.wangxingxing.wxxstoremanage.core.database.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.wangxingxing.wxxstoremanage.core.database.AppDatabase
import com.wangxingxing.wxxstoremanage.core.database.entity.SkuEntity
import com.wangxingxing.wxxstoremanage.core.database.entity.StockEntity
import com.wangxingxing.wxxstoremanage.core.database.entity.WarehouseEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StockDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var stockDao: StockDao
    private lateinit var skuDao: SkuDao
    private lateinit var warehouseDao: WarehouseDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
        ).build()
        stockDao = database.stockDao()
        skuDao = database.skuDao()
        warehouseDao = database.warehouseDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun summaryIsZeroWhenStockTableIsEmpty() = runTest {
        stockDao.observeSummary().test {
            assertEquals(StockSummaryRow(skuCount = 0, onHand = 0, locked = 0), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun summaryAggregatesRowsAcrossWarehouses() = runTest {
        seedCatalog()
        stockDao.upsertAll(
            listOf(
                stock("sku-1", "wh-1", onHand = 10, locked = 2),
                stock("sku-2", "wh-1", onHand = 5, locked = 1),
                stock("sku-1", "wh-2", onHand = 7, locked = 3),
            ),
        )

        stockDao.observeSummary().test {
            assertEquals(StockSummaryRow(skuCount = 3, onHand = 22, locked = 6), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun summaryEmitsNewValueAfterUpsert() = runTest {
        seedCatalog()
        stockDao.upsertAll(listOf(stock("sku-1", "wh-1", onHand = 10, locked = 2)))

        stockDao.observeSummary().test {
            assertEquals(StockSummaryRow(skuCount = 1, onHand = 10, locked = 2), awaitItem())

            stockDao.upsertAll(listOf(stock("sku-1", "wh-1", onHand = 4, locked = 0)))
            assertEquals(StockSummaryRow(skuCount = 1, onHand = 4, locked = 0), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeByWarehouseFiltersWarehouseAndOrdersBySkuId() = runTest {
        seedCatalog()
        stockDao.upsertAll(
            listOf(
                stock("sku-2", "wh-1", onHand = 1, locked = 0),
                stock("sku-1", "wh-1", onHand = 2, locked = 0),
                stock("sku-1", "wh-2", onHand = 3, locked = 0),
            ),
        )

        stockDao.observeByWarehouse("wh-1").test {
            assertEquals(listOf("sku-1", "sku-2"), awaitItem().map { it.skuId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upsertWithSameSkuAndWarehouseReplacesQuantities() = runTest {
        seedCatalog()
        stockDao.upsertAll(listOf(stock("sku-1", "wh-1", onHand = 10, locked = 2)))

        stockDao.upsertAll(listOf(stock("sku-1", "wh-1", onHand = 6, locked = 1)))

        stockDao.observeSummary().test {
            assertEquals(StockSummaryRow(skuCount = 1, onHand = 6, locked = 1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deletingWarehouseCascadesItsStockRows() = runTest {
        seedCatalog()
        stockDao.upsertAll(
            listOf(
                stock("sku-1", "wh-1", onHand = 1, locked = 0),
                stock("sku-1", "wh-2", onHand = 2, locked = 0),
            ),
        )

        database.openHelper.writableDatabase.execSQL("DELETE FROM warehouses WHERE id = 'wh-1'")

        stockDao.observeSummary().test {
            assertEquals(StockSummaryRow(skuCount = 1, onHand = 2, locked = 0), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun seedCatalog() {
        warehouseDao.upsertAll(
            listOf(
                warehouse("wh-1", "中心仓"),
                warehouse("wh-2", "分仓"),
            ),
        )
        skuDao.upsertAll(
            listOf(
                sku("sku-1", "A-001"),
                sku("sku-2", "B-001"),
            ),
        )
    }
}

internal fun warehouse(id: String, name: String, active: Boolean = true): WarehouseEntity =
    WarehouseEntity(
        id = id,
        code = "CODE-$id",
        name = name,
        isActive = active,
        updatedAt = UPDATED_AT,
    )

internal fun sku(id: String, name: String, active: Boolean = true): SkuEntity =
    SkuEntity(
        id = id,
        code = "CODE-$id",
        name = name,
        unit = "件",
        imageUrl = null,
        isActive = active,
        updatedAt = UPDATED_AT,
    )

private fun stock(
    skuId: String,
    warehouseId: String,
    onHand: Int,
    locked: Int,
): StockEntity = StockEntity(
    skuId = skuId,
    warehouseId = warehouseId,
    onHand = onHand,
    locked = locked,
    updatedAt = UPDATED_AT,
)

internal const val UPDATED_AT = 1_700_000_000_000L
