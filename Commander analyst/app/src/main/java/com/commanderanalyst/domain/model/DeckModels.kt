package com.commanderanalyst.domain.model

data class Deck(
    val id: String,
    val name: String,
    val commanderName: String?,
    val containerId: String?
)

data class DeckSlot(
    val id: String,
    val deckId: String,
    val cardName: String,
    val desiredQuantity: Int,
    val section: DeckSection
)

data class DeckAssignment(
    val id: String,
    val deckSlotId: String,
    val inventoryEntryId: String,
    val assignedQuantity: Int
)

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
