package com.commanderanalyst.desktop

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.commanderanalyst.core.model.Container
import com.commanderanalyst.core.model.ContainerType
import com.commanderanalyst.core.model.Deck
import com.commanderanalyst.core.model.DeckAssignment
import com.commanderanalyst.core.model.DeckSection
import com.commanderanalyst.core.model.DeckSlot
import com.commanderanalyst.core.model.InventoryEntry
import com.commanderanalyst.core.model.SyncBundle
import java.util.UUID

class DesktopAppState(
    private val persistence: DesktopPersistence = DesktopPersistence()
) {
    val decks = mutableStateListOf<Deck>()
    val deckSlots = mutableStateListOf<DeckSlot>()
    val deckAssignments = mutableStateListOf<DeckAssignment>()
    val containers = mutableStateListOf<Container>()
    val inventoryEntries = mutableStateListOf<InventoryEntry>()

    var selectedDeckId by mutableStateOf<String?>(null)
    var selectedContainerId by mutableStateOf<String?>(null)

    fun createDeck(name: String, commanderName: String?) {
        val deckContainer = Container(
            id = newId(),
            name = name.trim(),
            type = ContainerType.Deck
        )
        val deck = Deck(
            id = newId(),
            name = name.trim(),
            commanderName = commanderName?.trim()?.takeIf { it.isNotEmpty() },
            containerId = deckContainer.id
        )
        containers += deckContainer
        decks += deck
        selectedDeckId = deck.id
        save()
    }

    fun updateDeck(deckId: String, name: String, commanderName: String?) {
        decks.replaceFirst(deckId) { deck ->
            deck.copy(
                name = name.trim(),
                commanderName = commanderName?.trim()?.takeIf { it.isNotEmpty() }
            )
        }
        decks.firstOrNull { it.id == deckId }?.containerId?.let { containerId ->
            containers.replaceFirst(containerId) { container -> container.copy(name = name.trim()) }
        }
        save()
    }

    fun deleteDeck(deckId: String) {
        val deckContainerId = decks.firstOrNull { it.id == deckId }?.containerId
        decks.removeAll { it.id == deckId }
        val removedSlotIds = deckSlots.filter { it.deckId == deckId }.map { it.id }.toSet()
        deckSlots.removeAll { it.deckId == deckId }
        deckAssignments.removeAll { it.deckSlotId in removedSlotIds }
        if (deckContainerId != null) {
            containers.removeAll { it.id == deckContainerId }
            inventoryEntries.removeAll { it.containerId == deckContainerId }
        }
        if (selectedDeckId == deckId) selectedDeckId = null
        save()
    }

    fun addDeckSlot(deckId: String, cardName: String, quantity: Int, section: DeckSection, cardSelection: ScryfallCardSelection? = null) {
        deckSlots += DeckSlot(
            id = newId(),
            deckId = deckId,
            cardName = cardName.trim(),
            desiredQuantity = quantity,
            section = section,
            identityStatus = if (cardSelection == null) {
                com.commanderanalyst.core.model.CardIdentityStatus.Unresolved
            } else {
                com.commanderanalyst.core.model.CardIdentityStatus.Resolved
            },
            oracleId = cardSelection?.oracleId,
            oracleName = cardSelection?.oracleName,
            preferredScryfallCardId = cardSelection?.scryfallCardId,
            preferredPrintingName = cardSelection?.printingName,
            preferredSetCode = cardSelection?.setCode,
            preferredCollectorNumber = cardSelection?.collectorNumber
        )
        save()
    }

    fun addImportedDeckSlots(deckId: String, rows: List<ImportedDeckRow>) {
        rows.forEach { row ->
            deckSlots += DeckSlot(
                id = newId(),
                deckId = deckId,
                cardName = row.cardName.trim(),
                desiredQuantity = row.quantity,
                section = row.section,
                identityStatus = if (row.cardSelection == null) {
                    com.commanderanalyst.core.model.CardIdentityStatus.Unresolved
                } else {
                    com.commanderanalyst.core.model.CardIdentityStatus.Resolved
                },
                oracleId = row.cardSelection?.oracleId,
                oracleName = row.cardSelection?.oracleName,
                preferredScryfallCardId = row.cardSelection?.scryfallCardId,
                preferredPrintingName = row.cardSelection?.printingName,
                preferredSetCode = row.cardSelection?.setCode,
                preferredCollectorNumber = row.cardSelection?.collectorNumber
            )
        }
        save()
    }

    fun updateDeckSlot(slotId: String, cardName: String, quantity: Int, section: DeckSection, cardSelection: ScryfallCardSelection? = null) {
        deckSlots.replaceFirst(slotId) { slot ->
            slot.copy(
                cardName = cardName.trim(),
                desiredQuantity = quantity,
                section = section,
                identityStatus = if (cardSelection == null) slot.identityStatus else com.commanderanalyst.core.model.CardIdentityStatus.Resolved,
                oracleId = cardSelection?.oracleId ?: slot.oracleId,
                oracleName = cardSelection?.oracleName ?: slot.oracleName,
                preferredScryfallCardId = cardSelection?.scryfallCardId ?: slot.preferredScryfallCardId,
                preferredPrintingName = cardSelection?.printingName ?: slot.preferredPrintingName,
                preferredSetCode = cardSelection?.setCode ?: slot.preferredSetCode,
                preferredCollectorNumber = cardSelection?.collectorNumber ?: slot.preferredCollectorNumber
            )
        }
        save()
    }

    fun deleteDeckSlot(slotId: String) {
        deckSlots.removeAll { it.id == slotId }
        deckAssignments.removeAll { it.deckSlotId == slotId }
        save()
    }

    fun createContainer(name: String, type: ContainerType) {
        val container = Container(id = newId(), name = name.trim(), type = type)
        containers += container
        selectedContainerId = container.id
        save()
    }

    fun updateContainer(containerId: String, name: String, type: ContainerType) {
        containers.replaceFirst(containerId) { container ->
            container.copy(name = name.trim(), type = type)
        }
        save()
    }

    fun deleteContainer(containerId: String) {
        if (decks.any { it.containerId == containerId }) return
        containers.removeAll { it.id == containerId }
        inventoryEntries.removeAll { it.containerId == containerId }
        if (selectedContainerId == containerId) selectedContainerId = null
        save()
    }

    fun assignedQuantityFor(slotId: String): Int {
        return deckAssignments
            .filter { it.deckSlotId == slotId }
            .sumOf { it.assignedQuantity }
    }

    fun assignedEntriesFor(slotId: String): List<InventoryEntry> {
        val assignedEntryIds = deckAssignments
            .filter { it.deckSlotId == slotId }
            .map { it.inventoryEntryId }
            .toSet()
        return inventoryEntries.filter { it.id in assignedEntryIds }
    }

    fun assignmentSourceNameFor(slotId: String, inventoryEntryId: String): String? {
        val sourceContainerId = deckAssignments
            .firstOrNull { it.deckSlotId == slotId && it.inventoryEntryId == inventoryEntryId }
            ?.fromContainerId
            ?: return null
        return containerName(sourceContainerId)
    }

    fun availableEntriesFor(slot: DeckSlot): List<InventoryEntry> {
        val deckContainerId = decks.firstOrNull { it.id == slot.deckId }?.containerId
        val deckContainerIds = containers
            .filter { it.type == ContainerType.Deck }
            .map { it.id }
            .toSet()
        return inventoryEntries.filter { entry ->
            entry.containerId != deckContainerId &&
                entry.containerId !in deckContainerIds &&
                entry.quantity > 0 &&
                entry.matches(slot)
        }
    }

    fun containerName(containerId: String): String {
        return containers.firstOrNull { it.id == containerId }?.name ?: "Unknown container"
    }

    fun assignFirstAvailable(slot: DeckSlot) {
        val source = availableEntriesFor(slot).firstOrNull() ?: return
        assignInventoryEntry(slot, source)
    }

    fun assignInventoryEntry(slot: DeckSlot, source: InventoryEntry) {
        val deck = decks.firstOrNull { it.id == slot.deckId } ?: return
        val deckContainerId = ensureDeckContainer(deck)
        if (source !in availableEntriesFor(slot)) return

        val movedEntryId = if (source.quantity > 1) {
            inventoryEntries.replaceFirst(source.id) { entry -> entry.copy(quantity = entry.quantity - 1) }
            val moved = source.copy(
                id = newId(),
                containerId = deckContainerId,
                quantity = 1
            )
            inventoryEntries += moved
            moved.id
        } else {
            inventoryEntries.replaceFirst(source.id) { entry -> entry.copy(containerId = deckContainerId) }
            source.id
        }

        deckAssignments += DeckAssignment(
            id = newId(),
            deckSlotId = slot.id,
            inventoryEntryId = movedEntryId,
            assignedQuantity = 1,
            fromContainerId = source.containerId,
            toDeckId = deck.id
        )
        save()
    }

    fun unassignOne(slot: DeckSlot) {
        val assignment = deckAssignments.lastOrNull { it.deckSlotId == slot.id } ?: return
        val assignedEntry = inventoryEntries.firstOrNull { it.id == assignment.inventoryEntryId } ?: return
        val returnContainerId = assignment.fromContainerId
            ?.takeIf { containerId -> containers.any { it.id == containerId } }
            ?: containers.firstOrNull { it.type != ContainerType.Deck }?.id
            ?: return

        val mergeTarget = inventoryEntries.firstOrNull { entry ->
            entry.id != assignedEntry.id &&
                entry.containerId == returnContainerId &&
                entry.isSamePhysicalCardAs(assignedEntry)
        }

        if (mergeTarget != null) {
            inventoryEntries.replaceFirst(mergeTarget.id) { entry ->
                entry.copy(quantity = entry.quantity + assignedEntry.quantity)
            }
            inventoryEntries.removeAll { it.id == assignedEntry.id }
        } else {
            inventoryEntries.replaceFirst(assignedEntry.id) { entry ->
                entry.copy(containerId = returnContainerId)
            }
        }
        deckAssignments.removeAll { it.id == assignment.id }
        save()
    }

    fun addInventoryEntry(containerId: String, cardName: String, quantity: Int, isFoil: Boolean, cardSelection: ScryfallCardSelection? = null) {
        val newEntry = InventoryEntry(
            id = newId(),
            containerId = containerId,
            cardName = cardName.trim(),
            quantity = quantity,
            isFoil = isFoil,
            identityStatus = if (cardSelection == null) {
                com.commanderanalyst.core.model.CardIdentityStatus.Unresolved
            } else {
                com.commanderanalyst.core.model.CardIdentityStatus.Resolved
            },
            oracleId = cardSelection?.oracleId,
            oracleName = cardSelection?.oracleName,
            scryfallCardId = cardSelection?.scryfallCardId,
            printingName = cardSelection?.printingName,
            setCode = cardSelection?.setCode,
            collectorNumber = cardSelection?.collectorNumber
        )
        val mergeTarget = inventoryEntries.firstOrNull { entry ->
            entry.containerId == containerId && entry.isSamePhysicalCardAs(newEntry)
        }
        if (mergeTarget != null) {
            inventoryEntries.replaceFirst(mergeTarget.id) { entry ->
                entry.copy(quantity = entry.quantity + quantity)
            }
        } else {
            inventoryEntries += newEntry
        }
        save()
    }

    fun addImportedInventoryEntries(containerId: String, rows: List<ImportedCollectionRow>) {
        rows.forEach { row ->
            addInventoryEntry(
                containerId = containerId,
                cardName = row.cardSelection?.printingName ?: row.cardName,
                quantity = row.quantity,
                isFoil = row.isFoil,
                cardSelection = row.cardSelection
            )
        }
    }

    fun updateInventoryEntry(entryId: String, cardName: String, quantity: Int, isFoil: Boolean, cardSelection: ScryfallCardSelection? = null) {
        val current = inventoryEntries.firstOrNull { it.id == entryId } ?: return
        val updated = current.copy(
            cardName = cardName.trim(),
            quantity = quantity,
            isFoil = isFoil,
            identityStatus = if (cardSelection == null) current.identityStatus else com.commanderanalyst.core.model.CardIdentityStatus.Resolved,
            oracleId = cardSelection?.oracleId ?: current.oracleId,
            oracleName = cardSelection?.oracleName ?: current.oracleName,
            scryfallCardId = cardSelection?.scryfallCardId ?: current.scryfallCardId,
            printingName = cardSelection?.printingName ?: current.printingName,
            setCode = cardSelection?.setCode ?: current.setCode,
            collectorNumber = cardSelection?.collectorNumber ?: current.collectorNumber
        )
        val mergeTarget = inventoryEntries.firstOrNull { entry ->
            entry.id != entryId && entry.containerId == updated.containerId && entry.isSamePhysicalCardAs(updated)
        }
        if (mergeTarget != null) {
            inventoryEntries.replaceFirst(mergeTarget.id) { entry ->
                entry.copy(quantity = entry.quantity + updated.quantity)
            }
            inventoryEntries.removeAll { it.id == entryId }
        } else {
            inventoryEntries.replaceFirst(entryId) { updated }
        }
        save()
    }

    fun deleteInventoryEntry(entryId: String) {
        inventoryEntries.removeAll { it.id == entryId }
        save()
    }

    fun moveInventoryEntry(entryId: String, targetContainerId: String) {
        val entryToMove = inventoryEntries.firstOrNull { it.id == entryId } ?: return
        if (entryToMove.containerId == targetContainerId) return
        if (containers.none { it.id == targetContainerId && it.type != ContainerType.Deck }) return
        if (deckAssignments.any { it.inventoryEntryId == entryId }) return

        val mergeTarget = inventoryEntries.firstOrNull { entry ->
            entry.id != entryId &&
                entry.containerId == targetContainerId &&
                entry.isSamePhysicalCardAs(entryToMove)
        }

        if (mergeTarget != null) {
            inventoryEntries.replaceFirst(mergeTarget.id) { entry ->
                entry.copy(quantity = entry.quantity + entryToMove.quantity)
            }
            inventoryEntries.removeAll { it.id == entryId }
        } else {
            inventoryEntries.replaceFirst(entryId) { entry ->
                entry.copy(containerId = targetContainerId)
            }
        }
        selectedContainerId = targetContainerId
        save()
    }

    fun loadOrSeed() {
        val bundle = persistence.load()
        if (bundle != null) {
            replaceWith(bundle)
            return
        }

        createDeck("Karn, Legacy Reforged", "Karn, Legacy Reforged")
        val deckId = decks.first().id
        addDeckSlot(deckId, "Blightsteel Colossus", 1, DeckSection.Artifact)
        addDeckSlot(deckId, "Basalt Monolith", 1, DeckSection.Artifact)
        addDeckSlot(deckId, "Expedition Map", 1, DeckSection.Artifact)

        createContainer("Artifact binder", ContainerType.Binder)
        val containerId = containers.first().id
        addInventoryEntry(containerId, "Basalt Monolith", 1, isFoil = false)
        addInventoryEntry(containerId, "Everflowing Chalice", 2, isFoil = false)
    }

    private fun replaceWith(bundle: SyncBundle) {
        decks.clear()
        deckSlots.clear()
        deckAssignments.clear()
        containers.clear()
        inventoryEntries.clear()

        decks += bundle.decks
        deckSlots += bundle.deckSlots
        deckAssignments += bundle.deckAssignments
        containers += bundle.containers
        inventoryEntries += bundle.inventoryEntries

        selectedDeckId = decks.firstOrNull()?.id
        selectedContainerId = containers.firstOrNull()?.id
        if (mergeDuplicateUnassignedInventoryEntries()) {
            save()
        }
    }

    private fun ensureDeckContainer(deck: Deck): String {
        val existingContainerId = deck.containerId?.takeIf { containerId ->
            containers.any { it.id == containerId }
        }
        if (existingContainerId != null) return existingContainerId

        val container = Container(id = newId(), name = deck.name, type = ContainerType.Deck)
        containers += container
        decks.replaceFirst(deck.id) { it.copy(containerId = container.id) }
        return container.id
    }

    private fun save() {
        persistence.save(
            SyncBundle(
                schemaVersion = 1,
                exportedAtEpochMillis = System.currentTimeMillis(),
                containers = containers.toList(),
                inventoryEntries = inventoryEntries.toList(),
                decks = decks.toList(),
                deckSlots = deckSlots.toList(),
                deckAssignments = deckAssignments.toList()
            )
        )
    }

    private fun mergeDuplicateUnassignedInventoryEntries(): Boolean {
        val assignedEntryIds = deckAssignments.map { it.inventoryEntryId }.toSet()
        var changed = false

        inventoryEntries.toList().forEach { entry ->
            if (entry.id in assignedEntryIds || inventoryEntries.none { it.id == entry.id }) return@forEach

            val mergeTarget = inventoryEntries.firstOrNull { target ->
                target.id != entry.id &&
                    target.id !in assignedEntryIds &&
                    target.containerId == entry.containerId &&
                    target.isSamePhysicalCardAs(entry)
            }

            if (mergeTarget != null) {
                inventoryEntries.replaceFirst(mergeTarget.id) { target ->
                    target.copy(quantity = target.quantity + entry.quantity)
                }
                inventoryEntries.removeAll { it.id == entry.id }
                changed = true
            }
        }

        return changed
    }
}

