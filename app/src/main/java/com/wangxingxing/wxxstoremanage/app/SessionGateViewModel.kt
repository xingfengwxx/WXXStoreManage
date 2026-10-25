/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 把会话流归约为门状态；本地会话读取异常按未登录处理，避免应用卡在启动占位。
 */
package com.wangxingxing.wxxstoremanage.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SessionGateViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val logger: AppLogger,
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = authRepository.session
        .map { currentUser ->
            if (currentUser == null) SessionState.LoggedOut else SessionState.LoggedIn
        }
        .catch { throwable ->
            logger.log(
                AppLogEvent(
                    feature = FEATURE,
                    action = ACTION_OBSERVE_SESSION,
                    phase = AppLogPhase.Failure,
                    throwable = throwable,
                ),
            )
            emit(SessionState.LoggedOut)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SessionState.Loading,
        )

    private companion object {
        const val FEATURE = "auth"
        const val ACTION_OBSERVE_SESSION = "observeSession"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
