package com.commanderanalyst.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Container(
    val id: String,
    val name: String,
    val type: ContainerType
)

@Serializable
enum class ContainerType {
    Set,
    Binder,
    Box,
    Deck,
    Ordered,
    Proxy,
    Other
}

@Serializable
data class InventoryEntry(
    val id: String,
    val containerId: String,
    val cardName: String,
    val quantity: Int,
    val identityStatus: CardIdentityStatus = CardIdentityStatus.Unresolved,
    val oracleId: String? = null,
    val oracleName: String? = null,
    val scryfallCardId: String? = null,
    val printingName: String? = null,
    val setCode: String? = null,
    val collectorNumber: String? = null,
    val isFoil: Boolean = false
)

@Serializable
enum class CardIdentityStatus {
    Unresolved,
    Resolved,
    Manual,
    Ambiguous
}
