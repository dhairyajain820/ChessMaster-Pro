package com.example.chess.openings

import com.example.chess.engine.PieceColor

data class OpeningMoveStep(
    val ply: Int,
    val moveSan: String,
    val moveUci: String,
    val isUserMove: Boolean,
    val moveTitle: String,
    val explanation: String
)

data class OpeningLine(
    val id: String,
    val name: String,
    val movesSan: List<String>,
    val movesUci: List<String>,
    val explanations: List<OpeningMoveStep>
)

data class ChessOpening(
    val id: String,
    val name: String,
    val eco: String,
    val playerSide: PieceColor,
    val difficulty: String,
    val shortSummary: String,
    val strategicThemes: List<String>,
    val commonTraps: List<String>,
    val mainLine: OpeningLine,
    val variations: List<OpeningLine> = emptyList()
)
