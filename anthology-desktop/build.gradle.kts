plugins {
    application
}

dependencies {
    implementation(project(":anthology-core"))
    implementation(project(":anthology-game-gdx"))
    implementation(libs.sqlite.jdbc)
    runtimeOnly(libs.slf4j.nop)
}

application {
    mainClass.set("com.commanderanthology.desktop.CommanderAnthologyDesktop")
}

tasks.register<JavaExec>("persistenceSmokeTest") {
    group = "verification"
    description = "Runs the desktop persistence save/load smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.DesktopPersistenceSmokeTest")
}

tasks.register<JavaExec>("deckImportParserSmokeTest") {
    group = "verification"
    description = "Runs the desktop deck import parser smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.DeckImportParserSmokeTest")
}

tasks.register<JavaExec>("legacyTestDeckSeederSmokeTest") {
    group = "verification"
    description = "Runs the legacy Commander Sim test deck seeder smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.LegacyTestDeckSeederSmokeTest")
}

tasks.register<JavaExec>("legacyCcBuilderDataImporterSmokeTest") {
    group = "verification"
    description = "Runs the legacy CCBuilder user data importer smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.LegacyCcBuilderDataImporterSmokeTest")
}

tasks.register<JavaExec>("collectionMutationSmokeTest") {
    group = "verification"
    description = "Runs the desktop collection mutation smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.CollectionMutationSmokeTest")
}

tasks.register<JavaExec>("cardLookupValidatorSmokeTest") {
    group = "verification"
    description = "Runs the desktop card lookup validator smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.CardLookupValidatorSmokeTest")
}

tasks.register<JavaExec>("collectionImportParserSmokeTest") {
    group = "verification"
    description = "Runs the desktop collection import parser smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.CollectionImportParserSmokeTest")
}

tasks.register<JavaExec>("cardCodexSearchSmokeTest") {
    group = "verification"
    description = "Runs the desktop Card Codex search smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.CardCodexSearchSmokeTest")
}

tasks.register<JavaExec>("deckAssignmentCopyChoiceSmokeTest") {
    group = "verification"
    description = "Runs the desktop deck assignment copy-choice smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.DeckAssignmentCopyChoiceSmokeTest")
}

tasks.register<JavaExec>("deckImportReviewedIdentitySmokeTest") {
    group = "verification"
    description = "Runs the desktop deck import reviewed identity smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.DeckImportReviewedIdentitySmokeTest")
}

tasks.register<JavaExec>("commanderValidationSmokeTest") {
    group = "verification"
    description = "Runs the desktop commander validation smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.CommanderValidationSmokeTest")
}

tasks.register<JavaExec>("commanderLegalityRulesSmokeTest") {
    group = "verification"
    description = "Runs the desktop commander legality rules smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.CommanderLegalityRulesSmokeTest")
}

tasks.register<JavaExec>("manualCardSelectionSmokeTest") {
    group = "verification"
    description = "Runs the desktop manual card selection smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.ManualCardSelectionSmokeTest")
}

tasks.register<JavaExec>("syncBundleSmokeTest") {
    group = "verification"
    description = "Runs the desktop sync bundle export/import smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.SyncBundleSmokeTest")
}

tasks.register<JavaExec>("scryfallCacheStatusSmokeTest") {
    group = "verification"
    description = "Runs the desktop Scryfall cache status smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.ScryfallCacheStatusSmokeTest")
}

tasks.register<JavaExec>("scryfallBulkDataServiceSmokeTest") {
    group = "verification"
    description = "Runs the desktop Scryfall bulk data service smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.ScryfallBulkDataServiceSmokeTest")
}

tasks.register<JavaExec>("cardImageCacheSmokeTest") {
    group = "verification"
    description = "Runs the desktop card image cache smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.CardImageCacheSmokeTest")
}

tasks.register<JavaExec>("scryfallSqliteBuildSmokeTest") {
    group = "verification"
    description = "Runs the desktop Scryfall SQLite build smoke test."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.ScryfallSqliteBuildSmokeTest")
}

tasks.register<JavaExec>("restoreCcBuilderUserData") {
    group = "migration"
    description = "Backs up the current Anthology desktop state and restores CCBuilder user data."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.commanderanthology.desktop.RestoreCcBuilderUserData")
}
