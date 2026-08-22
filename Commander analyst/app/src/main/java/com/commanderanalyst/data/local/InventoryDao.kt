package com.commanderanalyst.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_entries ORDER BY cardName COLLATE NOCASE ASC")
    fun observeAllEntries(): Flow<List<InventoryEntryEntity>>

    @Query("SELECT * FROM inventory_entries WHERE containerId = :containerId ORDER BY cardName COLLATE NOCASE ASC")
    fun observeEntriesForContainer(containerId: String): Flow<List<InventoryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: InventoryEntryEntity)

    @Query("UPDATE inventory_entries SET cardName = :cardName, quantity = :quantity WHERE id = :entryId")
    suspend fun updateEntry(entryId: String, cardName: String, quantity: Int)

    @Query("DELETE FROM inventory_entries WHERE id = :entryId")
    suspend fun deleteEntry(entryId: String)
}