private fun InventoryEntry.matches(slot: DeckSlot): Boolean {
    if (slot.oracleId != null && oracleId != null) return slot.oracleId == oracleId
    return cardName.normalizedCardName() == slot.cardName.normalizedCardName()
}

private fun InventoryEntry.isSamePhysicalCardAs(other: InventoryEntry): Boolean {
    if (scryfallCardId != null && other.scryfallCardId != null) {
        return scryfallCardId == other.scryfallCardId && isFoil == other.isFoil
    }
    return cardName.normalizedCardName() == other.cardName.normalizedCardName() &&
        setCode == other.setCode &&
        collectorNumber == other.collectorNumber &&
        isFoil == other.isFoil
}

private fun String.normalizedCardName(): String {
    return trim().lowercase().replace(Regex("\\s+"), " ")
}

private inline fun <T> MutableList<T>.replaceFirst(
    id: String,
    crossinline replacement: (T) -> T
) where T : Any {
    val index = indexOfFirst {
        when (it) {
            is Deck -> it.id == id
            is DeckSlot -> it.id == id
            is Container -> it.id == id
            is InventoryEntry -> it.id == id
            else -> false
        }
    }
    if (index >= 0) this[index] = replacement(this[index])
}

private fun newId(): String = UUID.randomUUID().toString()

data class ImportedDeckRow(
    val quantity: Int,
    val cardName: String,
    val section: DeckSection,
    val cardSelection: ScryfallCardSelection? = null
)

data class ImportedCollectionRow(
    val quantity: Int,
    val cardName: String,
    val setCode: String? = null,
    val collectorNumber: String? = null,
    val scryfallCardId: String? = null,
    val isFoil: Boolean = false,
    val cardSelection: ScryfallCardSelection? = null,
    val printingOptions: List<ScryfallCardSelection> = emptyList()
)
