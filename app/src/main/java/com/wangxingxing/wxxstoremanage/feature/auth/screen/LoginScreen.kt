/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 登录页纯 UI；只渲染状态并转发动作，处理 IME inset、密码可见性与提交中禁用。
 */
package com.wangxingxing.wxxstoremanage.feature.auth.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wangxingxing.wxxstoremanage.R
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState
import com.wangxingxing.wxxstoremanage.core.ui.errorMessageRes
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginAction
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginField
import com.wangxingxing.wxxstoremanage.feature.auth.model.LoginUiState

@Composable
internal fun LoginScreen(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSubmitting = state.submitState == SubmitState.Submitting
    val submitError = state.submitState as? SubmitState.Error

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .widthIn(max = 480.dp),
            ) {
                Text(
                    text = stringResource(R.string.login_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.login_subtitle),
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val usernameHasError = state.fieldErrors.containsKey(LoginField.Username)
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { value -> onAction(LoginAction.UsernameChanged(value)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    enabled = !isSubmitting,
                    label = { Text(stringResource(R.string.login_username_label)) },
                    singleLine = true,
                    isError = usernameHasError,
                    supportingText = if (usernameHasError) {
                        { Text(stringResource(R.string.login_username_required)) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )

                val passwordHasError = state.fieldErrors.containsKey(LoginField.Password)
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { value -> onAction(LoginAction.PasswordChanged(value)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = !isSubmitting,
                    label = { Text(stringResource(R.string.login_password_label)) },
                    singleLine = true,
                    isError = passwordHasError,
                    supportingText = if (passwordHasError) {
                        { Text(stringResource(R.string.login_password_required)) }
                    } else {
                        null
                    },
                    visualTransformation = if (state.passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = { onAction(LoginAction.PasswordVisibilityToggled) },
                            enabled = !isSubmitting,
                        ) {
                            Text(
                                stringResource(
                                    if (state.passwordVisible) {
                                        R.string.login_password_hide
                                    } else {
                                        R.string.login_password_show
                                    },
                                ),
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onAction(LoginAction.Submit) },
                    ),
                )

                if (submitError != null) {
                    Text(
                        text = stringResource(submitError.type.errorMessageRes()),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Button(
                    onClick = { onAction(LoginAction.Submit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .heightIn(min = 48.dp),
                    enabled = !isSubmitting,
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = LocalContentColor.current,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.login_submitting))
                    } else {
                        Text(stringResource(R.string.login_submit))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    WXXStoreManageTheme {
        LoginScreen(
            state = LoginUiState(
                username = "demo",
                password = "123456",
            ),
            onAction = {},
        )
    }
}
