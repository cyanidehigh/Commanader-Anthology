package com.commanderanalyst.domain.model

data class Card(
    val oracleId: String,
    val name: String,
    val colorIdentity: Set<ManaColor>,
    val commanderLegal: Boolean
)

data class CardPrinting(
    val scryfallId: String,
    val oracleId: String,
    val setCode: String,
    val collectorNumber: String,
    val imageUrl: String?
)

enum class ManaColor {
    White,
    Blue,
    Black,
    Red,
    Green
}
