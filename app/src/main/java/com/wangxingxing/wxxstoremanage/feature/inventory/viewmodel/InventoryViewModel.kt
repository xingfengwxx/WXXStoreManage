/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 观察库存汇总并归约页面状态；错误映射与日志在内部流处理，保证重试可重新订阅。
 */
package com.wangxingxing.wxxstoremanage.feature.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.data.mapper.toAppError
import com.wangxingxing.wxxstoremanage.core.data.repository.StockRepository
import com.wangxingxing.wxxstoremanage.core.model.StockSummary
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.feature.inventory.mapper.mapToInventoryUiModel
import com.wangxingxing.wxxstoremanage.feature.inventory.model.InventoryAction
import com.wangxingxing.wxxstoremanage.feature.inventory.model.InventoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val logger: AppLogger,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<InventoryUiState> = retryTrigger
        .flatMapLatest {
            stockRepository.observeSummary()
                .map { summary -> summary.toUiState() }
                // 必须留在 flatMapLatest 内部：放在链尾时错误会终止整条流，Retry 无法重新订阅。
                .catch { throwable ->
                    logger.log(
                        AppLogEvent(
                            feature = FEATURE,
                            action = ACTION_LOAD_SUMMARY,
                            phase = AppLogPhase.Failure,
                            throwable = throwable,
                        ),
                    )
                    emit(InventoryUiState(ContentState.Error(throwable.toAppError().type)))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = InventoryUiState(),
        )

    fun onAction(action: InventoryAction) {
        when (action) {
            InventoryAction.Retry -> retryTrigger.update { it + 1 }
        }
    }

    private fun StockSummary.toUiState(): InventoryUiState =
        if (skuCount == 0) {
            InventoryUiState(ContentState.Empty)
        } else {
            InventoryUiState(ContentState.Content(mapToInventoryUiModel()))
        }

    private companion object {
        const val FEATURE = "inventory"
        const val ACTION_LOAD_SUMMARY = "loadStockSummary"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
