package com.example.chess.engine

import java.util.Random

object Zobrist {
    // 64 squares x 12 piece representations (0..5 White, 6..11 Black)
    val pieceKeys = Array(64) { LongArray(12) }
    var sideKey: Long = 0L
    val castleKeys = LongArray(16) // 4 bits of castling rights
    val epKeys = LongArray(8)      // 8 files for en-passant

    init {
        val rng = Random(0xCAFEBABEDEADL)
        for (sq in 0 until 64) {
            for (p in 0 until 12) {
                pieceKeys[sq][p] = rng.nextLong()
            }
        }
        sideKey = rng.nextLong()
        for (i in 0 until 16) {
            castleKeys[i] = rng.nextLong()
        }
        for (i in 0 until 8) {
            epKeys[i] = rng.nextLong()
        }
    }

    private fun pieceToIndex(piece: Piece): Int {
        val base = when (piece.type) {
            PieceType.PAWN -> 0
            PieceType.KNIGHT -> 1
            PieceType.BISHOP -> 2
            PieceType.ROOK -> 3
            PieceType.QUEEN -> 4
            PieceType.KING -> 5
        }
        return if (piece.color == PieceColor.WHITE) base else base + 6
    }

    fun computeHash(board: Board): Long {
        var hash = 0L
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board.getPiece(r, c)
                if (piece != null) {
                    val sq = r * 8 + c
                    val pIdx = pieceToIndex(piece)
                    hash = hash xor pieceKeys[sq][pIdx]
                }
            }
        }

        if (board.activeColor == PieceColor.BLACK) {
            hash = hash xor sideKey
        }

        var castleIndex = 0
        if (board.whiteCanCastleKingSide) castleIndex = castleIndex or 1
        if (board.whiteCanCastleQueenSide) castleIndex = castleIndex or 2
        if (board.blackCanCastleKingSide) castleIndex = castleIndex or 4
        if (board.blackCanCastleQueenSide) castleIndex = castleIndex or 8
        hash = hash xor castleKeys[castleIndex]

        board.enPassantTarget?.let {
            hash = hash xor epKeys[it.col]
        }

        return hash
    }
}
