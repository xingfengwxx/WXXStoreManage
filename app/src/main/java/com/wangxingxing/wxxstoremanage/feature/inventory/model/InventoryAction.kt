package com.wangxingxing.wxxstoremanage.feature.inventory.model

sealed interface InventoryAction {
    data object Retry : InventoryAction
}
