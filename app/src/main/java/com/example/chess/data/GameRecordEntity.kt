package com.example.chess.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val opponentName: String,
    val engineLevel: String,
    val playerColor: String,
    val gameResult: String, // "WIN", "LOSS", "DRAW"
    val movesSan: String, // space separated
    val movesCount: Int,
    val playerAccuracy: Int,
    val primaryWeakness: String,
    val pgn: String
)
