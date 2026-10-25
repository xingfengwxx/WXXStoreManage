/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 个人页状态归约；观察当前用户，退出登录必须经 SessionCleaner 统一清理。
 */
package com.wangxingxing.wxxstoremanage.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.data.mapper.toAppError
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepository
import com.wangxingxing.wxxstoremanage.core.data.session.SessionCleaner
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState
import com.wangxingxing.wxxstoremanage.feature.profile.mapper.toUiModel
import com.wangxingxing.wxxstoremanage.feature.profile.model.ProfileAction
import com.wangxingxing.wxxstoremanage.feature.profile.model.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionCleaner: SessionCleaner,
    private val logger: AppLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var sessionJob: Job? = null

    init {
        observeSession()
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.Retry -> observeSession()
            ProfileAction.RequestLogout -> requestLogout()
            ProfileAction.DismissLogout -> dismissLogout()
            ProfileAction.ConfirmLogout -> confirmLogout()
        }
    }

    private fun observeSession() {
        sessionJob?.cancel()
        _uiState.update { state -> state.copy(content = ContentState.Loading) }
        sessionJob = viewModelScope.launch {
            authRepository.session
                .catch { throwable ->
                    logger.log(
                        AppLogEvent(
                            feature = FEATURE,
                            action = ACTION_OBSERVE_SESSION,
                            phase = AppLogPhase.Failure,
                            throwable = throwable,
                        ),
                    )
                    _uiState.update { state ->
                        state.copy(content = ContentState.Error(throwable.toAppError().type))
                    }
                }
                .collect { currentUser ->
                    _uiState.update { state ->
                        state.copy(
                            content = if (currentUser == null) {
                                ContentState.Empty
                            } else {
                                ContentState.Content(currentUser.toUiModel())
                            },
                        )
                    }
                }
        }
    }

    private fun requestLogout() {
        if (_uiState.value.logoutState == SubmitState.Submitting) {
            return
        }
        _uiState.update { state -> state.copy(showLogoutConfirmation = true) }
    }

    private fun dismissLogout() {
        _uiState.update { state -> state.copy(showLogoutConfirmation = false) }
    }

    private fun confirmLogout() {
        if (_uiState.value.logoutState == SubmitState.Submitting) {
            return
        }
        // 先同步进入 Submitting，避免连续点击启动两次清理。
        _uiState.update { state ->
            state.copy(
                showLogoutConfirmation = false,
                logoutState = SubmitState.Submitting,
            )
        }
        viewModelScope.launch {
            logger.log(
                AppLogEvent(
                    feature = FEATURE,
                    action = ACTION_LOGOUT,
                    phase = AppLogPhase.Start,
                ),
            )
            when (val result = sessionCleaner.endSession()) {
                is AppResult.Success -> {
                    // 会话清除后 SessionGate 会切回登录页，这里只记录结果。
                    _uiState.update { state -> state.copy(logoutState = SubmitState.Success) }
                    logger.log(
                        AppLogEvent(
                            feature = FEATURE,
                            action = ACTION_LOGOUT,
                            phase = AppLogPhase.Success,
                        ),
                    )
                }
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(logoutState = SubmitState.Error(result.error.type))
                    }
                    logger.log(
                        AppLogEvent(
                            feature = FEATURE,
                            action = ACTION_LOGOUT,
                            phase = AppLogPhase.Failure,
                            context = mapOf("errorType" to result.error.type.name),
                        ),
                    )
                }
            }
        }
    }

    private companion object {
        const val FEATURE = "profile"
        const val ACTION_OBSERVE_SESSION = "observeSession"
        const val ACTION_LOGOUT = "logout"
    }
}
