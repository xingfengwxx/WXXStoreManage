/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 观察启用仓库并归约首页状态；注意 catch 必须位于 flatMapLatest 内部，否则错误后整条流结束、重试失效。
 */
package com.wangxingxing.wxxstoremanage.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.data.mapper.toAppError
import com.wangxingxing.wxxstoremanage.core.data.repository.WarehouseRepository
import com.wangxingxing.wxxstoremanage.core.model.Warehouse
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.feature.home.mapper.mapToHomeUiModel
import com.wangxingxing.wxxstoremanage.feature.home.model.HomeAction
import com.wangxingxing.wxxstoremanage.feature.home.model.HomeUiState
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
class HomeViewModel @Inject constructor(
    private val warehouseRepository: WarehouseRepository,
    private val logger: AppLogger,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = retryTrigger
        .flatMapLatest {
            warehouseRepository.observeActiveWarehouses()
                .map { warehouses -> warehouses.toUiState() }
                // 必须留在 flatMapLatest 内部：放在链尾时错误会终止整条流，Retry 无法重新订阅。
                .catch { throwable ->
                    logger.log(
                        AppLogEvent(
                            feature = FEATURE,
                            action = ACTION_LOAD_WAREHOUSES,
                            phase = AppLogPhase.Failure,
                            throwable = throwable,
                        ),
                    )
                    emit(HomeUiState(ContentState.Error(throwable.toAppError().type)))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = HomeUiState(),
        )

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Retry -> retryTrigger.update { it + 1 }
        }
    }

    private fun List<Warehouse>.toUiState(): HomeUiState =
        if (isEmpty()) {
            HomeUiState(ContentState.Empty)
        } else {
            HomeUiState(ContentState.Content(mapToHomeUiModel()))
        }

    private companion object {
        const val FEATURE = "home"
        const val ACTION_LOAD_WAREHOUSES = "loadWarehouses"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
