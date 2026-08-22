package com.commanderanalyst.domain.model

data class ManualInventoryCard(
    val id: String,
    val containerId: String,
    val name: String,
    val quantity: Int
)
