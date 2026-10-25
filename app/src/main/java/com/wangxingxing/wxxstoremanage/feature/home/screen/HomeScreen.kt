package com.wangxingxing.wxxstoremanage.feature.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wangxingxing.wxxstoremanage.R
import com.wangxingxing.wxxstoremanage.core.designsystem.theme.WXXStoreManageTheme
import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.core.ui.EmptyState
import com.wangxingxing.wxxstoremanage.core.ui.ErrorState
import com.wangxingxing.wxxstoremanage.core.ui.LoadingIndicator
import com.wangxingxing.wxxstoremanage.feature.home.model.HomeAction
import com.wangxingxing.wxxstoremanage.feature.home.model.HomeUiModel
import com.wangxingxing.wxxstoremanage.feature.home.model.HomeUiState
import com.wangxingxing.wxxstoremanage.feature.home.model.WarehouseUiModel

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.home_title),
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
                    title = stringResource(R.string.home_empty_title),
                    description = stringResource(R.string.home_empty_description),
                )
                is ContentState.Error -> ErrorState(
                    type = content.type,
                    onRetry = { onAction(HomeAction.Retry) },
                )
                is ContentState.Content -> WarehouseList(content.value)
            }
        }
    }
}

@Composable
private fun WarehouseList(
    model: HomeUiModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = model.warehouses, key = { it.id }) { warehouse ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = warehouse.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.warehouse_code, warehouse.code),
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    WXXStoreManageTheme {
        HomeScreen(
            state = HomeUiState(ContentState.Empty),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenContentPreview() {
    WXXStoreManageTheme {
        HomeScreen(
            state = HomeUiState(
                ContentState.Content(
                    HomeUiModel(
                        warehouses = listOf(
                            WarehouseUiModel(id = "1", name = "示例中心仓", code = "WH-001"),
                            WarehouseUiModel(id = "2", name = "示例分仓", code = "WH-002"),
                        ),
                    ),
                ),
            ),
            onAction = {},
        )
    }
}
