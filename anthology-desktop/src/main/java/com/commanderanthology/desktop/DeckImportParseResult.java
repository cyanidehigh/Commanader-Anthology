package com.commanderanthology.desktop;

import java.util.List;

record DeckImportParseResult(List<ImportedDeckRow> rows, List<String> warnings) {
    DeckImportParseResult {
        rows = List.copyOf(rows);
        warnings = List.copyOf(warnings);
    }
}
