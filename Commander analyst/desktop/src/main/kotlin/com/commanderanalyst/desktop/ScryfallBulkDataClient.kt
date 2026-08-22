package com.commanderanalyst.desktop

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ScryfallBulkDataClient(
    private val cacheDirectory: Path = defaultBulkDataDirectory()
) {
    private val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }
    private val manifestFile = cacheDirectory.resolve("manifest.json")

    fun checkForUpdates(): Result<List<BulkDataStatus>> {
        return runCatching {
            val remote = fetchRemoteBulkData()
            val local = loadManifest().associateBy { it.type }
            remote.map { item ->
                val localItem = local[item.type]
                BulkDataStatus(
                    type = item.type,
                    name = item.name,
                    remoteUpdatedAt = item.updatedAt,
                    localUpdatedAt = localItem?.updatedAt,
                    size = item.size,
                    installed = localItem != null && cacheDirectory.resolve(localItem.fileName).exists(),
                    updateAvailable = localItem?.updatedAt != item.updatedAt,
                    downloadUri = item.downloadUri
                )
            }
        }
    }

    fun installAll(progress: (BulkInstallProgress) -> Unit = {}): Result<List<BulkDataStatus>> {
        return runCatching {
            cacheDirectory.createDirectories()
            val remote = fetchRemoteBulkData()
            val installed = mutableListOf<LocalBulkDataItem>()

            remote.forEachIndexed { index, item ->
                val fileName = "${item.type}.json"
                val target = cacheDirectory.resolve(fileName)
                download(item.downloadUri, target) { bytesRead, totalBytes ->
                    progress(
                        BulkInstallProgress(
                            fileName = item.name,
                            completedFiles = index,
                            totalFiles = remote.size,
                            bytesRead = bytesRead,
                            totalBytes = totalBytes
                        )
                    )
                }
                installed += LocalBulkDataItem(
                    type = item.type,
                    name = item.name,
                    updatedAt = item.updatedAt,
                    fileName = fileName,
                    size = item.size
                )
                progress(
                    BulkInstallProgress(
                        fileName = item.name,
                        completedFiles = index + 1,
                        totalFiles = remote.size,
                        bytesRead = item.size,
                        totalBytes = item.size
                    )
                )
            }

            manifestFile.writeText(json.encodeToString(BulkDataManifest(installed)))
            checkForUpdates().getOrThrow()
        }
    }

    fun cachePath(): Path = cacheDirectory

    private fun fetchRemoteBulkData(): List<RemoteBulkDataItem> {
        val body = requestText("https://api.scryfall.com/bulk-data")
        return json.decodeFromString<RemoteBulkDataResponse>(body).data
            .filter { it.downloadUri.isNotBlank() && it.contentType == "application/json" }
            .sortedBy { it.type }
    }

    private fun loadManifest(): List<LocalBulkDataItem> {
        if (!manifestFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<BulkDataManifest>(manifestFile.readText()).items
        }.getOrElse { emptyList() }
    }

    private fun requestText(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("User-Agent", "CommanderAnalyst/0.1")
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("Scryfall returned ${response.statusCode()}.")
        }
        return response.body()
    }

    private fun download(url: String, target: Path, progress: (Long, Long?) -> Unit) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("User-Agent", "CommanderAnalyst/0.1")
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("Scryfall returned ${response.statusCode()} while downloading.")
        }

        val tempFile = target.resolveSibling("${target.fileName}.tmp")
        val contentLength = response.headers().firstValueAsLong("Content-Length")
        val totalBytes = if (contentLength.isPresent && contentLength.asLong > 0) contentLength.asLong else null
        var bytesRead = 0L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

        response.body().use { input ->
            Files.newOutputStream(
                tempFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            ).use { output ->
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    bytesRead += read
                    progress(bytesRead, totalBytes)
                }
            }
        }
        Files.move(
            tempFile,
            target,
            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE
        )
    }
}

data class BulkInstallProgress(
    val fileName: String,
    val completedFiles: Int,
    val totalFiles: Int,
    val bytesRead: Long,
    val totalBytes: Long?
) {
    val fileFraction: Float?
        get() = totalBytes?.takeIf { it > 0 }?.let { (bytesRead.toDouble() / it).coerceIn(0.0, 1.0).toFloat() }

    val overallFraction: Float
        get() {
            if (totalFiles <= 0) return 0f
            val currentFileFraction = fileFraction ?: 0f
            return ((completedFiles + currentFileFraction) / totalFiles).coerceIn(0f, 1f)
        }

    fun label(): String {
        val currentFile = (completedFiles + 1).coerceAtMost(totalFiles)
        val sizeText = totalBytes?.let { "${formatBulkBytes(bytesRead)} / ${formatBulkBytes(it)}" }
            ?: formatBulkBytes(bytesRead)
        return "Downloading $currentFile / $totalFiles: $fileName ($sizeText)"
    }
}

data class BulkDataStatus(
    val type: String,
    val name: String,
    val remoteUpdatedAt: String,
    val localUpdatedAt: String?,
    val size: Long,
    val installed: Boolean,
    val updateAvailable: Boolean,
    val downloadUri: String
)

@Serializable
private data class RemoteBulkDataResponse(
    val data: List<RemoteBulkDataItem>
)

@Serializable
private data class RemoteBulkDataItem(
    val type: String,
    val name: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("download_uri") val downloadUri: String,
    @SerialName("content_type") val contentType: String = "application/json",
    val size: Long = 0
)

@Serializable
private data class BulkDataManifest(
    val items: List<LocalBulkDataItem>
)

@Serializable
private data class LocalBulkDataItem(
    val type: String,
    val name: String,
    val updatedAt: String,
    val fileName: String,
    val size: Long
)

private fun defaultBulkDataDirectory(): Path {
    val projectDirectory = findProjectDirectory(Path.of(System.getProperty("user.dir")).toAbsolutePath())
    return projectDirectory.resolve("data").resolve("scryfall-bulk-data")
}

private fun findProjectDirectory(start: Path): Path {
    var current: Path? = start
    while (current != null) {
        if (Files.exists(current.resolve("settings.gradle.kts")) || Files.exists(current.resolve("gradlew.bat"))) {
            return current
        }
        current = current.parent
    }
    return start
}

private fun formatBulkBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val mib = bytes / 1024.0 / 1024.0
    return "%.1f MB".format(mib)
}
