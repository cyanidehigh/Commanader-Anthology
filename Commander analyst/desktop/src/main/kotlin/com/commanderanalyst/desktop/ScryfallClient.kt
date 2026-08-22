package com.commanderanalyst.desktop

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable as JavaSerializable
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager
import java.time.Duration
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.Json

class ScryfallClient {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    private val sqliteCache = ScryfallSqliteCardCache(json)
    private val bulkCache = ScryfallBulkCardCache(json)
    private val detailCache = ScryfallCardDetailCache(json)

    fun prepareBulkLookupIndex(progress: (String) -> Unit = {}): Result<Unit> {
        return runCatching {
            sqliteCache.prepare(progress)
        }
    }

    fun lookupCard(name: String, setCode: String? = null): Result<ScryfallCardSelection> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return Result.failure(IllegalArgumentException("Card name is required."))
        val cleanSetCode = setCode?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

        sqliteCache.lookupCard(cleanName, cleanSetCode)?.let { details ->
            detailCache.save(details)
            return Result.success(details.toSelection())
        }

        val card = requestCard(namedUrl("exact", cleanName, cleanSetCode)).getOrElse {
            requestCard(namedUrl("fuzzy", cleanName, cleanSetCode)).getOrElse { error -> return Result.failure(error) }
        }
        val oracleName = fastOracleName(card)
        detailCache.save(card.toDetails(oracleName))

        return Result.success(card.toSelection(oracleName))
    }

    fun lookupImportedDeckCard(name: String): Result<ScryfallCardSelection> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return Result.failure(IllegalArgumentException("Card name is required."))

        sqliteCache.lookupCard(cleanName, null)?.let { details ->
            detailCache.save(details)
            return Result.success(details.toSelection())
        }

        val card = requestCard(namedUrl("exact", cleanName, null)).getOrElse {
            requestCard(namedUrl("fuzzy", cleanName, null)).getOrElse { error -> return Result.failure(error) }
        }

        val oracleName = fastOracleName(card)
        detailCache.save(card.toDetails(oracleName))
        return Result.success(card.toSelection(oracleName))
    }

    fun lookupCardOptions(name: String, setCode: String? = null): Result<List<ScryfallCardSelection>> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return Result.failure(IllegalArgumentException("Card name is required."))
        val cleanSetCode = setCode?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

        sqliteCache.lookupPrintingsForName(cleanName, cleanSetCode)?.let { options ->
            return Result.success(options)
        }

        val selectedCard = lookupCard(cleanName, cleanSetCode).getOrElse { error -> return Result.failure(error) }
        if (cleanSetCode == null) return Result.success(listOf(selectedCard))

        sqliteCache.lookupPrintings(selectedCard, cleanSetCode)?.let { options ->
            return Result.success(options)
        }

        val query = selectedCard.oracleId
            ?.let { "oracleid:$it set:$cleanSetCode" }
            ?: "!\"${selectedCard.printingName}\" set:$cleanSetCode"

        val printings = requestPrints(searchUrl(query)).getOrElse {
            return Result.success(listOf(selectedCard))
        }
        val options = printings
            .filter { card ->
                card.set.equals(cleanSetCode, ignoreCase = true) &&
                    (selectedCard.oracleId == null || card.oracleId == selectedCard.oracleId)
            }
            .map { card -> card.toSelection(selectedCard.oracleName) }
            .distinctBy { it.scryfallCardId }
            .sortedWith(compareBy<ScryfallCardSelection> { it.collectorNumber.collectorSortKey() }.thenBy { it.printingName })

        return Result.success(options.ifEmpty { listOf(selectedCard) })
    }

    fun lookupCardById(scryfallCardId: String): Result<ScryfallCardSelection> {
        return cardDetails(scryfallCardId).map { details -> details.toSelection() }
    }

    fun cardDetails(scryfallCardId: String): Result<ScryfallCardDetails> {
        val cleanId = scryfallCardId.trim()
        if (cleanId.isEmpty()) return Result.failure(IllegalArgumentException("Scryfall card ID is required."))

        detailCache.load(cleanId)?.let { details ->
            return Result.success(details)
        }

        sqliteCache.cardDetails(cleanId)?.let { details ->
            detailCache.save(details)
            return Result.success(details)
        }

        bulkCache.cardDetailsIfLoaded(cleanId)?.let { details ->
            detailCache.save(details)
            return Result.success(details)
        }

        return requestCard("https://api.scryfall.com/cards/${urlEncode(cleanId)}").map { card ->
            card.toDetails(fastOracleName(card)).also { details -> detailCache.save(details) }
        }
    }

    private fun fastOracleName(card: ScryfallCardResponse): String {
        return card.oracleName ?: card.name
    }

    private fun requestCard(url: String): Result<ScryfallCardResponse> {
        return runCatching {
            val body = request(url)
            json.decodeFromString<ScryfallCardResponse>(body)
        }
    }

    private fun requestPrints(url: String): Result<List<ScryfallCardResponse>> {
        return runCatching {
            val body = request(url)
            json.decodeFromString<ScryfallListResponse>(body).data
        }
    }

    private fun request(url: String): String {
        repeat(4) { attempt ->
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("User-Agent", "CommanderAnalyst/0.1")
                .GET()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() in 200..299) return response.body()
            if (response.statusCode() == 429 || response.statusCode() == 503) {
                val retryAfterMillis = response.headers()
                    .firstValue("Retry-After")
                    .map { seconds -> seconds.toLongOrNull()?.times(1000) ?: 1000L }
                    .orElse(1000L + (attempt * 500L))
                Thread.sleep(retryAfterMillis)
            } else {
                throw IllegalStateException("Scryfall returned ${response.statusCode()}.")
            }
        }
        throw IllegalStateException("Scryfall rate limit is still active. Try again in a moment.")
    }

    private fun namedUrl(mode: String, name: String, setCode: String?): String {
        val setQuery = setCode?.let { "&set=${urlEncode(it)}" }.orEmpty()
        return "https://api.scryfall.com/cards/named?$mode=${urlEncode(name)}$setQuery"
    }

    private fun searchUrl(query: String): String {
        return "https://api.scryfall.com/cards/search?unique=prints&order=set&q=${urlEncode(query)}"
    }

    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
    }
}

