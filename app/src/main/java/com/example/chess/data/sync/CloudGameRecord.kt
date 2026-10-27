package com.example.chess.data.sync

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class CloudGameRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val gameId: String = "",
    val opponentName: String = "",
    val engineLevel: String = "",
    val playerColor: String = "",
    val gameResult: String = "",
    val movesSan: String = "",
    val movesCount: Long = 0L,
    val playerAccuracy: Long = 0L,
    val primaryWeakness: String = "",
    val pgn: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
