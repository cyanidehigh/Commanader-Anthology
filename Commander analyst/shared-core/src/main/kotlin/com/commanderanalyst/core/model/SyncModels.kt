package com.commanderanalyst.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncBundle(
    val schemaVersion: Int,
    val exportedAtEpochMillis: Long,
    val containers: List<Container>,
    val inventoryEntries: List<InventoryEntry>,
    val decks: List<Deck>,
    val deckSlots: List<DeckSlot>,
    val deckAssignments: List<DeckAssignment> = emptyList()
)
