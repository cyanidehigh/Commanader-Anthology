package com.commanderanalyst.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY createdAtMillis ASC")
    fun observeDecks(): Flow<List<DeckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDeck(deck: DeckEntity)

    @Query("UPDATE decks SET name = :name, commanderName = :commanderName WHERE id = :deckId")
    suspend fun updateDeck(deckId: String, name: String, commanderName: String?)

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeck(deckId: String)
}

