package com.commanderanalyst.data

import com.commanderanalyst.data.local.DeckDao
import com.commanderanalyst.data.local.DeckEntity
import com.commanderanalyst.data.local.DeckSlotDao
import com.commanderanalyst.data.local.DeckSlotEntity
import com.commanderanalyst.data.local.toDomain
import com.commanderanalyst.domain.model.Deck
import com.commanderanalyst.domain.model.DeckSection
import com.commanderanalyst.domain.model.DeckSlot
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeckRepository(
    private val deckDao: DeckDao,
    private val deckSlotDao: DeckSlotDao
) {
    val decks: Flow<List<Deck>> = deckDao.observeDecks()
        .map { entities -> entities.map { it.toDomain() } }

    fun observeSlots(deckId: String): Flow<List<DeckSlot>> {
        return deckSlotDao.observeSlotsForDeck(deckId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun createDeck(name: String, commanderName: String?) {
        deckDao.upsertDeck(
            DeckEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                commanderName = commanderName,
                containerId = null,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateDeck(deckId: String, name: String, commanderName: String?) {
        deckDao.updateDeck(deckId, name, commanderName)
    }

    suspend fun deleteDeck(deckId: String) {
        deckDao.deleteDeck(deckId)
    }

    suspend fun addSlot(deckId: String, cardName: String, quantity: Int, section: DeckSection) {
        deckSlotDao.upsertSlot(
            DeckSlotEntity(
                id = UUID.randomUUID().toString(),
                deckId = deckId,
                cardName = cardName,
                desiredQuantity = quantity,
                section = section.name,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateSlot(slotId: String, cardName: String, quantity: Int, section: DeckSection) {
        deckSlotDao.updateSlot(slotId, cardName, quantity, section.name)
    }

    suspend fun deleteSlot(slotId: String) {
        deckSlotDao.deleteSlot(slotId)
    }
}

