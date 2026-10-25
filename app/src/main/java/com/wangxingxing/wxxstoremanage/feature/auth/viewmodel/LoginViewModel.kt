/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 登录状态归约；提交中忽略重复点击，用户名去首尾空白，密码保持原样。
 */
package com.wangxingxing.wxxstoremanage.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepository
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginAction
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginField
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logger: AppLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.UsernameChanged -> updateUsername(action.value)
            is LoginAction.PasswordChanged -> updatePassword(action.value)
            LoginAction.PasswordVisibilityToggled -> togglePasswordVisibility()
            LoginAction.Submit -> submit()
        }
    }

    private fun updateUsername(value: String) {
        _uiState.update { state ->
            state.copy(
                username = value,
                fieldErrors = state.fieldErrors - LoginField.Username,
                submitState = state.submitState.clearedError(),
            )
        }
    }

    private fun updatePassword(value: String) {
        _uiState.update { state ->
            state.copy(
                password = value,
                fieldErrors = state.fieldErrors - LoginField.Password,
                submitState = state.submitState.clearedError(),
            )
        }
    }

    private fun togglePasswordVisibility() {
        _uiState.update { state ->
            state.copy(passwordVisible = !state.passwordVisible)
        }
    }

    private fun submit() {
        val currentState = _uiState.value
        if (currentState.submitState == SubmitState.Submitting) {
            return
        }

        val trimmedUsername = currentState.username.trim()
        val fieldErrors = buildMap {
            if (trimmedUsername.isEmpty()) {
                put(LoginField.Username, AppErrorType.Validation)
            }
            if (currentState.password.isEmpty()) {
                put(LoginField.Password, AppErrorType.Validation)
            }
        }
        if (fieldErrors.isNotEmpty()) {
            _uiState.update { state ->
                state.copy(
                    username = trimmedUsername,
                    fieldErrors = fieldErrors,
                    submitState = SubmitState.Idle,
                )
            }
            return
        }

        _uiState.update { state ->
            state.copy(
                username = trimmedUsername,
                fieldErrors = emptyMap(),
                submitState = SubmitState.Submitting,
            )
        }
        // 提交期间允许用户继续编辑输入框没有意义，Screen 会禁用；这里捕获本次提交的实际值。
        val password = currentState.password

        viewModelScope.launch {
            logger.log(
                AppLogEvent(
                    feature = FEATURE,
                    action = ACTION_LOGIN,
                    phase = AppLogPhase.Start,
                ),
            )
            when (val result = authRepository.login(trimmedUsername, password)) {
                is AppResult.Success -> {
                    // 登录成功不发送导航事件；SessionGate 观察会话变化后切换根级页面。
                    _uiState.update { state -> state.copy(submitState = SubmitState.Success) }
                    logger.log(
                        AppLogEvent(
                            feature = FEATURE,
                            action = ACTION_LOGIN,
                            phase = AppLogPhase.Success,
                        ),
                    )
                }
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(submitState = SubmitState.Error(result.error.type))
                    }
                    logger.log(
                        AppLogEvent(
                            feature = FEATURE,
                            action = ACTION_LOGIN,
                            phase = AppLogPhase.Failure,
                            context = buildMap {
                                put("errorType", result.error.type.name)
                                result.error.serverCode?.let { put("serverCode", it) }
                            },
                        ),
                    )
                }
            }
        }
    }

    private fun SubmitState.clearedError(): SubmitState =
        if (this is SubmitState.Error) SubmitState.Idle else this

    private companion object {
        const val FEATURE = "auth"
        const val ACTION_LOGIN = "login"
    }
}
