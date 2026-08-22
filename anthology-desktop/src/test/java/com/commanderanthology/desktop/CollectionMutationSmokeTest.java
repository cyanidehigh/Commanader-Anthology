package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.ContainerType;
import com.commanderanthology.core.collection.InventoryEntry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CollectionMutationSmokeTest {
    private CollectionMutationSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("anthology-collection-test");
        DesktopAppState state = new DesktopAppState(new DesktopPersistence(tempDir.resolve("state.json")));

        CardContainer binder = state.createContainer("Test Binder A", ContainerType.BINDER);
        CardContainer box = state.createContainer("Test Box B", ContainerType.BOX);

        InventoryEntry solRing = state.addInventoryEntry(binder.id(), "Sol Ring", 2, false);
        state.updateInventoryEntry(solRing.id(), "Sol Ring", 3, true);
        InventoryEntry edited = onlyEntry(state.entriesFor(binder.id()), "edited binder entry");
        require(edited.quantity() == 3, "edited quantity");
        require(edited.foil(), "edited foil");

        state.moveInventoryEntry(edited.id(), box.id());
        require(state.entriesFor(binder.id()).isEmpty(), "source emptied after move");
        InventoryEntry moved = onlyEntry(state.entriesFor(box.id()), "moved box entry");
        require("Sol Ring".equals(moved.cardName()), "moved card name");

        InventoryEntry duplicate = state.addInventoryEntry(binder.id(), "Sol Ring", 1, true);
        state.moveInventoryEntry(duplicate.id(), box.id());
        InventoryEntry merged = onlyEntry(state.entriesFor(box.id()), "merged box entry");
        require(merged.quantity() == 4, "merge quantity");

        state.addInventoryEntry(binder.id(), "Migration Test Relic", 1, false);
        state.addInventoryEntry(binder.id(), "Migration Test Relic", 2, false);
        List<InventoryEntry> binderEntries = state.entriesFor(binder.id());
        require(binderEntries.size() == 1, "duplicate add merged row count");
        require(binderEntries.get(0).quantity() == 3, "duplicate add merged quantity");

        InventoryEntry mergeSource = state.addInventoryEntry(binder.id(), "Migration Test Relic Variant", 4, false);
        state.updateInventoryEntry(mergeSource.id(), "Migration Test Relic", 5, false);
        binderEntries = state.entriesFor(binder.id());
        require(binderEntries.size() == 1, "edit into duplicate merged row count");
        require(binderEntries.get(0).quantity() == 8, "edit into duplicate merged quantity");

        state.updateContainer(box.id(), "Updated Test Box", ContainerType.SET);
        CardContainer updated = state.visibleContainers().stream()
                .filter(container -> container.id().equals(box.id()))
                .findFirst()
                .orElseThrow();
        require("Updated Test Box".equals(updated.name()), "container rename");
        require(updated.type() == ContainerType.SET, "container type update");

        state.deleteInventoryEntry(merged.id());
        require(state.entriesFor(box.id()).isEmpty(), "entry delete");
        state.deleteContainer(box.id());
        require(state.visibleContainers().stream().noneMatch(container -> container.id().equals(box.id())), "container delete");

        System.out.println("Collection mutation smoke test passed: " + tempDir);
    }

    private static InventoryEntry onlyEntry(List<InventoryEntry> entries, String label) {
        require(entries.size() == 1, label + " count");
        return entries.get(0);
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
