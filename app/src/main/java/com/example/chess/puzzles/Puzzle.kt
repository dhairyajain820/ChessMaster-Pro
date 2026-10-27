package com.example.chess.puzzles

import com.example.chess.engine.PieceColor

enum class PuzzleDifficulty(val displayName: String, val ratingRange: String) {
    EASY("Easy", "600 - 1200"),
    MEDIUM("Medium", "1200 - 1800"),
    HARD("Hard", "1800 - 2400+")
}

data class ChessPuzzle(
    val id: String,
    val title: String,
    val difficulty: PuzzleDifficulty,
    val theme: String,
    val fen: String,
    val playerColor: PieceColor,
    val solutionMovesUci: List<String>,
    val solutionMovesSan: List<String>,
    val winningStrategy: String,
    val keyMovesExplanation: String,
    val hint: String
)
