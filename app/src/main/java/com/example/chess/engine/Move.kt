package com.example.chess.engine

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
    val isCastling: Boolean = false,
    val isEnPassant: Boolean = false,
    val capturedPiece: Piece? = null,
    var san: String = "" // Standard algebraic notation like "Nf3", "exd5", "O-O"
) {
    val uci: String
        get() {
            val promo = promotion?.notation?.lowercaseChar()?.toString() ?: ""
            return "${from.toAlgebraic()}${to.toAlgebraic()}$promo"
        }

    fun isSameMove(other: Move): Boolean {
        return from == other.from && to == other.to && promotion == other.promotion
    }
}
