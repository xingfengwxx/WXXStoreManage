/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 我的页纯 UI；渲染当前用户、头像与退出确认，不读取 ViewModel，可独立 Preview。
 */
package com.wangxingxing.wxxstoremanage.feature.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.wangxingxing.wxxstoremanage.R
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.core.ui.EmptyState
import com.wangxingxing.wxxstoremanage.core.ui.ErrorState
import com.wangxingxing.wxxstoremanage.core.ui.LoadingIndicator
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState
import com.wangxingxing.wxxstoremanage.core.ui.errorMessageRes
import com.wangxingxing.wxxstoremanage.feature.profile.model.ProfileAction
import com.wangxingxing.wxxstoremanage.feature.profile.model.ProfileUiModel
import com.wangxingxing.wxxstoremanage.feature.profile.model.ProfileUiState

@Composable
internal fun ProfileScreen(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.profile_title),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            style = MaterialTheme.typography.headlineMedium,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (val content = state.content) {
                ContentState.Loading -> LoadingIndicator()
                ContentState.Empty -> EmptyState(
                    title = stringResource(R.string.profile_empty_title),
                    description = stringResource(R.string.profile_empty_description),
                )
                is ContentState.Error -> ErrorState(
                    type = content.type,
                    onRetry = { onAction(ProfileAction.Retry) },
                )
                is ContentState.Content -> ProfileContent(
                    model = content.value,
                    logoutState = state.logoutState,
                    onLogoutClick = { onAction(ProfileAction.RequestLogout) },
                )
            }
        }
    }

    if (state.showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { onAction(ProfileAction.DismissLogout) },
            title = { Text(stringResource(R.string.profile_logout_title)) },
            text = { Text(stringResource(R.string.profile_logout_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(ProfileAction.ConfirmLogout) }) {
                    Text(stringResource(R.string.profile_logout_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ProfileAction.DismissLogout) }) {
                    Text(stringResource(R.string.profile_logout_cancel))
                }
            },
        )
    }
}

@Composable
private fun ProfileContent(
    model: ProfileUiModel,
    logoutState: SubmitState,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val logoutError = logoutState as? SubmitState.Error
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(avatarUrl = model.avatarUrl)
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = model.displayName,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.profile_account, model.username),
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        model.email?.let { email ->
            Text(
                text = stringResource(R.string.profile_email, email),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        model.userId?.let { userId ->
            Text(
                text = stringResource(R.string.profile_user_id, userId),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        if (logoutError != null) {
            Text(
                text = stringResource(logoutError.type.errorMessageRes()),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            enabled = logoutState != SubmitState.Submitting,
        ) {
            if (logoutState == SubmitState.Submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_logging_out))
            } else {
                Text(stringResource(R.string.profile_logout))
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    val fallback: @Composable () -> Unit = {
        Box(
            modifier = modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (avatarUrl == null) {
        fallback()
    } else {
        SubcomposeAsyncImage(
            model = avatarUrl,
            contentDescription = stringResource(R.string.profile_avatar_description),
            modifier = modifier
                .size(72.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = { fallback() },
            error = { fallback() },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    WXXStoreManageTheme {
        ProfileScreen(
            state = ProfileUiState(
                content = ContentState.Content(
                    ProfileUiModel(
                        displayName = "示例用户",
                        username = "demo",
                        email = "demo@example.com",
                        avatarUrl = null,
                        userId = 10086L,
                    ),
                ),
            ),
            onAction = {},
        )
    }
}
