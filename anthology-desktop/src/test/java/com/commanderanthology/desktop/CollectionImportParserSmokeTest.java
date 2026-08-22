package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.ContainerType;
import com.commanderanthology.core.collection.InventoryEntry;
import com.commanderanthology.core.deck.CardIdentityStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CollectionImportParserSmokeTest {
    private CollectionImportParserSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        CollectionImportParseResult pasted = CollectionImportParser.parse("""
                Cards
                2 Sol Ring (LEA) #269 *F*
                1 Lantern of Insight (5DN) #135
                3x Basalt Monolith
                """);
        require(pasted.rows().size() == 3, "pasted row count");
        require(pasted.rows().get(0).foil(), "foil marker");
        require("LEA".equals(pasted.rows().get(0).setCode()), "set suffix");
        require("269".equals(pasted.rows().get(0).collectorNumber()), "collector suffix");

        CollectionImportParseResult csv = CollectionImportParser.parse("""
                Card Name,Quantity,Set Code,Collector Number,Finish,Scryfall ID
                Sol Ring,1,LEA,269,Foil,c4300d24-1cae-4dd5-be7e-38cc677cf5bd
                "Rafiq of the Many",1,ALA,185,,
                """);
        require(csv.rows().size() == 2, "csv row count");
        require(csv.rows().get(0).scryfallCardId() != null, "csv scryfall id");

        Path tempDir = Files.createTempDirectory("anthology-collection-import-test");
        DesktopAppState state = new DesktopAppState(new DesktopPersistence(tempDir.resolve("state.json")));
        CardContainer binder = state.createContainer("Import Test Binder", ContainerType.BINDER);
        state.addImportedInventoryEntries(binder.id(), csv.rows());
        List<InventoryEntry> entries = state.entriesFor(binder.id());
        require(entries.size() == 2, "state imported entries");
        require(entries.stream().allMatch(entry -> entry.identityStatus() == CardIdentityStatus.RESOLVED), "state resolved entries");

        System.out.println("Collection import parser smoke test passed.");
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
