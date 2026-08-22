package com.commanderanalyst.data

import com.commanderanalyst.data.local.ContainerDao
import com.commanderanalyst.data.local.InventoryDao
import com.commanderanalyst.data.local.InventoryEntryEntity
import com.commanderanalyst.data.local.toDomain
import com.commanderanalyst.data.local.toEntity
import com.commanderanalyst.domain.model.Container
import com.commanderanalyst.domain.model.ContainerType
import com.commanderanalyst.domain.model.ManualInventoryCard
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContainerRepository(
    private val containerDao: ContainerDao,
    private val inventoryDao: InventoryDao
) {
    val containers: Flow<List<Container>> = containerDao.observeContainers()
        .map { entities -> entities.map { it.toDomain() } }

    val cardCountsByContainer: Flow<Map<String, Int>> = inventoryDao.observeAllEntries()
        .map { entries ->
            entries
                .groupBy { it.containerId }
                .mapValues { (_, groupedEntries) -> groupedEntries.sumOf { it.quantity } }
        }

    suspend fun createContainer(name: String, type: ContainerType) {
        val container = Container(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type
        )
        containerDao.upsertContainer(container.toEntity(System.currentTimeMillis()))
    }

    suspend fun updateContainer(containerId: String, name: String, type: ContainerType) {
        containerDao.updateContainer(containerId, name, type.name)
    }

    suspend fun deleteContainer(containerId: String) {
        containerDao.deleteContainer(containerId)
    }

    fun observeCards(containerId: String): Flow<List<ManualInventoryCard>> {
        return inventoryDao.observeEntriesForContainer(containerId)
            .map { entries -> entries.map { it.toDomain() } }
    }

    suspend fun addManualCard(containerId: String, cardName: String, quantity: Int) {
        inventoryDao.upsertEntry(
            InventoryEntryEntity(
                id = UUID.randomUUID().toString(),
                containerId = containerId,
                cardName = cardName,
                quantity = quantity,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateManualCard(cardId: String, cardName: String, quantity: Int) {
        inventoryDao.updateEntry(cardId, cardName, quantity)
    }

    suspend fun deleteManualCard(cardId: String) {
        inventoryDao.deleteEntry(cardId)
    }
}