private class ScryfallSqliteCardCache(
    private val json: Json,
    private val cacheDirectory: Path = ScryfallBulkDataClient().cachePath()
) {
    private val defaultCardsPath = cacheDirectory.resolve("default_cards.json")
    private val databasePath = cacheDirectory.resolve("scryfall-cards.sqlite")

    @OptIn(ExperimentalSerializationApi::class)
    fun prepare(progress: (String) -> Unit = {}) {
        if (!defaultCardsPath.exists()) {
            throw IllegalStateException("Install Scryfall default cards before building the SQLite cache.")
        }
        if (isReady()) {
            progress("SQLite card cache is already current.")
            return
        }

        Files.createDirectories(cacheDirectory)
        val tempDatabasePath = databasePath.resolveSibling("${databasePath.fileName}.tmp")
        Files.deleteIfExists(tempDatabasePath)

        connection(tempDatabasePath).use { connection ->
            configureImportConnection(connection)
            connection.autoCommit = false
            progress("Creating SQLite schema...")
            createSchema(connection)
            progress("Reading Scryfall default cards...")
            val cards = Files.newInputStream(defaultCardsPath).use { input ->
                json.decodeFromStream<List<ScryfallCardResponse>>(input)
            }
            progress("Preparing ${cards.size} card rows...")
            val canonicalNames = cards
                .filter { it.oracleId != null }
                .groupBy { it.oracleId!! }
                .mapValues { (_, printings) -> printings.minByOrNull { it.releasedAt.orEmpty() }?.name ?: printings.first().name }

            connection.prepareStatement(
                """
                insert into cards(
                    id, oracle_id, name, oracle_name, normalized_name, normalized_oracle_name,
                    set_code, collector_number, collector_sort, released_at, commander_legal, details_json
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
            ).use { statement ->
                cards.forEachIndexed { index, card ->
                    val oracleName = card.oracleId?.let { canonicalNames[it] } ?: card.name
                    val details = card.toDetails(oracleName)
                    statement.setString(1, card.id)
                    statement.setString(2, card.oracleId)
                    statement.setString(3, card.name)
                    statement.setString(4, oracleName)
                    statement.setString(5, card.name.normalizedCardName())
                    statement.setString(6, oracleName.normalizedCardName())
                    statement.setString(7, card.set.lowercase())
                    statement.setString(8, card.collectorNumber)
                    statement.setString(9, card.collectorNumber.collectorSortKey())
                    statement.setString(10, card.releasedAt.orEmpty())
                    statement.setInt(11, if (card.isCommanderLegal()) 1 else 0)
                    statement.setString(12, json.encodeToString(details))
                    statement.addBatch()
                    if (index % 1000 == 0) {
                        statement.executeBatch()
                        if (index % 10000 == 0) progress("Indexed $index / ${cards.size} cards...")
                    }
                }
                statement.executeBatch()
            }

            progress("Writing cache metadata...")
            writeMetadata(connection)
            connection.commit()
        }

        Files.move(tempDatabasePath, databasePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        progress("SQLite card cache is ready.")
    }

    fun lookupCard(name: String, setCode: String?): ScryfallCardDetails? {
        if (!isReady()) return null
        val normalizedName = name.normalizedCardName()
        val cleanSetCode = setCode?.lowercase()
        val sql = buildString {
            append(
                """
                select details_json from cards
                where (normalized_name = ? or normalized_oracle_name = ?)
                """.trimIndent()
            )
            if (cleanSetCode != null) append(" and set_code = ?")
            append(" order by commander_legal desc, released_at asc, collector_sort asc limit 1")
        }
        return connection(databasePath).use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, normalizedName)
                statement.setString(2, normalizedName)
                if (cleanSetCode != null) statement.setString(3, cleanSetCode)
                statement.executeQuery().use { results ->
                    if (results.next()) decodeDetails(results.getString("details_json")) else null
                }
            }
        }
    }

    fun lookupPrintings(selection: ScryfallCardSelection, setCode: String): List<ScryfallCardSelection>? {
        if (!isReady()) return null
        val sql = if (selection.oracleId != null) {
            """
            select details_json from cards
            where oracle_id = ? and set_code = ?
            order by collector_sort asc, name asc
            """.trimIndent()
        } else {
            """
            select details_json from cards
            where normalized_oracle_name = ? and set_code = ?
            order by collector_sort asc, name asc
            """.trimIndent()
        }
        val key = selection.oracleId ?: selection.oracleName.normalizedCardName()
        val options = connection(databasePath).use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, key)
                statement.setString(2, setCode.lowercase())
                statement.executeQuery().use { results ->
                    val details = mutableListOf<ScryfallCardDetails>()
                    while (results.next()) details += decodeDetails(results.getString("details_json"))
                    details
                }
            }
        }.map { it.toSelection() }.distinctBy { it.scryfallCardId }

        return options.takeIf { it.isNotEmpty() }
    }

    fun lookupPrintingsForName(name: String, setCode: String?): List<ScryfallCardSelection>? {
        if (!isReady()) return null
        val normalizedName = name.normalizedCardName()
        val cleanSetCode = setCode?.lowercase()
        val sql = buildString {
            append(
                """
                select details_json from cards
                where (normalized_name = ? or normalized_oracle_name = ?)
                """.trimIndent()
            )
            if (cleanSetCode != null) append(" and set_code = ?")
            append(" order by released_at asc, set_code asc, collector_sort asc")
        }

        val options = connection(databasePath).use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, normalizedName)
                statement.setString(2, normalizedName)
                if (cleanSetCode != null) statement.setString(3, cleanSetCode)
                statement.executeQuery().use { results ->
                    val details = mutableListOf<ScryfallCardDetails>()
                    while (results.next()) details += decodeDetails(results.getString("details_json"))
                    details
                }
            }
        }.map { it.toSelection() }.distinctBy { it.scryfallCardId }

        return options.takeIf { it.isNotEmpty() }
    }

    fun cardDetails(scryfallCardId: String): ScryfallCardDetails? {
        if (!isReady()) return null
        return connection(databasePath).use { connection ->
            connection.prepareStatement("select details_json from cards where id = ?").use { statement ->
                statement.setString(1, scryfallCardId)
                statement.executeQuery().use { results ->
                    if (results.next()) decodeDetails(results.getString("details_json")) else null
                }
            }
        }
    }

    private fun isReady(): Boolean {
        if (!databasePath.exists() || !defaultCardsPath.exists()) return false
        return runCatching {
            connection(databasePath).use { connection ->
                metadata(connection, "source_last_modified") == Files.getLastModifiedTime(defaultCardsPath).toMillis().toString() &&
                    metadata(connection, "source_size") == Files.size(defaultCardsPath).toString()
            }
        }.getOrDefault(false)
    }

    private fun configureImportConnection(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.executeUpdate("pragma journal_mode = off")
            statement.executeUpdate("pragma synchronous = off")
        }
    }

    private fun createSchema(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.executeUpdate("create table metadata(key text primary key, value text not null)")
            statement.executeUpdate(
                """
                create table cards(
                    id text primary key,
                    oracle_id text,
                    name text not null,
                    oracle_name text not null,
                    normalized_name text not null,
                    normalized_oracle_name text not null,
                    set_code text not null,
                    collector_number text not null,
                    collector_sort text not null,
                    released_at text not null,
                    commander_legal integer not null,
                    details_json text not null
                )
                """.trimIndent()
            )
            statement.executeUpdate("create index idx_cards_normalized_name on cards(normalized_name)")
            statement.executeUpdate("create index idx_cards_normalized_oracle_name on cards(normalized_oracle_name)")
            statement.executeUpdate("create index idx_cards_oracle_id_set on cards(oracle_id, set_code)")
            statement.executeUpdate("create index idx_cards_set on cards(set_code)")
        }
    }

    private fun writeMetadata(connection: Connection) {
        connection.prepareStatement("insert into metadata(key, value) values (?, ?)").use { statement ->
            listOf(
                "source_last_modified" to Files.getLastModifiedTime(defaultCardsPath).toMillis().toString(),
                "source_size" to Files.size(defaultCardsPath).toString()
            ).forEach { (key, value) ->
                statement.setString(1, key)
                statement.setString(2, value)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    private fun metadata(connection: Connection, key: String): String? {
        return connection.prepareStatement("select value from metadata where key = ?").use { statement ->
            statement.setString(1, key)
            statement.executeQuery().use { results ->
                if (results.next()) results.getString("value") else null
            }
        }
    }

    private fun decodeDetails(value: String): ScryfallCardDetails {
        return json.decodeFromString<ScryfallCardDetails>(value)
    }

    private fun connection(path: Path): Connection {
        return DriverManager.getConnection("jdbc:sqlite:${path.toAbsolutePath()}")
    }
}

private class ScryfallCardDetailCache(
    private val json: Json,
    cacheDirectory: Path = ScryfallBulkDataClient().cachePath().resolve("card-details-cache")
) {
    private val cacheDirectory = cacheDirectory

    fun load(scryfallCardId: String): ScryfallCardDetails? {
        val path = cachePath(scryfallCardId)
        if (!path.exists()) return null
        return runCatching {
            json.decodeFromString<ScryfallCardDetails>(path.readText())
        }.getOrNull()
    }

    fun save(details: ScryfallCardDetails) {
        runCatching {
            Files.createDirectories(cacheDirectory)
            cachePath(details.scryfallCardId).writeText(json.encodeToString(details))
        }
    }

    private fun cachePath(scryfallCardId: String): Path {
        val cleanId = scryfallCardId.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        return cacheDirectory.resolve("$cleanId.json")
    }
}

private class ScryfallBulkCardCache(
    private val json: Json,
    private val defaultCardsPath: Path = ScryfallBulkDataClient().cachePath().resolve("default_cards.json")
) {
    private val indexPath = defaultCardsPath.resolveSibling("default_cards.lookup-index.bin")
    @Volatile private var index: BulkCardIndex? = null

    fun prepare() {
        loadIndexOrNull() ?: throw IllegalStateException("Install Scryfall default cards before building the lookup index.")
    }

    fun lookupCard(name: String, setCode: String?): ScryfallCardResponse? {
        val loaded = loadIndexOrNull() ?: return null
        val normalizedName = name.normalizedCardName()
        val set = setCode?.lowercase()

        val candidates = loaded.byName[normalizedName].orEmpty()
            .ifEmpty { loaded.byOracleName[normalizedName].orEmpty() }
        if (candidates.isEmpty()) return null

        val setCandidates = set?.let { cleanSet -> candidates.filter { it.set.equals(cleanSet, ignoreCase = true) } }
            ?.takeIf { it.isNotEmpty() }
        val usableCandidates = setCandidates ?: candidates

        return usableCandidates.preferredCard()
    }

    fun lookupPrintings(selection: ScryfallCardSelection, setCode: String): List<ScryfallCardSelection>? {
        val loaded = loadIndexOrNull() ?: return null
        val cards = selection.oracleId
            ?.let { loaded.byOracleId[it].orEmpty() }
            ?: loaded.byOracleName[selection.oracleName.normalizedCardName()].orEmpty()

        val options = cards
            .filter { it.set.equals(setCode, ignoreCase = true) }
            .map { it.toSelection(canonicalOracleName(it)) }
            .distinctBy { it.scryfallCardId }
            .sortedWith(compareBy<ScryfallCardSelection> { it.collectorNumber.collectorSortKey() }.thenBy { it.printingName })

        return options.takeIf { it.isNotEmpty() }
    }

    fun cardDetails(scryfallCardId: String): ScryfallCardDetails? {
        val loaded = loadIndexOrNull() ?: return null
        val card = loaded.byId[scryfallCardId] ?: return null
        return card.toDetails(canonicalOracleName(card))
    }

    fun cardDetailsIfLoaded(scryfallCardId: String): ScryfallCardDetails? {
        val loaded = index ?: return null
        val card = loaded.byId[scryfallCardId] ?: return null
        return card.toDetails(canonicalOracleName(card))
    }

    fun canonicalOracleName(card: ScryfallCardResponse): String {
        val oracleId = card.oracleId ?: return card.name
        return loadIndexOrNull()
            ?.byOracleId
            ?.get(oracleId)
            ?.minByOrNull { it.releasedAt.orEmpty() }
            ?.name
            ?: card.name
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadIndexOrNull(): BulkCardIndex? {
        index?.let { return it }
        if (!defaultCardsPath.exists()) return null

        synchronized(this) {
            index?.let { return it }
            return runCatching {
                readIndexSnapshot()?.let { snapshot ->
                    if (snapshot.matches(defaultCardsPath)) {
                        index = snapshot.index
                        return snapshot.index
                    }
                }
                Files.newInputStream(defaultCardsPath).use { input ->
                    val cards = json.decodeFromStream<List<ScryfallCardResponse>>(input)
                    BulkCardIndex(cards).also { builtIndex ->
                        index = builtIndex
                        writeIndexSnapshot(builtIndex)
                    }
                }
            }.getOrNull()
        }
    }

    private fun readIndexSnapshot(): BulkCardIndexSnapshot? {
        if (!indexPath.exists()) return null
        return runCatching {
            ObjectInputStream(Files.newInputStream(indexPath)).use { input ->
                input.readObject() as? BulkCardIndexSnapshot
            }
        }.getOrNull()
    }

    private fun writeIndexSnapshot(index: BulkCardIndex) {
        runCatching {
            val tempPath = indexPath.resolveSibling("${indexPath.fileName}.tmp")
            ObjectOutputStream(Files.newOutputStream(tempPath)).use { output ->
                output.writeObject(BulkCardIndexSnapshot.from(defaultCardsPath, index))
            }
            Files.move(tempPath, indexPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        }
    }
}

private class BulkCardIndex(cards: List<ScryfallCardResponse>) : JavaSerializable {
    val byId: Map<String, ScryfallCardResponse> = cards.associateBy { it.id }
    val byName: Map<String, List<ScryfallCardResponse>> = cards.groupBy { it.name.normalizedCardName() }
    val byOracleName: Map<String, List<ScryfallCardResponse>> = cards.groupBy { (it.oracleName ?: it.name).normalizedCardName() }
    val byOracleId: Map<String, List<ScryfallCardResponse>> = cards
        .mapNotNull { card -> card.oracleId?.let { it to card } }
        .groupBy({ it.first }, { it.second })
}

private data class BulkCardIndexSnapshot(
    val sourceLastModified: Long,
    val sourceSize: Long,
    val index: BulkCardIndex
) : JavaSerializable {
    fun matches(sourcePath: Path): Boolean {
        return sourcePath.exists() &&
            sourceLastModified == Files.getLastModifiedTime(sourcePath).toMillis() &&
            sourceSize == Files.size(sourcePath)
    }

    companion object {
        fun from(sourcePath: Path, index: BulkCardIndex): BulkCardIndexSnapshot {
            return BulkCardIndexSnapshot(
                sourceLastModified = Files.getLastModifiedTime(sourcePath).toMillis(),
                sourceSize = Files.size(sourcePath),
                index = index
            )
        }
    }
}

private fun List<ScryfallCardResponse>.preferredCard(): ScryfallCardResponse? {
    return sortedWith(
        compareByDescending<ScryfallCardResponse> { it.isCommanderLegal() }
            .thenBy { it.releasedAt.orEmpty() }
            .thenBy { it.collectorNumber.collectorSortKey() }
    ).firstOrNull()
}

private fun ScryfallCardResponse.isCommanderLegal(): Boolean {
    return legalities["commander"] == "legal"
}

private fun String.normalizedCardName(): String {
    return trim().lowercase().replace(Regex("\\s+"), " ")
}

private fun String.collectorSortKey(): String {
    return padStart(12, '0')
}

data class ScryfallCardSelection(
    val scryfallCardId: String,
    val oracleId: String?,
    val oracleName: String,
    val printingName: String,
    val setCode: String,
    val collectorNumber: String,
    val typeLine: String? = null
)

private fun ScryfallCardDetails.toSelection(): ScryfallCardSelection {
    return ScryfallCardSelection(
        scryfallCardId = scryfallCardId,
        oracleId = oracleId,
        oracleName = oracleName,
        printingName = name,
        setCode = setCode,
        collectorNumber = collectorNumber,
        typeLine = typeLine
    )
}

@Serializable
data class ScryfallCardDetails(
    val scryfallCardId: String,
    val oracleId: String?,
    val name: String,
    val oracleName: String,
    val manaCost: String?,
    val typeLine: String?,
    val oracleText: String?,
    val power: String?,
    val toughness: String?,
    val setName: String?,
    val setCode: String,
    val collectorNumber: String,
    val rarity: String?,
    val imageUrl: String?,
    val legalities: Map<String, String>,
    val prices: Map<String, String?>
)

@Serializable
private data class ScryfallListResponse(
    val data: List<ScryfallCardResponse>
)

@Serializable
private data class ScryfallCardResponse(
    val id: String,
    @SerialName("oracle_id") val oracleId: String? = null,
    val name: String,
    @SerialName("oracle_name") val oracleName: String? = null,
    @SerialName("mana_cost") val manaCost: String? = null,
    @SerialName("type_line") val typeLine: String? = null,
    @SerialName("oracle_text") val oracleText: String? = null,
    val power: String? = null,
    val toughness: String? = null,
    val set: String,
    @SerialName("set_name") val setName: String? = null,
    @SerialName("collector_number") val collectorNumber: String,
    val rarity: String? = null,
    @SerialName("image_uris") val imageUris: ScryfallImageUris? = null,
    val legalities: Map<String, String> = emptyMap(),
    val prices: Map<String, String?> = emptyMap(),
    @SerialName("prints_search_uri") val printsSearchUri: String? = null,
    @SerialName("released_at") val releasedAt: String? = null
) : JavaSerializable {
    fun toSelection(oracleName: String): ScryfallCardSelection {
        return ScryfallCardSelection(
            scryfallCardId = id,
            oracleId = oracleId,
            oracleName = oracleName,
            printingName = name,
            setCode = set.uppercase(),
            collectorNumber = collectorNumber,
            typeLine = typeLine
        )
    }

    fun toDetails(oracleName: String): ScryfallCardDetails {
        return ScryfallCardDetails(
            scryfallCardId = id,
            oracleId = oracleId,
            name = name,
            oracleName = oracleName,
            manaCost = manaCost,
            typeLine = typeLine,
            oracleText = oracleText,
            power = power,
            toughness = toughness,
            setName = setName,
            setCode = set.uppercase(),
            collectorNumber = collectorNumber,
            rarity = rarity,
            imageUrl = imageUris?.normal ?: imageUris?.large,
            legalities = legalities,
            prices = prices
        )
    }
}

@Serializable
private data class ScryfallImageUris(
    val small: String? = null,
    val normal: String? = null,
    val large: String? = null,
    @SerialName("png") val png: String? = null
) : JavaSerializable
