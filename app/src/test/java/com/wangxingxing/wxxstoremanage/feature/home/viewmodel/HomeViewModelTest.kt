/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 覆盖首页 Loading、Empty、Content、Error、失败日志与重试后的重新订阅。
 */
package com.wangxingxing.wxxstoremanage.feature.home.viewmodel

import app.cash.turbine.test
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.data.repository.WarehouseRepository
import com.wangxingxing.wxxstoremanage.core.model.Warehouse
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.feature.home.model.HomeAction
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
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
class HomeViewModelTest {

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
    fun `emits Loading then Empty when there are no warehouses`() = runTest(dispatcher) {
        val viewModel = HomeViewModel(FakeWarehouseRepository(), RecordingLogger())

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)
            advanceUntilIdle()
            assertEquals(ContentState.Empty, awaitItem().content)
        }
    }

    @Test
    fun `emits Content with mapped warehouses`() = runTest(dispatcher) {
        val repository = FakeWarehouseRepository(
            MutableStateFlow(
                listOf(
                    Warehouse(
                        id = "wh-1",
                        code = "WH-001",
                        name = "中心仓",
                        isActive = true,
                    ),
                ),
            ),
        )
        val viewModel = HomeViewModel(repository, RecordingLogger())

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)

            advanceUntilIdle()
            val content = awaitItem().content.asContent()
            val warehouse = content.value.warehouses.single()
            assertEquals("wh-1", warehouse.id)
            assertEquals("WH-001", warehouse.code)
            assertEquals("中心仓", warehouse.name)
        }
    }

    @Test
    fun `emits Error and logs failure when the repository fails`() = runTest(dispatcher) {
        val logger = RecordingLogger()
        val viewModel = HomeViewModel(FakeWarehouseRepository(failuresRemaining = 1), logger)

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)

            advanceUntilIdle()
            assertEquals(
                ContentState.Error(AppErrorType.NetworkUnavailable),
                awaitItem().content,
            )
            assertEquals(AppLogPhase.Failure, logger.events.single().phase)
        }
    }

    @Test
    fun `Retry resubscribes after a failure`() = runTest(dispatcher) {
        val viewModel = HomeViewModel(
            FakeWarehouseRepository(failuresRemaining = 1),
            RecordingLogger(),
        )

        viewModel.uiState.test {
            assertEquals(ContentState.Loading, awaitItem().content)

            advanceUntilIdle()
            assertEquals(
                ContentState.Error(AppErrorType.NetworkUnavailable),
                awaitItem().content,
            )

            viewModel.onAction(HomeAction.Retry)
            advanceUntilIdle()

            assertEquals(ContentState.Empty, awaitItem().content)
        }
    }

    private fun <T> ContentState<T>.asContent(): ContentState.Content<T> {
        check(this is ContentState.Content) { "Expected Content but was $this" }
        return this
    }
}

private class FakeWarehouseRepository(
    private val warehouses: MutableStateFlow<List<Warehouse>> = MutableStateFlow(emptyList()),
    private var failuresRemaining: Int = 0,
) : WarehouseRepository {

    override fun observeActiveWarehouses(): Flow<List<Warehouse>> = flow {
        if (failuresRemaining > 0) {
            failuresRemaining--
            throw IOException("offline")
        }
        emitAll(warehouses)
    }
}

private class RecordingLogger : AppLogger {
    val events = mutableListOf<AppLogEvent>()

    override fun log(event: AppLogEvent) {
        events += event
    }

    override fun debug(section: String, message: String) = Unit
}
