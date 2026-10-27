package com.example.chess.engine

class Board {
    private val squares: Array<Array<Piece?>> = Array(8) { Array(8) { null } }

    var activeColor: PieceColor = PieceColor.WHITE
    var whiteCanCastleKingSide: Boolean = true
    var whiteCanCastleQueenSide: Boolean = true
    var blackCanCastleKingSide: Boolean = true
    var blackCanCastleQueenSide: Boolean = true
    var enPassantTarget: Square? = null
    var halfmoveClock: Int = 0
    var fullmoveNumber: Int = 1

    init {
        setupInitialPosition()
    }

    fun getPiece(square: Square): Piece? {
        if (!square.isValid) return null
        return squares[square.row][square.col]
    }

    fun getPiece(row: Int, col: Int): Piece? {
        if (row !in 0..7 || col !in 0..7) return null
        return squares[row][col]
    }

    fun setPiece(square: Square, piece: Piece?) {
        if (square.isValid) {
            squares[square.row][square.col] = piece
        }
    }

    fun clear() {
        for (r in 0..7) {
            for (c in 0..7) {
                squares[r][c] = null
            }
        }
        whiteCanCastleKingSide = false
        whiteCanCastleQueenSide = false
        blackCanCastleKingSide = false
        blackCanCastleQueenSide = false
        enPassantTarget = null
        halfmoveClock = 0
        fullmoveNumber = 1
        activeColor = PieceColor.WHITE
    }

    fun setupInitialPosition() {
        clear()
        // Black major pieces (Row 0, Rank 8)
        squares[0][0] = Piece(PieceType.ROOK, PieceColor.BLACK)
        squares[0][1] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
        squares[0][2] = Piece(PieceType.BISHOP, PieceColor.BLACK)
        squares[0][3] = Piece(PieceType.QUEEN, PieceColor.BLACK)
        squares[0][4] = Piece(PieceType.KING, PieceColor.BLACK)
        squares[0][5] = Piece(PieceType.BISHOP, PieceColor.BLACK)
        squares[0][6] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
        squares[0][7] = Piece(PieceType.ROOK, PieceColor.BLACK)

        // Black pawns (Row 1, Rank 7)
        for (c in 0..7) {
            squares[1][c] = Piece(PieceType.PAWN, PieceColor.BLACK)
        }

        // White pawns (Row 6, Rank 2)
        for (c in 0..7) {
            squares[6][c] = Piece(PieceType.PAWN, PieceColor.WHITE)
        }

        // White major pieces (Row 7, Rank 1)
        squares[7][0] = Piece(PieceType.ROOK, PieceColor.WHITE)
        squares[7][1] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
        squares[7][2] = Piece(PieceType.BISHOP, PieceColor.WHITE)
        squares[7][3] = Piece(PieceType.QUEEN, PieceColor.WHITE)
        squares[7][4] = Piece(PieceType.KING, PieceColor.WHITE)
        squares[7][5] = Piece(PieceType.BISHOP, PieceColor.WHITE)
        squares[7][6] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
        squares[7][7] = Piece(PieceType.ROOK, PieceColor.WHITE)

        whiteCanCastleKingSide = true
        whiteCanCastleQueenSide = true
        blackCanCastleKingSide = true
        blackCanCastleQueenSide = true
        enPassantTarget = null
        halfmoveClock = 0
        fullmoveNumber = 1
        activeColor = PieceColor.WHITE
    }

    fun clone(): Board {
        val copy = Board()
        for (r in 0..7) {
            for (c in 0..7) {
                copy.squares[r][c] = this.squares[r][c]
            }
        }
        copy.activeColor = this.activeColor
        copy.whiteCanCastleKingSide = this.whiteCanCastleKingSide
        copy.whiteCanCastleQueenSide = this.whiteCanCastleQueenSide
        copy.blackCanCastleKingSide = this.blackCanCastleKingSide
        copy.blackCanCastleQueenSide = this.blackCanCastleQueenSide
        copy.enPassantTarget = this.enPassantTarget
        copy.halfmoveClock = this.halfmoveClock
        copy.fullmoveNumber = this.fullmoveNumber
        return copy
    }

    fun findKing(color: PieceColor): Square? {
        for (r in 0..7) {
            for (c in 0..7) {
                val p = squares[r][c]
                if (p != null && p.type == PieceType.KING && p.color == color) {
                    return Square(r, c)
                }
            }
        }
        return null
    }

    fun toFen(): String {
        val fen = StringBuilder()
        for (r in 0..7) {
            var emptyCount = 0
            for (c in 0..7) {
                val piece = squares[r][c]
                if (piece == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount)
                        emptyCount = 0
                    }
                    fen.append(piece.fenChar)
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount)
            }
            if (r < 7) {
                fen.append('/')
            }
        }

        fen.append(' ')
        fen.append(if (activeColor == PieceColor.WHITE) 'w' else 'b')

        fen.append(' ')
        val castling = StringBuilder()
        if (whiteCanCastleKingSide) castling.append('K')
        if (whiteCanCastleQueenSide) castling.append('Q')
        if (blackCanCastleKingSide) castling.append('k')
        if (blackCanCastleQueenSide) castling.append('q')
        if (castling.isEmpty()) castling.append('-')
        fen.append(castling)

        fen.append(' ')
        fen.append(enPassantTarget?.toAlgebraic() ?: "-")

        fen.append(' ')
        fen.append(halfmoveClock)

        fen.append(' ')
        fen.append(fullmoveNumber)

        return fen.toString()
    }

    fun loadFen(fen: String): Boolean {
        val parts = fen.trim().split("\\s+".toRegex())
        if (parts.isEmpty()) return false

        clear()
        val rows = parts[0].split('/')
        if (rows.size != 8) return false

        for (r in 0..7) {
            val rowStr = rows[r]
            var c = 0
            for (ch in rowStr) {
                if (ch.isDigit()) {
                    c += ch - '0'
                } else {
                    val piece = Piece.fromFenChar(ch) ?: return false
                    if (c < 8) {
                        squares[r][c] = piece
                        c++
                    }
                }
            }
        }

        if (parts.size > 1) {
            activeColor = if (parts[1] == "b") PieceColor.BLACK else PieceColor.WHITE
        }
        if (parts.size > 2) {
            val castling = parts[2]
            whiteCanCastleKingSide = castling.contains('K')
            whiteCanCastleQueenSide = castling.contains('Q')
            blackCanCastleKingSide = castling.contains('k')
            blackCanCastleQueenSide = castling.contains('q')
        }
        if (parts.size > 3) {
            enPassantTarget = if (parts[3] != "-") Square.fromAlgebraic(parts[3]) else null
        }
        if (parts.size > 4) {
            halfmoveClock = parts[4].toIntOrNull() ?: 0
        }
        if (parts.size > 5) {
            fullmoveNumber = parts[5].toIntOrNull() ?: 1
        }
        return true
    }
}
