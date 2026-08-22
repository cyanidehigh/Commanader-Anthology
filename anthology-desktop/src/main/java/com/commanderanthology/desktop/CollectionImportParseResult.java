package com.commanderanthology.desktop;

import java.util.List;

record CollectionImportParseResult(List<ImportedCollectionRow> rows, List<String> warnings) {
}
