package com.wangxingxing.wxxstoremanage.feature.inventory.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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
import com.wangxingxing.wxxstoremanage.feature.inventory.model.InventoryAction
import com.wangxingxing.wxxstoremanage.feature.inventory.model.InventoryUiModel
import com.wangxingxing.wxxstoremanage.feature.inventory.model.InventoryUiState

@Composable
internal fun InventoryScreen(
    state: InventoryUiState,
    onAction: (InventoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.inventory_title),
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
                    title = stringResource(R.string.inventory_empty_title),
                    description = stringResource(R.string.inventory_empty_description),
                )
                is ContentState.Error -> ErrorState(
                    type = content.type,
                    onRetry = { onAction(InventoryAction.Retry) },
                )
                is ContentState.Content -> StockSummaryContent(content.value)
            }
        }
    }
}

@Composable
private fun StockSummaryContent(
    model: InventoryUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricRow(
                    label = stringResource(R.string.inventory_sku_count),
                    value = model.skuCount.toString(),
                )
                HorizontalDivider()
                MetricRow(
                    label = stringResource(R.string.inventory_on_hand),
                    value = model.onHand.toString(),
                )
                HorizontalDivider()
                MetricRow(
                    label = stringResource(R.string.inventory_locked),
                    value = model.locked.toString(),
                )
                HorizontalDivider()
                MetricRow(
                    label = stringResource(R.string.inventory_available),
                    value = model.available.toString(),
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryScreenEmptyPreview() {
    WXXStoreManageTheme {
        InventoryScreen(
            state = InventoryUiState(ContentState.Empty),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryScreenContentPreview() {
    WXXStoreManageTheme {
        InventoryScreen(
            state = InventoryUiState(
                ContentState.Content(
                    InventoryUiModel(
                        skuCount = 12,
                        onHand = 340,
                        locked = 20,
                        available = 320,
                    ),
                ),
            ),
            onAction = {},
        )
    }
}
