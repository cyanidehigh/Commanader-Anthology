package com.commanderanalyst.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
    @Query("SELECT * FROM containers ORDER BY createdAtMillis ASC")
    fun observeContainers(): Flow<List<ContainerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContainer(container: ContainerEntity)

    @Query("UPDATE containers SET name = :name, type = :type WHERE id = :containerId")
    suspend fun updateContainer(containerId: String, name: String, type: String)

    @Query("DELETE FROM containers WHERE id = :containerId")
    suspend fun deleteContainer(containerId: String)
}
