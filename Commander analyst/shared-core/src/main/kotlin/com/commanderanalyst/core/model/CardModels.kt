package com.commanderanalyst.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val oracleId: String,
    val name: String,
    val manaValue: Double,
    val colorIdentity: Set<ManaColor>,
    val typeLine: String,
    val oracleText: String
)

@Serializable
data class CardPrinting(
    val scryfallId: String,
    val oracleId: String,
    val setCode: String,
    val collectorNumber: String,
    val imageUrl: String?
)

@Serializable
enum class ManaColor {
    White,
    Blue,
    Black,
    Red,
    Green
}
