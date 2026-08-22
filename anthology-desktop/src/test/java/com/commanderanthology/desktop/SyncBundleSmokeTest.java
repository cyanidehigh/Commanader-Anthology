package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.ContainerType;

import java.nio.file.Files;
import java.nio.file.Path;

public final class SyncBundleSmokeTest {
    private SyncBundleSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("anthology-sync-test");
        DesktopAppState source = new DesktopAppState(new DesktopPersistence(tempDir.resolve("source.json")));
        CardContainer binder = source.createContainer("Sync Test Binder", ContainerType.BINDER);
        source.addInventoryEntry(binder.id(), "Sol Ring", 1, false);
        Path bundle = tempDir.resolve("bundle.json");
        source.exportBundle(bundle);
        require(Files.exists(bundle), "bundle exists");
        require(Files.readString(bundle).contains("\"inventoryEntries\""), "bundle contains inventory");

        DesktopAppState target = new DesktopAppState(new DesktopPersistence(tempDir.resolve("target.json")));
        target.importBundle(bundle);
        require(target.visibleContainers().stream().anyMatch(container -> "Sync Test Binder".equals(container.name())), "imported container");
        require(target.visibleContainers().stream().flatMap(container -> target.entriesFor(container.id()).stream()).anyMatch(entry -> "Sol Ring".equals(entry.oracleName())), "imported resolved card");
        System.out.println("Sync bundle smoke test passed: " + bundle);
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
