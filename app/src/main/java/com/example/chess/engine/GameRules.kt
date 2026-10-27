package com.example.chess.engine

object GameRules {

    fun isSquareAttackedBy(board: Board, target: Square, attackingColor: PieceColor): Boolean {
        // 1. Attacked by pawns
        val pawnPawnRow = if (attackingColor == PieceColor.WHITE) target.row + 1 else target.row - 1
        for (dc in listOf(-1, 1)) {
            val pCol = target.col + dc
            if (pawnPawnRow in 0..7 && pCol in 0..7) {
                val piece = board.getPiece(pawnPawnRow, pCol)
                if (piece != null && piece.color == attackingColor && piece.type == PieceType.PAWN) {
                    return true
                }
            }
        }

        // 2. Attacked by Knights
        val knightOffsets = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1
        )
        for ((dr, dc) in knightOffsets) {
            val r = target.row + dr
            val c = target.col + dc
            if (r in 0..7 && c in 0..7) {
                val piece = board.getPiece(r, c)
                if (piece != null && piece.color == attackingColor && piece.type == PieceType.KNIGHT) {
                    return true
                }
            }
        }

        // 3. Attacked by King
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = target.row + dr
                val c = target.col + dc
                if (r in 0..7 && c in 0..7) {
                    val piece = board.getPiece(r, c)
                    if (piece != null && piece.color == attackingColor && piece.type == PieceType.KING) {
                        return true
                    }
                }
            }
        }

        // 4. Sliding pieces: Orthogonal (Rook, Queen)
        val orthoDirs = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        for ((dr, dc) in orthoDirs) {
            var r = target.row + dr
            var c = target.col + dc
            while (r in 0..7 && c in 0..7) {
                val piece = board.getPiece(r, c)
                if (piece != null) {
                    if (piece.color == attackingColor && (piece.type == PieceType.ROOK || piece.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                r += dr
                c += dc
            }
        }

        // 5. Sliding pieces: Diagonal (Bishop, Queen)
        val diagDirs = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
        for ((dr, dc) in diagDirs) {
            var r = target.row + dr
            var c = target.col + dc
            while (r in 0..7 && c in 0..7) {
                val piece = board.getPiece(r, c)
                if (piece != null) {
                    if (piece.color == attackingColor && (piece.type == PieceType.BISHOP || piece.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                r += dr
                c += dc
            }
        }

        return false
    }

    fun isInCheck(board: Board, color: PieceColor): Boolean {
        val kingSquare = board.findKing(color) ?: return false
        return isSquareAttackedBy(board, kingSquare, color.opposite)
    }

    fun generatePseudoLegalMoves(board: Board, color: PieceColor): List<Move> {
        val moves = mutableListOf<Move>()
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board.getPiece(r, c) ?: continue
                if (piece.color != color) continue
                val from = Square(r, c)

                when (piece.type) {
                    PieceType.PAWN -> generatePawnMoves(board, from, color, moves)
                    PieceType.KNIGHT -> generateKnightMoves(board, from, color, moves)
                    PieceType.BISHOP -> generateSlidingMoves(board, from, color, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1), moves)
                    PieceType.ROOK -> generateSlidingMoves(board, from, color, listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1), moves)
                    PieceType.QUEEN -> {
                        generateSlidingMoves(board, from, color, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1), moves)
                        generateSlidingMoves(board, from, color, listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1), moves)
                    }
                    PieceType.KING -> generateKingMoves(board, from, color, moves)
                }
            }
        }
        return moves
    }

    private fun generatePawnMoves(board: Board, from: Square, color: PieceColor, moves: MutableList<Move>) {
        val dir = if (color == PieceColor.WHITE) -1 else 1
        val startRow = if (color == PieceColor.WHITE) 6 else 1
        val promoRow = if (color == PieceColor.WHITE) 0 else 7

        // 1. Single step forward
        val oneStepRow = from.row + dir
        if (oneStepRow in 0..7 && board.getPiece(oneStepRow, from.col) == null) {
            val to = Square(oneStepRow, from.col)
            if (oneStepRow == promoRow) {
                for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                    moves.add(Move(from, to, promotion = promo))
                }
            } else {
                moves.add(Move(from, to))
                // 2. Double step forward
                if (from.row == startRow) {
                    val twoStepRow = from.row + 2 * dir
                    if (board.getPiece(twoStepRow, from.col) == null) {
                        moves.add(Move(from, Square(twoStepRow, from.col)))
                    }
                }
            }
        }

        // 3. Captures & En-passant
        for (dc in listOf(-1, 1)) {
            val capCol = from.col + dc
            if (capCol in 0..7) {
                val capRow = from.row + dir
                if (capRow in 0..7) {
                    val targetSquare = Square(capRow, capCol)
                    val targetPiece = board.getPiece(targetSquare)
                    if (targetPiece != null && targetPiece.color != color) {
                        if (capRow == promoRow) {
                            for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                                moves.add(Move(from, targetSquare, promotion = promo, capturedPiece = targetPiece))
                            }
                        } else {
                            moves.add(Move(from, targetSquare, capturedPiece = targetPiece))
                        }
                    } else if (targetSquare == board.enPassantTarget) {
                        val epCapturedPiece = board.getPiece(from.row, capCol)
                        moves.add(Move(from, targetSquare, isEnPassant = true, capturedPiece = epCapturedPiece))
                    }
                }
            }
        }
    }

    private fun generateKnightMoves(board: Board, from: Square, color: PieceColor, moves: MutableList<Move>) {
        val offsets = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1
        )
        for ((dr, dc) in offsets) {
            val r = from.row + dr
            val c = from.col + dc
            if (r in 0..7 && c in 0..7) {
                val to = Square(r, c)
                val target = board.getPiece(to)
                if (target == null) {
                    moves.add(Move(from, to))
                } else if (target.color != color) {
                    moves.add(Move(from, to, capturedPiece = target))
                }
            }
        }
    }

    private fun generateSlidingMoves(
        board: Board,
        from: Square,
        color: PieceColor,
        directions: List<Pair<Int, Int>>,
        moves: MutableList<Move>
    ) {
        for ((dr, dc) in directions) {
            var r = from.row + dr
            var c = from.col + dc
            while (r in 0..7 && c in 0..7) {
                val to = Square(r, c)
                val target = board.getPiece(to)
                if (target == null) {
                    moves.add(Move(from, to))
                } else {
                    if (target.color != color) {
                        moves.add(Move(from, to, capturedPiece = target))
                    }
                    break
                }
                r += dr
                c += dc
            }
        }
    }

    private fun generateKingMoves(board: Board, from: Square, color: PieceColor, moves: MutableList<Move>) {
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = from.row + dr
                val c = from.col + dc
                if (r in 0..7 && c in 0..7) {
                    val to = Square(r, c)
                    val target = board.getPiece(to)
                    if (target == null) {
                        moves.add(Move(from, to))
                    } else if (target.color != color) {
                        moves.add(Move(from, to, capturedPiece = target))
                    }
                }
            }
        }

        // Castling
        val enemyColor = color.opposite
        val kingRow = if (color == PieceColor.WHITE) 7 else 0
        if (from.row == kingRow && from.col == 4 && !isInCheck(board, color)) {
            // King-side castling
            val canCastleK = if (color == PieceColor.WHITE) board.whiteCanCastleKingSide else board.blackCanCastleKingSide
            if (canCastleK) {
                if (board.getPiece(kingRow, 5) == null && board.getPiece(kingRow, 6) == null) {
                    val rook = board.getPiece(kingRow, 7)
                    if (rook != null && rook.type == PieceType.ROOK && rook.color == color) {
                        if (!isSquareAttackedBy(board, Square(kingRow, 5), enemyColor) &&
                            !isSquareAttackedBy(board, Square(kingRow, 6), enemyColor)
                        ) {
                            moves.add(Move(from, Square(kingRow, 6), isCastling = true))
                        }
                    }
                }
            }

            // Queen-side castling
            val canCastleQ = if (color == PieceColor.WHITE) board.whiteCanCastleQueenSide else board.blackCanCastleQueenSide
            if (canCastleQ) {
                if (board.getPiece(kingRow, 1) == null && board.getPiece(kingRow, 2) == null && board.getPiece(kingRow, 3) == null) {
                    val rook = board.getPiece(kingRow, 0)
                    if (rook != null && rook.type == PieceType.ROOK && rook.color == color) {
                        if (!isSquareAttackedBy(board, Square(kingRow, 3), enemyColor) &&
                            !isSquareAttackedBy(board, Square(kingRow, 2), enemyColor)
                        ) {
                            moves.add(Move(from, Square(kingRow, 2), isCastling = true))
                        }
                    }
                }
            }
        }
    }

    fun makeMove(board: Board, move: Move) {
        val movingPiece = board.getPiece(move.from) ?: return

        // 1. En-passant capture removal
        if (move.isEnPassant) {
            val capRow = move.from.row
            val capCol = move.to.col
            board.setPiece(Square(capRow, capCol), null)
        }

        // 2. Move moving piece
        val placedPiece = if (move.promotion != null) {
            Piece(move.promotion, movingPiece.color)
        } else {
            movingPiece
        }
        board.setPiece(move.to, placedPiece)
        board.setPiece(move.from, null)

        // 3. Castling rook relocation
        if (move.isCastling) {
            val row = move.from.row
            if (move.to.col == 6) { // Kingside
                val rook = board.getPiece(row, 7)
                board.setPiece(Square(row, 5), rook)
                board.setPiece(Square(row, 7), null)
            } else if (move.to.col == 2) { // Queenside
                val rook = board.getPiece(row, 0)
                board.setPiece(Square(row, 3), rook)
                board.setPiece(Square(row, 0), null)
            }
        }

        // 4. Update en-passant square
        if (movingPiece.type == PieceType.PAWN && Math.abs(move.to.row - move.from.row) == 2) {
            val epRow = (move.from.row + move.to.row) / 2
            board.enPassantTarget = Square(epRow, move.from.col)
        } else {
            board.enPassantTarget = null
        }

        // 5. Update castling rights
        if (movingPiece.type == PieceType.KING) {
            if (movingPiece.color == PieceColor.WHITE) {
                board.whiteCanCastleKingSide = false
                board.whiteCanCastleQueenSide = false
            } else {
                board.blackCanCastleKingSide = false
                board.blackCanCastleQueenSide = false
            }
        } else if (movingPiece.type == PieceType.ROOK) {
            if (move.from.row == 7 && move.from.col == 0) board.whiteCanCastleQueenSide = false
            if (move.from.row == 7 && move.from.col == 7) board.whiteCanCastleKingSide = false
            if (move.from.row == 0 && move.from.col == 0) board.blackCanCastleQueenSide = false
            if (move.from.row == 0 && move.from.col == 7) board.blackCanCastleKingSide = false
        }

        // If a rook is captured on its original square
        if (move.to.row == 7 && move.to.col == 0) board.whiteCanCastleQueenSide = false
        if (move.to.row == 7 && move.to.col == 7) board.whiteCanCastleKingSide = false
        if (move.to.row == 0 && move.to.col == 0) board.blackCanCastleQueenSide = false
        if (move.to.row == 0 && move.to.col == 7) board.blackCanCastleKingSide = false

        // 6. Halfmove clock & fullmove
        if (movingPiece.type == PieceType.PAWN || move.capturedPiece != null) {
            board.halfmoveClock = 0
        } else {
            board.halfmoveClock++
        }

        if (board.activeColor == PieceColor.BLACK) {
            board.fullmoveNumber++
        }

        // 7. Toggle active color
        board.activeColor = board.activeColor.opposite
    }

    fun getLegalMoves(board: Board, color: PieceColor = board.activeColor): List<Move> {
        val pseudoMoves = generatePseudoLegalMoves(board, color)
        val legalMoves = mutableListOf<Move>()

        for (m in pseudoMoves) {
            val testBoard = board.clone()
            makeMove(testBoard, m)
            if (!isInCheck(testBoard, color)) {
                // Populate SAN notation
                m.san = formatSan(board, m, testBoard)
                legalMoves.add(m)
            }
        }
        return legalMoves
    }

    private fun formatSan(currentBoard: Board, move: Move, nextBoard: Board): String {
        if (move.isCastling) {
            val castlingStr = if (move.to.col == 6) "O-O" else "O-O-O"
            return if (isInCheck(nextBoard, nextBoard.activeColor)) {
                if (getLegalMoves(nextBoard).isEmpty()) "$castlingStr#" else "$castlingStr+"
            } else castlingStr
        }

        val piece = currentBoard.getPiece(move.from) ?: return move.uci
        val sb = StringBuilder()

        if (piece.type != PieceType.PAWN) {
            sb.append(piece.type.notation)
            // Disambiguation
            val others = generatePseudoLegalMoves(currentBoard, piece.color).filter {
                it.to == move.to && it.from != move.from &&
                        currentBoard.getPiece(it.from)?.type == piece.type
            }
            if (others.isNotEmpty()) {
                val sameCol = others.any { it.from.col == move.from.col }
                val sameRow = others.any { it.from.row == move.from.row }
                if (!sameCol) {
                    sb.append(move.from.fileChar)
                } else if (!sameRow) {
                    sb.append(move.from.rankNumber)
                } else {
                    sb.append(move.from.toAlgebraic())
                }
            }
        }

        val isCapture = move.capturedPiece != null || move.isEnPassant
        if (isCapture) {
            if (piece.type == PieceType.PAWN) {
                sb.append(move.from.fileChar)
            }
            sb.append('x')
        }

        sb.append(move.to.toAlgebraic())

        if (move.promotion != null) {
            sb.append("=").append(move.promotion.notation)
        }

        if (isInCheck(nextBoard, nextBoard.activeColor)) {
            val oppLegal = generatePseudoLegalMoves(nextBoard, nextBoard.activeColor).filter {
                val temp = nextBoard.clone()
                makeMove(temp, it)
                !isInCheck(temp, nextBoard.activeColor)
            }
            if (oppLegal.isEmpty()) {
                sb.append('#')
            } else {
                sb.append('+')
            }
        }

        return sb.toString()
    }

    fun isCheckmate(board: Board): Boolean {
        return isInCheck(board, board.activeColor) && getLegalMoves(board).isEmpty()
    }

    fun isStalemate(board: Board): Boolean {
        return !isInCheck(board, board.activeColor) && getLegalMoves(board).isEmpty()
    }

    fun isInsufficientMaterial(board: Board): Boolean {
        val pieces = mutableListOf<Piece>()
        for (r in 0..7) {
            for (c in 0..7) {
                board.getPiece(r, c)?.let { pieces.add(it) }
            }
        }
        if (pieces.size == 2) return true // K vs K
        if (pieces.size == 3) {
            val nonKing = pieces.find { it.type != PieceType.KING }
            if (nonKing != null && (nonKing.type == PieceType.BISHOP || nonKing.type == PieceType.KNIGHT)) {
                return true // K+B vs K or K+N vs K
            }
        }
        return false
    }
}
