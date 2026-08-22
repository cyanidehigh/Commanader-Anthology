package com.commanderanalyst.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckSlotDao {
    @Query("SELECT * FROM deck_slots WHERE deckId = :deckId ORDER BY section ASC, cardName COLLATE NOCASE ASC")
    fun observeSlotsForDeck(deckId: String): Flow<List<DeckSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlot(slot: DeckSlotEntity)

    @Query("UPDATE deck_slots SET cardName = :cardName, desiredQuantity = :quantity, section = :section WHERE id = :slotId")
    suspend fun updateSlot(slotId: String, cardName: String, quantity: Int, section: String)

    @Query("DELETE FROM deck_slots WHERE id = :slotId")
    suspend fun deleteSlot(slotId: String)
}

