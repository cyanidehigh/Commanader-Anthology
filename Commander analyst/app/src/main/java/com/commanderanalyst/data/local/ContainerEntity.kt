package com.commanderanalyst.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.commanderanalyst.domain.model.Container
import com.commanderanalyst.domain.model.ContainerType

@Entity(tableName = "containers")
data class ContainerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val createdAtMillis: Long
)

fun ContainerEntity.toDomain(): Container {
    return Container(
        id = id,
        name = name,
        type = runCatching { ContainerType.valueOf(type) }.getOrDefault(ContainerType.Other)
    )
}

fun Container.toEntity(createdAtMillis: Long): ContainerEntity {
    return ContainerEntity(
        id = id,
        name = name,
        type = type.name,
        createdAtMillis = createdAtMillis
    )
}
