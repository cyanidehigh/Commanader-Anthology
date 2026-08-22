package com.commanderanalyst.desktop

import com.commanderanalyst.core.model.SyncBundle
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.serialization.json.Json

class DesktopPersistence(
    private val dataFile: Path = defaultDataFile()
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun load(): SyncBundle? {
        if (!dataFile.exists()) return null
        return runCatching {
            json.decodeFromString<SyncBundle>(dataFile.readText())
        }.getOrNull()
    }

    fun save(bundle: SyncBundle) {
        dataFile.parent.createDirectories()
        val tempFile = dataFile.resolveSibling("${dataFile.fileName}.tmp")
        tempFile.writeText(json.encodeToString(bundle))
        Files.move(
            tempFile,
            dataFile,
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE
        )
    }
}

private fun defaultDataFile(): Path {
    val appData = System.getenv("APPDATA")
        ?.takeIf { it.isNotBlank() }
        ?.let { Path.of(it) }
        ?: Path.of(System.getProperty("user.home"), "AppData", "Roaming")

    return appData.resolve("Commander Analyst").resolve("commander-analyst-data.json")
}

