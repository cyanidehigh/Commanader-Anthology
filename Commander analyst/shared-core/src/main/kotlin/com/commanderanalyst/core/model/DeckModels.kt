package com.commanderanalyst.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Deck(
    val id: String,
    val name: String,
    val commanderName: String?,
    val containerId: String?
)

@Serializable
data class DeckSlot(
    val id: String,
    val deckId: String,
    val cardName: String,
    val desiredQuantity: Int,
    val section: DeckSection,
    val identityStatus: CardIdentityStatus = CardIdentityStatus.Unresolved,
    val oracleId: String? = null,
    val oracleName: String? = null,
    val preferredScryfallCardId: String? = null,
    val preferredPrintingName: String? = null,
    val preferredSetCode: String? = null,
    val preferredCollectorNumber: String? = null
)

@Serializable
data class DeckAssignment(
    val id: String,
    val deckSlotId: String,
    val inventoryEntryId: String,
    val assignedQuantity: Int,
    val fromContainerId: String?,
    val toDeckId: String
)

@Serializable
enum class DeckSection {
    Commander,
    Creature,
    Artifact,
    Enchantment,
    Instant,
    Sorcery,
    Planeswalker,
    Battle,
    Land,
    Other
}
