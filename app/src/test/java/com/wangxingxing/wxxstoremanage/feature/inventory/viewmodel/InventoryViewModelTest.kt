package com.wangxingxing.wxxstoremanage.feature.inventory.viewmodel

import app.cash.turbine.test
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.data.repository.StockRepository
import com.wangxingxing.wxxstoremanage.core.model.StockSummary
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits Empty when there are no SKUs`() = runTest(dispatcher) {
        val repository = FakeStockRepository(MutableStateFlow(StockSummary(0, 0, 0)))
        val viewModel = InventoryViewModel(repository, RecordingLogger())

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)
            advanceUntilIdle()
            assertEquals(ContentState.Empty, awaitItem().content)
        }
    }

    @Test
    fun `emits Content with mapped summary`() = runTest(dispatcher) {
        val repository = FakeStockRepository(MutableStateFlow(StockSummary(3, 12, 5)))
        val viewModel = InventoryViewModel(repository, RecordingLogger())

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)

            advanceUntilIdle()
            val content = awaitItem().content.asContent()
            assertEquals(3, content.value.skuCount)
            assertEquals(12, content.value.onHand)
            assertEquals(5, content.value.locked)
            assertEquals(7, content.value.available)
        }
    }

    @Test
    fun `emits Error when the repository fails`() = runTest(dispatcher) {
        val viewModel = InventoryViewModel(FailingStockRepository(), RecordingLogger())

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)

            advanceUntilIdle()
            assertEquals(
                ContentState.Error(AppErrorType.NetworkUnavailable),
                awaitItem().content,
            )
        }
    }

    private fun <T> ContentState<T>.asContent(): ContentState.Content<T> {
        check(this is ContentState.Content) { "Expected Content but was $this" }
        return this
    }
}

private class FakeStockRepository(
    private val summary: MutableStateFlow<StockSummary>,
) : StockRepository {

    override fun observeSummary(): Flow<StockSummary> = summary
}

private class FailingStockRepository : StockRepository {

    override fun observeSummary(): Flow<StockSummary> = flow {
        throw IOException("offline")
    }
}

private class RecordingLogger : AppLogger {
    val events = mutableListOf<AppLogEvent>()

    override fun log(event: AppLogEvent) {
        events += event
    }
}
