package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.ContainerType;
import com.commanderanthology.core.collection.InventoryEntry;
import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckAssignment;
import com.commanderanthology.core.deck.DeckSection;
import com.commanderanthology.core.deck.DeckSlot;
import com.commanderanthology.core.deck.CardIdentityStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

final class DesktopAppState {
    private final List<Deck> decks = new ArrayList<>();
    private final List<DeckSlot> deckSlots = new ArrayList<>();
    private final List<DeckAssignment> deckAssignments = new ArrayList<>();
    private final List<CardContainer> containers = new ArrayList<>();
    private final List<InventoryEntry> inventoryEntries = new ArrayList<>();
    private final DesktopPersistence persistence;
    private final ScryfallCardLookupService cardLookup = new ScryfallCardLookupService();

    DesktopAppState() {
        this(new DesktopPersistence());
    }

    DesktopAppState(DesktopPersistence persistence) {
        this.persistence = persistence;
        persistence.load().ifPresentOrElse(this::replaceWith, this::loadInitialData);
    }

    List<Deck> decks() {
        return decks.stream().sorted(Comparator.comparing(Deck::name, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    List<DeckSlot> deckSlotsFor(String deckId) {
        return deckSlots.stream()
                .filter(slot -> slot.deckId().equals(deckId))
                .sorted(Comparator.comparing(DeckSlot::section).thenComparing(DeckSlot::cardName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    List<CardContainer> visibleContainers() {
        return containers.stream()
                .filter(container -> container.type() != ContainerType.DECK)
                .sorted(Comparator.comparing(CardContainer::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    List<InventoryEntry> entriesFor(String containerId) {
        return inventoryEntries.stream()
                .filter(entry -> entry.containerId().equals(containerId))
                .sorted(Comparator.comparing(InventoryEntry::cardName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    Deck createDeck(String name, String commanderName) {
        CommanderValidation commander = requireCommander(commanderName);
        String deckContainerId = newId();
        CardContainer deckContainer = new CardContainer(deckContainerId, name.trim(), ContainerType.DECK);
        Deck deck = new Deck(newId(), name.trim(), commander.cardName(), deckContainerId);
        containers.add(deckContainer);
        decks.add(deck);
        ensureCommanderSlot(deck.id(), commander.cardName());
        save();
        return deck;
    }

    void updateDeck(String deckId, String name, String commanderName) {
        Deck deck = deckById(deckId);
        CommanderValidation commander = requireCommander(commanderName);
        replaceDeck(deckId, new Deck(deck.id(), name.trim(), commander.cardName(), deck.containerId()));
        if (deck.containerId() != null) {
            replaceContainer(deck.containerId(), new CardContainer(deck.containerId(), name.trim(), ContainerType.DECK));
        }
        ensureCommanderSlot(deck.id(), commander.cardName());
        save();
    }

    void deleteDeck(String deckId) {
        Deck deck = deckById(deckId);
        List<String> removedSlotIds = deckSlots.stream()
                .filter(slot -> slot.deckId().equals(deckId))
                .map(DeckSlot::id)
                .toList();
        decks.removeIf(existing -> existing.id().equals(deckId));
        deckSlots.removeIf(slot -> slot.deckId().equals(deckId));
        deckAssignments.removeIf(assignment -> removedSlotIds.contains(assignment.deckSlotId()));
        if (deck.containerId() != null) {
            inventoryEntries.removeIf(entry -> entry.containerId().equals(deck.containerId()));
            containers.removeIf(container -> container.id().equals(deck.containerId()));
        }
        save();
    }

    Deck createImportedDeck(String name, String commanderName, List<ImportedDeckRow> rows) {
        CommanderValidation commander = requireCommander(commanderName);
        String deckContainerId = newId();
        CardContainer deckContainer = new CardContainer(deckContainerId, name.trim(), ContainerType.DECK);
        Deck deck = new Deck(newId(), name.trim(), commander.cardName(), deckContainerId);
        containers.add(deckContainer);
        decks.add(deck);
        ensureCommanderSlot(deck.id(), commander.cardName());
        for (ImportedDeckRow row : rows) {
            if (sameCardName(row.cardName(), commander.cardName())) {
                continue;
            }
            deckSlots.add(resolvedDeckSlot(newId(), deck.id(), row));
        }
        save();
        return deck;
    }

    DeckSlot addDeckSlot(String deckId, String cardName, int quantity, DeckSection section) {
        DeckSlot slot = resolvedDeckSlot(newId(), deckId, cardName, quantity, section);
        deckSlots.add(slot);
        save();
        return slot;
    }

    DeckSlot addDeckSlot(String deckId, ImportedDeckRow row) {
        DeckSlot slot = resolvedDeckSlot(newId(), deckId, row);
        deckSlots.add(slot);
        save();
        return slot;
    }

    void updateDeckSlot(String slotId, String cardName, int quantity, DeckSection section) {
        DeckSlot slot = deckSlotById(slotId);
        replaceDeckSlot(slotId, resolvedDeckSlot(slot.id(), slot.deckId(), cardName, quantity, section));
        save();
    }

    void updateDeckSlot(String slotId, ImportedDeckRow row) {
        DeckSlot slot = deckSlotById(slotId);
        replaceDeckSlot(slotId, resolvedDeckSlot(slot.id(), slot.deckId(), row));
        save();
    }

    void deleteDeckSlot(String slotId) {
        deckSlots.removeIf(slot -> slot.id().equals(slotId));
        deckAssignments.removeIf(assignment -> assignment.deckSlotId().equals(slotId));
        save();
    }

    Deck setCommanderFromSlot(String slotId) {
        DeckSlot slot = deckSlotById(slotId);
        CommanderValidation commander = requireCommander(slot.oracleName() == null ? slot.cardName() : slot.oracleName());
        Deck deck = deckById(slot.deckId());

        if (slot.section() != DeckSection.COMMANDER) {
            if (slot.desiredQuantity() <= 1) {
                deckSlots.removeIf(existing -> existing.id().equals(slot.id()));
                deckAssignments.removeIf(assignment -> assignment.deckSlotId().equals(slot.id()));
            } else {
                replaceDeckSlot(slot.id(), new DeckSlot(
                        slot.id(),
                        slot.deckId(),
                        slot.cardName(),
                        slot.desiredQuantity() - 1,
                        slot.section(),
                        slot.identityStatus(),
                        slot.oracleId(),
                        slot.oracleName(),
                        slot.preferredScryfallCardId(),
                        slot.preferredPrintingName(),
                        slot.preferredSetCode(),
                        slot.preferredCollectorNumber()
                ));
            }
        }

        Deck replacement = new Deck(deck.id(), deck.name(), commander.cardName(), deck.containerId());
        replaceDeck(deck.id(), replacement);
        ensureCommanderSlot(deck.id(), commander.cardName());
        save();
        return replacement;
    }

    void addImportedDeckSlots(String deckId, List<ImportedDeckRow> rows) {
        for (ImportedDeckRow row : rows) {
            deckSlots.add(resolvedDeckSlot(newId(), deckId, row));
        }
        save();
    }

    CardContainer createContainer(String name, ContainerType type) {
        CardContainer container = new CardContainer(newId(), name.trim(), type);
        containers.add(container);
        save();
        return container;
    }

    void updateContainer(String containerId, String name, ContainerType type) {
        if (decks.stream().anyMatch(deck -> containerId.equals(deck.containerId()))) {
            return;
        }
        replaceContainer(containerId, new CardContainer(containerId, name.trim(), type));
        save();
    }

    void deleteContainer(String containerId) {
        if (decks.stream().anyMatch(deck -> containerId.equals(deck.containerId()))) {
            return;
        }
        containers.removeIf(container -> container.id().equals(containerId));
        inventoryEntries.removeIf(entry -> entry.containerId().equals(containerId));
        save();
    }

    InventoryEntry addInventoryEntry(String containerId, String cardName, int quantity, boolean foil) {
        InventoryEntry entry = resolvedInventoryEntry(newId(), containerId, cardName, quantity, foil);
        addOrMergeInventoryEntry(entry);
        save();
        return entry;
    }

    InventoryEntry addInventoryEntry(String containerId, ImportedCollectionRow row) {
        InventoryEntry entry = resolvedInventoryEntry(newId(), containerId, row);
        addOrMergeInventoryEntry(entry);
        save();
        return entry;
    }

    void addImportedInventoryEntries(String containerId, List<ImportedCollectionRow> rows) {
        for (ImportedCollectionRow row : rows) {
            addOrMergeInventoryEntry(resolvedInventoryEntry(newId(), containerId, row));
        }
        save();
    }

    void updateInventoryEntry(String entryId, String cardName, int quantity, boolean foil) {
        InventoryEntry entry = inventoryEntryById(entryId);
        mergeUpdatedInventoryEntry(entryId, resolvedInventoryEntry(entry.id(), entry.containerId(), cardName, quantity, foil));
        save();
    }

    void updateInventoryEntry(String entryId, ImportedCollectionRow row) {
        InventoryEntry entry = inventoryEntryById(entryId);
        mergeUpdatedInventoryEntry(entryId, resolvedInventoryEntry(entry.id(), entry.containerId(), row));
        save();
    }

    int resolveAllDeckSlots(String deckId) {
        int resolved = 0;
        for (DeckSlot slot : new ArrayList<>(deckSlotsFor(deckId))) {
            DeckSlot replacement = resolvedDeckSlot(slot.id(), slot.deckId(), slot.cardName(), slot.desiredQuantity(), slot.section());
            replaceDeckSlot(slot.id(), replacement);
            if (replacement.identityStatus() == CardIdentityStatus.RESOLVED) {
                resolved++;
            }
        }
        save();
        return resolved;
    }

    int resolveAllInventoryEntries(String containerId) {
        int resolved = 0;
        for (InventoryEntry entry : new ArrayList<>(entriesFor(containerId))) {
            InventoryEntry replacement = resolvedInventoryEntry(entry.id(), entry.containerId(), entry.cardName(), entry.quantity(), entry.foil());
            replaceInventory(entry.id(), replacement);
            if (replacement.identityStatus() == CardIdentityStatus.RESOLVED) {
                resolved++;
            }
        }
        save();
        return resolved;
    }

    void deleteInventoryEntry(String entryId) {
        inventoryEntries.removeIf(entry -> entry.id().equals(entryId));
        deckAssignments.removeIf(assignment -> assignment.inventoryEntryId().equals(entryId));
        save();
    }

    void moveInventoryEntry(String entryId, String targetContainerId) {
        InventoryEntry entry = inventoryEntryById(entryId);
        if (entry.containerId().equals(targetContainerId)) {
            return;
        }
        if (containerById(targetContainerId).type() == ContainerType.DECK) {
            return;
        }
        if (deckAssignments.stream().anyMatch(assignment -> assignment.inventoryEntryId().equals(entryId))) {
            return;
        }
        InventoryEntry mergeTarget = inventoryEntries.stream()
                .filter(existing -> !existing.id().equals(entry.id()))
                .filter(existing -> existing.containerId().equals(targetContainerId))
                .filter(existing -> existing.samePhysicalCardAs(entry))
                .findFirst()
                .orElse(null);
        if (mergeTarget == null) {
            replaceInventory(entry.id(), new InventoryEntry(
                    entry.id(),
                    targetContainerId,
                    entry.cardName(),
                    entry.quantity(),
                    entry.identityStatus(),
                    entry.oracleId(),
                    entry.oracleName(),
                    entry.scryfallCardId(),
                    entry.printingName(),
                    entry.setCode(),
                    entry.collectorNumber(),
                    entry.foil()
            ));
        } else {
            replaceInventory(mergeTarget.id(), new InventoryEntry(
                    mergeTarget.id(),
                    mergeTarget.containerId(),
                    mergeTarget.cardName(),
                    mergeTarget.quantity() + entry.quantity(),
                    mergeTarget.identityStatus(),
                    mergeTarget.oracleId(),
                    mergeTarget.oracleName(),
                    mergeTarget.scryfallCardId(),
                    mergeTarget.printingName(),
                    mergeTarget.setCode(),
                    mergeTarget.collectorNumber(),
                    mergeTarget.foil()
            ));
            inventoryEntries.removeIf(existing -> existing.id().equals(entry.id()));
        }
        save();
    }

    int assignedQuantityFor(String slotId) {
        return deckAssignments.stream()
                .filter(assignment -> assignment.deckSlotId().equals(slotId))
                .mapToInt(DeckAssignment::assignedQuantity)
                .sum();
    }

    List<InventoryEntry> assignedEntriesFor(String slotId) {
        List<String> entryIds = deckAssignments.stream()
                .filter(assignment -> assignment.deckSlotId().equals(slotId))
                .map(DeckAssignment::inventoryEntryId)
                .toList();
        return inventoryEntries.stream()
                .filter(entry -> entryIds.contains(entry.id()))
                .toList();
    }

    String assignmentSourceNameFor(String slotId, String inventoryEntryId) {
        return deckAssignments.stream()
                .filter(assignment -> assignment.deckSlotId().equals(slotId))
                .filter(assignment -> assignment.inventoryEntryId().equals(inventoryEntryId))
                .map(DeckAssignment::fromContainerId)
                .filter(id -> id != null && !id.isBlank())
                .findFirst()
                .map(this::containerName)
                .orElse(null);
    }

    List<InventoryEntry> availableEntriesFor(DeckSlot slot) {
        String deckContainerId = deckById(slot.deckId()).containerId();
        return inventoryEntries.stream()
                .filter(entry -> !entry.containerId().equals(deckContainerId))
                .filter(entry -> containerById(entry.containerId()).type() != ContainerType.DECK)
                .filter(entry -> entry.matchesDeckSlot(slot.cardName(), slot.oracleId()))
                .toList();
    }

    boolean assignFirstAvailable(DeckSlot slot) {
        List<InventoryEntry> available = availableEntriesFor(slot);
        if (available.isEmpty()) {
            return false;
        }
        assignInventoryEntry(slot, available.get(0));
        return true;
    }

    boolean assignInventoryEntryById(DeckSlot slot, String inventoryEntryId) {
        InventoryEntry entry = inventoryEntries.stream()
                .filter(existing -> existing.id().equals(inventoryEntryId))
                .findFirst()
                .orElse(null);
        if (entry == null || availableEntriesFor(slot).stream().noneMatch(available -> available.id().equals(inventoryEntryId))) {
            return false;
        }
        assignInventoryEntry(slot, entry);
        return true;
    }

    boolean unassignOne(DeckSlot slot) {
        DeckAssignment assignment = deckAssignments.stream()
                .filter(existing -> existing.deckSlotId().equals(slot.id()))
                .reduce((first, second) -> second)
                .orElse(null);
        if (assignment == null) {
            return false;
        }
        InventoryEntry assignedEntry = inventoryEntries.stream()
                .filter(entry -> entry.id().equals(assignment.inventoryEntryId()))
                .findFirst()
                .orElse(null);
        if (assignedEntry == null) {
            deckAssignments.removeIf(existing -> existing.id().equals(assignment.id()));
            save();
            return true;
        }
        String returnContainerId = assignment.fromContainerId();
        boolean returnContainerExists = returnContainerId != null && containers.stream()
                .anyMatch(container -> container.id().equals(assignment.fromContainerId()));
        if (!returnContainerExists) {
            returnContainerId = containers.stream()
                    .filter(container -> container.type() != ContainerType.DECK)
                    .map(CardContainer::id)
                    .findFirst()
                    .orElse(null);
        }
        if (returnContainerId == null) {
            return false;
        }

        String finalReturnContainerId = returnContainerId;
        InventoryEntry mergeTarget = inventoryEntries.stream()
                .filter(entry -> !entry.id().equals(assignedEntry.id()))
                .filter(entry -> entry.containerId().equals(finalReturnContainerId))
                .filter(entry -> entry.samePhysicalCardAs(assignedEntry))
                .findFirst()
                .orElse(null);
        if (mergeTarget == null) {
            replaceInventory(assignedEntry.id(), new InventoryEntry(
                    assignedEntry.id(),
                    finalReturnContainerId,
                    assignedEntry.cardName(),
                    assignedEntry.quantity(),
                    assignedEntry.identityStatus(),
                    assignedEntry.oracleId(),
                    assignedEntry.oracleName(),
                    assignedEntry.scryfallCardId(),
                    assignedEntry.printingName(),
                    assignedEntry.setCode(),
                    assignedEntry.collectorNumber(),
                    assignedEntry.foil()
            ));
        } else {
            replaceInventory(mergeTarget.id(), new InventoryEntry(
                    mergeTarget.id(),
                    mergeTarget.containerId(),
                    mergeTarget.cardName(),
                    mergeTarget.quantity() + assignedEntry.quantity(),
                    mergeTarget.identityStatus(),
                    mergeTarget.oracleId(),
                    mergeTarget.oracleName(),
                    mergeTarget.scryfallCardId(),
                    mergeTarget.printingName(),
                    mergeTarget.setCode(),
                    mergeTarget.collectorNumber(),
                    mergeTarget.foil()
            ));
            inventoryEntries.removeIf(entry -> entry.id().equals(assignedEntry.id()));
        }
        deckAssignments.removeIf(existing -> existing.id().equals(assignment.id()));
        save();
        return true;
    }

    String containerName(String containerId) {
        return containerById(containerId).name();
    }

    boolean hasDeckNamed(String deckName) {
        return decks.stream().anyMatch(deck -> deck.name().equalsIgnoreCase(deckName.trim()));
    }

    int importLegacyTestDecks() {
        int imported = LegacyTestDeckSeeder.seedAvailableDecks(this);
        if (imported > 0) {
            save();
        }
        return imported;
    }

    boolean importCcBuilderUserData() {
        return LegacyCcBuilderDataImporter.loadSnapshot()
                .map(snapshot -> {
                    replaceWith(snapshot);
                    resolveCurrentState(false);
                    save();
                    return true;
                })
                .orElse(false);
    }

    int validateAllCardInputs() {
        int resolved = resolveCurrentState(false);
        save();
        return resolved;
    }

    int unresolvedCardInputCount() {
        int unresolvedSlots = (int) deckSlots.stream()
                .filter(slot -> slot.identityStatus() != CardIdentityStatus.RESOLVED)
                .count();
        int unresolvedEntries = (int) inventoryEntries.stream()
                .filter(entry -> entry.identityStatus() != CardIdentityStatus.RESOLVED)
                .count();
        return unresolvedSlots + unresolvedEntries;
    }

    Path dataFile() {
        return persistence.dataFile();
    }

    void exportBundle(Path target) {
        save();
        try {
            Files.copy(persistence.dataFile(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            throw new IllegalStateException("Could not export sync bundle", error);
        }
    }

    void importBundle(Path source) {
        DesktopSnapshot snapshot = new DesktopPersistence(source).load()
                .orElseThrow(() -> new IllegalArgumentException("Selected file is not a readable Anthology bundle."));
        replaceWith(snapshot);
        resolveCurrentState(false);
        save();
    }

    private void assignInventoryEntry(DeckSlot slot, InventoryEntry source) {
        Deck deck = deckById(slot.deckId());
        InventoryEntry moved;
        if (source.quantity() > 1) {
            replaceInventory(source.id(), new InventoryEntry(
                    source.id(),
                    source.containerId(),
                    source.cardName(),
                    source.quantity() - 1,
                    source.identityStatus(),
                    source.oracleId(),
                    source.oracleName(),
                    source.scryfallCardId(),
                    source.printingName(),
                    source.setCode(),
                    source.collectorNumber(),
                    source.foil()
            ));
            moved = new InventoryEntry(
                    newId(),
                    deck.containerId(),
                    source.cardName(),
                    1,
                    source.identityStatus(),
                    source.oracleId(),
                    source.oracleName(),
                    source.scryfallCardId(),
                    source.printingName(),
                    source.setCode(),
                    source.collectorNumber(),
                    source.foil()
            );
            inventoryEntries.add(moved);
        } else {
            moved = new InventoryEntry(
                    source.id(),
                    deck.containerId(),
                    source.cardName(),
                    source.quantity(),
                    source.identityStatus(),
                    source.oracleId(),
                    source.oracleName(),
                    source.scryfallCardId(),
                    source.printingName(),
                    source.setCode(),
                    source.collectorNumber(),
                    source.foil()
            );
            replaceInventory(source.id(), moved);
        }

        deckAssignments.add(new DeckAssignment(newId(), slot.id(), moved.id(), 1, source.containerId(), deck.id()));
        save();
    }

    private void addOrMergeInventoryEntry(InventoryEntry entry) {
        InventoryEntry mergeTarget = inventoryEntries.stream()
                .filter(existing -> existing.containerId().equals(entry.containerId()))
                .filter(existing -> existing.samePhysicalCardAs(entry))
                .findFirst()
                .orElse(null);
        if (mergeTarget == null) {
            inventoryEntries.add(entry);
            return;
        }
        replaceInventory(mergeTarget.id(), withQuantity(mergeTarget, mergeTarget.quantity() + entry.quantity()));
    }

    private void mergeUpdatedInventoryEntry(String entryId, InventoryEntry updated) {
        InventoryEntry mergeTarget = inventoryEntries.stream()
                .filter(existing -> !existing.id().equals(entryId))
                .filter(existing -> existing.containerId().equals(updated.containerId()))
                .filter(existing -> existing.samePhysicalCardAs(updated))
                .findFirst()
                .orElse(null);
        if (mergeTarget == null) {
            replaceInventory(entryId, updated);
            return;
        }
        replaceInventory(mergeTarget.id(), withQuantity(mergeTarget, mergeTarget.quantity() + updated.quantity()));
        inventoryEntries.removeIf(existing -> existing.id().equals(entryId));
        deckAssignments.removeIf(assignment -> assignment.inventoryEntryId().equals(entryId));
    }

    private static InventoryEntry withQuantity(InventoryEntry entry, int quantity) {
        return new InventoryEntry(
                entry.id(),
                entry.containerId(),
                entry.cardName(),
                quantity,
                entry.identityStatus(),
                entry.oracleId(),
                entry.oracleName(),
                entry.scryfallCardId(),
                entry.printingName(),
                entry.setCode(),
                entry.collectorNumber(),
                entry.foil()
        );
    }

    private void seed() {
        int importedDecks = LegacyTestDeckSeeder.seedAvailableDecks(this);
        if (importedDecks == 0) {
            Deck deck = createDeck("Karn, Legacy Reforged", "Karn, Legacy Reforged");
            addDeckSlot(deck.id(), "Basalt Monolith", 1, DeckSection.ARTIFACT);
            addDeckSlot(deck.id(), "Expedition Map", 1, DeckSection.ARTIFACT);
            addDeckSlot(deck.id(), "Blightsteel Colossus", 1, DeckSection.ARTIFACT);
        }

        CardContainer binder = createContainer("Artifact binder", ContainerType.BINDER);
        addInventoryEntry(binder.id(), "Basalt Monolith", 1, false);
        addInventoryEntry(binder.id(), "Everflowing Chalice", 2, false);
        addInventoryEntry(binder.id(), "Sol Ring", 1, false);
    }

    private int resolveCurrentState(boolean replaceAlreadyResolved) {
        int resolved = 0;
        for (DeckSlot slot : new ArrayList<>(deckSlots)) {
            if (!replaceAlreadyResolved && slot.identityStatus() == CardIdentityStatus.RESOLVED) {
                continue;
            }
            DeckSlot replacement = resolvedDeckSlot(slot.id(), slot.deckId(), slot.cardName(), slot.desiredQuantity(), slot.section());
            replaceDeckSlot(slot.id(), replacement);
            if (replacement.identityStatus() == CardIdentityStatus.RESOLVED) {
                resolved++;
            }
        }
        for (InventoryEntry entry : new ArrayList<>(inventoryEntries)) {
            if (!replaceAlreadyResolved && entry.identityStatus() == CardIdentityStatus.RESOLVED) {
                continue;
            }
            InventoryEntry replacement = resolvedInventoryEntry(entry.id(), entry.containerId(), entry.cardName(), entry.quantity(), entry.foil());
            replaceInventory(entry.id(), replacement);
            if (replacement.identityStatus() == CardIdentityStatus.RESOLVED) {
                resolved++;
            }
        }
        return resolved;
    }

    private DeckSlot resolvedDeckSlot(String id, String deckId, String cardName, int quantity, DeckSection section) {
        String cleanName = cardName.trim();
        return cardLookup.lookupPreferredCard(cleanName, null)
                .map(selection -> new DeckSlot(
                        id,
                        deckId,
                        selection.oracleName(),
                        quantity,
                        section,
                        CardIdentityStatus.RESOLVED,
                        selection.oracleId(),
                        selection.oracleName(),
                        null,
                        null,
                        null,
                        null
                ))
                .orElseGet(() -> DeckSlot.unresolved(id, deckId, cleanName, quantity, section));
    }

    private DeckSlot resolvedDeckSlot(String id, String deckId, ImportedDeckRow row) {
        ScryfallCardSelection selection = null;
        if (row.scryfallCardId() != null) {
            selection = cardLookup.lookupCardById(row.scryfallCardId()).orElse(null);
        }
        boolean explicitPrinting = row.scryfallCardId() != null;
        if (selection == null) {
            selection = cardLookup.lookupPreferredCard(row.cardName(), row.setCode()).orElse(null);
        }
        if (selection == null) {
            return DeckSlot.unresolved(id, deckId, row.cardName(), row.quantity(), row.section());
        }
        return new DeckSlot(
                id,
                deckId,
                selection.oracleName(),
                row.quantity(),
                row.section(),
                CardIdentityStatus.RESOLVED,
                selection.oracleId(),
                selection.oracleName(),
                explicitPrinting ? selection.scryfallCardId() : null,
                explicitPrinting ? selection.printingName() : null,
                explicitPrinting ? selection.setCode() : null,
                explicitPrinting ? selection.collectorNumber() : null
        );
    }

    private InventoryEntry resolvedInventoryEntry(String id, String containerId, String cardName, int quantity, boolean foil) {
        String cleanName = cardName.trim();
        return cardLookup.lookupPreferredCard(cleanName, null)
                .map(selection -> new InventoryEntry(
                        id,
                        containerId,
                        selection.printingName(),
                        quantity,
                        CardIdentityStatus.RESOLVED,
                        selection.oracleId(),
                        selection.oracleName(),
                        selection.scryfallCardId(),
                        selection.printingName(),
                        selection.setCode(),
                        selection.collectorNumber(),
                        foil
                ))
                .orElseGet(() -> InventoryEntry.unresolved(id, containerId, cleanName, quantity, foil));
    }

    private InventoryEntry resolvedInventoryEntry(String id, String containerId, ImportedCollectionRow row) {
        ScryfallCardSelection selection = null;
        if (row.scryfallCardId() != null) {
            selection = cardLookup.lookupCardById(row.scryfallCardId()).orElse(null);
        }
        if (selection == null) {
            selection = cardLookup.lookupPreferredCard(row.cardName(), row.setCode()).orElse(null);
        }
        if (selection == null) {
            return InventoryEntry.unresolved(id, containerId, row.cardName(), row.quantity(), row.foil());
        }
        return new InventoryEntry(
                id,
                containerId,
                selection.printingName(),
                row.quantity(),
                CardIdentityStatus.RESOLVED,
                selection.oracleId(),
                selection.oracleName(),
                selection.scryfallCardId(),
                selection.printingName(),
                selection.setCode(),
                selection.collectorNumber(),
                row.foil()
        );
    }

    private void loadInitialData() {
        if (importCcBuilderUserData()) {
            return;
        }
        seed();
        save();
    }

    private void replaceWith(DesktopSnapshot snapshot) {
        decks.clear();
        deckSlots.clear();
        deckAssignments.clear();
        containers.clear();
        inventoryEntries.clear();

        decks.addAll(snapshot.decks());
        deckSlots.addAll(snapshot.deckSlots());
        deckAssignments.addAll(snapshot.deckAssignments());
        containers.addAll(snapshot.containers());
        inventoryEntries.addAll(snapshot.inventoryEntries());
    }

    private void save() {
        persistence.save(new DesktopSnapshot(decks, deckSlots, deckAssignments, containers, inventoryEntries));
    }

    private Deck deckById(String deckId) {
        return decks.stream()
                .filter(deck -> deck.id().equals(deckId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown deck: " + deckId));
    }

    private DeckSlot deckSlotById(String slotId) {
        return deckSlots.stream()
                .filter(slot -> slot.id().equals(slotId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown deck slot: " + slotId));
    }

    private CardContainer containerById(String containerId) {
        return containers.stream()
                .filter(container -> container.id().equals(containerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown container: " + containerId));
    }

    private InventoryEntry inventoryEntryById(String entryId) {
        return inventoryEntries.stream()
                .filter(entry -> entry.id().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory entry: " + entryId));
    }

    private void replaceInventory(String entryId, InventoryEntry replacement) {
        for (int index = 0; index < inventoryEntries.size(); index++) {
            if (inventoryEntries.get(index).id().equals(entryId)) {
                inventoryEntries.set(index, replacement);
                return;
            }
        }
    }

    private void replaceDeck(String deckId, Deck replacement) {
        for (int index = 0; index < decks.size(); index++) {
            if (decks.get(index).id().equals(deckId)) {
                decks.set(index, replacement);
                return;
            }
        }
    }

    private void replaceDeckSlot(String slotId, DeckSlot replacement) {
        for (int index = 0; index < deckSlots.size(); index++) {
            if (deckSlots.get(index).id().equals(slotId)) {
                deckSlots.set(index, replacement);
                return;
            }
        }
    }

    private void replaceContainer(String containerId, CardContainer replacement) {
        for (int index = 0; index < containers.size(); index++) {
            if (containers.get(index).id().equals(containerId)) {
                containers.set(index, replacement);
                return;
            }
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CommanderValidation requireCommander(String commanderName) {
        CommanderValidation validation = cardLookup.validateCommander(commanderName);
        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.message());
        }
        return validation;
    }

    private void ensureCommanderSlot(String deckId, String commanderName) {
        DeckSlot commanderSlot = deckSlots.stream()
                .filter(slot -> slot.deckId().equals(deckId))
                .filter(slot -> slot.section() == DeckSection.COMMANDER)
                .findFirst()
                .orElse(null);
        DeckSlot replacement = resolvedDeckSlot(
                commanderSlot == null ? newId() : commanderSlot.id(),
                deckId,
                commanderName,
                1,
                DeckSection.COMMANDER
        );
        if (commanderSlot == null) {
            deckSlots.add(replacement);
        } else {
            replaceDeckSlot(commanderSlot.id(), replacement);
        }
        deckSlots.removeIf(slot ->
                slot.deckId().equals(deckId)
                        && slot.section() == DeckSection.COMMANDER
                        && !slot.id().equals(replacement.id()));
    }

    private static boolean sameCardName(String left, String right) {
        return normalizeCardName(left).equals(normalizeCardName(right));
    }

    private static String normalizeCardName(String value) {
        return value == null ? "" : value.trim().toLowerCase().replaceAll("[_'`]", "'").replaceAll("\\s+", " ");
    }

    private static String newId() {
        return UUID.randomUUID().toString();
    }
}
