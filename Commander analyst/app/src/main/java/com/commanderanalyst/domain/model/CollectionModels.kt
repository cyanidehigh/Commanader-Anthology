package com.commanderanalyst.domain.model

data class Container(
    val id: String,
    val name: String,
    val type: ContainerType
)

enum class ContainerType {
    Set,
    Binder,
    Box,
    Deck,
    Ordered,
    Proxy,
    Other
}

data class InventoryEntry(
    val id: String,
    val cardOracleId: String,
    val printingId: String?,
    val containerId: String,
    val quantity: Int
)
