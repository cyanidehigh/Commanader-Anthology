package com.commanderanalyst.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.commanderanalyst.domain.model.ManualInventoryCard

@Entity(
    tableName = "inventory_entries",
    foreignKeys = [
        ForeignKey(
            entity = ContainerEntity::class,
            parentColumns = ["id"],
            childColumns = ["containerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("containerId")
    ]
)
data class InventoryEntryEntity(
    @PrimaryKey val id: String,
    val containerId: String,
    val cardName: String,
    val quantity: Int,
    val createdAtMillis: Long
)

fun InventoryEntryEntity.toDomain(): ManualInventoryCard {
    return ManualInventoryCard(
        id = id,
        containerId = containerId,
        name = cardName,
        quantity = quantity
    )
}
