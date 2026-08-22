package com.commanderanalyst.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.commanderanalyst.domain.model.Deck

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey val id: String,
    val name: String,
    val commanderName: String?,
    val containerId: String?,
    val createdAtMillis: Long
)

fun DeckEntity.toDomain(): Deck {
    return Deck(
        id = id,
        name = name,
        commanderName = commanderName,
        containerId = containerId
    )
}

