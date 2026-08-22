package com.commanderanalyst.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.commanderanalyst.domain.model.DeckSection
import com.commanderanalyst.domain.model.DeckSlot

@Entity(
    tableName = "deck_slots",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("deckId")
    ]
)
data class DeckSlotEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val cardName: String,
    val desiredQuantity: Int,
    val section: String,
    val createdAtMillis: Long
)

fun DeckSlotEntity.toDomain(): DeckSlot {
    return DeckSlot(
        id = id,
        deckId = deckId,
        cardName = cardName,
        desiredQuantity = desiredQuantity,
        section = runCatching { DeckSection.valueOf(section) }.getOrDefault(DeckSection.Other)
    )
}

