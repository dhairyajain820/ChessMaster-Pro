package com.example.chess.ai

import com.example.chess.engine.Board
import com.example.chess.engine.GameRules
import com.example.chess.engine.Move
import com.example.chess.engine.Piece
import com.example.chess.engine.PieceColor
import com.example.chess.engine.PieceType
import com.example.chess.engine.Square
import com.example.chess.engine.Zobrist
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class EngineLevel(val displayName: String, val elo: Int, val maxDepth: Int, val blunderProbability: Float) {
    BEGINNER("Beginner (Novice)", 800, 1, 0.40f),
    CASUAL("Casual (Club)", 1200, 2, 0.18f),
    INTERMEDIATE("Intermediate (Strong)", 1600, 3, 0.05f),
    MASTER("Master (Elite)", 2000, 4, 0.0f),
    GRANDMASTER("Grandmaster (Stockfish-Tier)", 2400, 5, 0.0f)
}

data class EngineEvaluation(
    val scoreCentipawns: Int,
    val bestMove: Move?,
    val nodesEvaluated: Int,
    val searchDepth: Int,
    val principalVariation: List<String> = emptyList()
) {
    val winProbabilityPercent: Int
        get() {
            // Standard logistic conversion
            val clamped = scoreCentipawns.coerceIn(-1500, 1500)
            val wp = 50.0 + 50.0 * (2.0 / (1.0 + Math.exp(-0.00368208 * clamped)) - 1.0)
            return wp.toInt().coerceIn(1, 99)
        }

    val formattedScore: String
        get() {
            if (scoreCentipawns > 15000) {
                val mateIn = (20000 - scoreCentipawns + 1) / 2
                return "+M$mateIn"
            }
            if (scoreCentipawns < -15000) {
                val mateIn = (20000 + scoreCentipawns + 1) / 2
                return "-M$mateIn"
            }
            val sign = if (scoreCentipawns > 0) "+" else ""
            val cp = scoreCentipawns / 100.0
            return String.format("%s%.1f", sign, cp)
        }
}

object AdvancedChessEngine {

    private const val INF = 30000
    private const val MATE_SCORE = 20000

    // Transposition Table Entry
    private const val EXACT = 0
    private const val LOWER_BOUND = 1
    private const val UPPER_BOUND = 2

    private class TTEntry(
        val hash: Long,
        val depth: Int,
        val score: Int,
        val flag: Int,
        val bestMove: Move?
    )

    private const val TT_SIZE = 131072 // 2^17
    private val transpositionTable = arrayOfNulls<TTEntry>(TT_SIZE)

    // Killer moves [ply][2]
    private val killerMoves = Array(64) { arrayOfNulls<Move>(2) }
    // History heuristic [64][64]
    private val historyHeuristic = Array(64) { IntArray(64) }

    private var nodesVisited = 0

    // Piece-Square Tables (Midgame & Endgame) - standard 8x8 mapped from White's perspective
    private val PAWN_PST_MG = intArrayOf(
        0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5,  5, 10, 25, 25, 10,  5,  5,
        0,  0,  0, 20, 20,  0,  0,  0,
        5, -5,-10,  0,  0,-10, -5,  5,
        5, 10, 10,-20,-20, 10, 10,  5,
        0,  0,  0,  0,  0,  0,  0,  0
    )

    private val PAWN_PST_EG = intArrayOf(
        0,  0,  0,  0,  0,  0,  0,  0,
        90, 90, 90, 90, 90, 90, 90, 90,
        50, 50, 50, 50, 50, 50, 50, 50,
        30, 30, 30, 30, 30, 30, 30, 30,
        20, 20, 20, 20, 20, 20, 20, 20,
        10, 10, 10, 10, 10, 10, 10, 10,
        0,  0,  0,  0,  0,  0,  0,  0,
        0,  0,  0,  0,  0,  0,  0,  0
    )

    private val KNIGHT_PST = intArrayOf(
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    )

    private val BISHOP_PST = intArrayOf(
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    )

    private val ROOK_PST = intArrayOf(
        0,  0,  0,  0,  0,  0,  0,  0,
        15, 20, 20, 20, 20, 20, 20, 15,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        0,  0,  0,  5,  5,  0,  0,  0
    )

    private val QUEEN_PST = intArrayOf(
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
        -5,  0,  5,  5,  5,  5,  0, -5,
        0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    )

    private val KING_PST_MG = intArrayOf(
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        20, 20,  0,  0,  0,  0, 20, 20,
        20, 30, 10,  0,  0, 10, 30, 20
    )

    private val KING_PST_EG = intArrayOf(
        -50,-40,-30,-20,-20,-30,-40,-50,
        -30,-20,-10,  0,  0,-10,-20,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-30,  0,  0,  0,  0,-30,-30,
        -50,-30,-30,-30,-30,-30,-30,-50
    )

    fun clearTables() {
        for (i in transpositionTable.indices) transpositionTable[i] = null
        for (i in killerMoves.indices) {
            killerMoves[i][0] = null
            killerMoves[i][1] = null
        }
        for (i in historyHeuristic.indices) {
            historyHeuristic[i].fill(0)
        }
    }

    fun findBestMove(board: Board, level: EngineLevel): EngineEvaluation {
        val legalMoves = GameRules.getLegalMoves(board)
        if (legalMoves.isEmpty()) {
            val inCheck = GameRules.isInCheck(board, board.activeColor)
            val score = if (inCheck) -MATE_SCORE else 0
            return EngineEvaluation(score, null, 0, 0)
        }

        // Blunder injection for beginner and casual levels
        if (level.blunderProbability > 0f && Random.nextFloat() < level.blunderProbability) {
            val randomMove = legalMoves.random()
            return EngineEvaluation(0, randomMove, 1, 1)
        }

        nodesVisited = 0
        var bestOverallMove: Move = legalMoves.first()
        var bestOverallScore = -INF

        // Iterative Deepening up to level.maxDepth
        for (depth in 1..level.maxDepth) {
            var alpha = -INF
            val beta = INF
            var currentBestMove: Move? = null

            // Order legal moves
            val orderedMoves = orderMoves(board, legalMoves, 0, null)

            for (move in orderedMoves) {
                val nextBoard = board.clone()
                GameRules.makeMove(nextBoard, move)

                val score = -alphaBeta(nextBoard, depth - 1, -beta, -alpha, 1)

                if (score > alpha) {
                    alpha = score
                    currentBestMove = move
                }
            }

            if (currentBestMove != null) {
                bestOverallMove = currentBestMove
                bestOverallScore = alpha
            }

            // If we found a forced checkmate, break early
            if (bestOverallScore >= MATE_SCORE - 100) {
                break
            }
        }

        val perspectiveScore = if (board.activeColor == PieceColor.WHITE) bestOverallScore else -bestOverallScore
        return EngineEvaluation(
            scoreCentipawns = perspectiveScore,
            bestMove = bestOverallMove,
            nodesEvaluated = nodesVisited,
            searchDepth = level.maxDepth
        )
    }

    private fun alphaBeta(
        board: Board,
        depth: Int,
        alphaParam: Int,
        beta: Int,
        ply: Int
    ): Int {
        var alpha = alphaParam
        nodesVisited++

        if (GameRules.isInsufficientMaterial(board) || board.halfmoveClock >= 100) {
            return 0
        }

        // Transposition table lookup
        val hash = Zobrist.computeHash(board)
        val ttIndex = (hash.toInt() and 0x7FFFFFFF) % TT_SIZE
        val entry = transpositionTable[ttIndex]
        var ttMove: Move? = null

        if (entry != null && entry.hash == hash && entry.depth >= depth) {
            ttMove = entry.bestMove
            when (entry.flag) {
                EXACT -> return entry.score
                LOWER_BOUND -> if (entry.score >= beta) return entry.score
                UPPER_BOUND -> if (entry.score <= alpha) return entry.score
            }
        }

        if (depth <= 0) {
            return quiescenceSearch(board, alpha, beta, ply)
        }

        val legalMoves = GameRules.getLegalMoves(board)
        if (legalMoves.isEmpty()) {
            return if (GameRules.isInCheck(board, board.activeColor)) {
                -MATE_SCORE + ply // Prefer faster mate
            } else {
                0 // Stalemate
            }
        }

        val orderedMoves = orderMoves(board, legalMoves, ply, ttMove)
        var bestScore = -INF
        var bestMove: Move? = null
        val originalAlpha = alpha

        for (move in orderedMoves) {
            val nextBoard = board.clone()
            GameRules.makeMove(nextBoard, move)

            val score = -alphaBeta(nextBoard, depth - 1, -beta, -alpha, ply + 1)

            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }

            if (score > alpha) {
                alpha = score
            }

            if (alpha >= beta) {
                // Beta cutoff
                if (move.capturedPiece == null && ply < 64) {
                    killerMoves[ply][1] = killerMoves[ply][0]
                    killerMoves[ply][0] = move
                    val fromIdx = move.from.row * 8 + move.from.col
                    val toIdx = move.to.row * 8 + move.to.col
                    historyHeuristic[fromIdx][toIdx] += depth * depth
                }
                break
            }
        }

        // Save to TT
        val flag = when {
            bestScore <= originalAlpha -> UPPER_BOUND
            bestScore >= beta -> LOWER_BOUND
            else -> EXACT
        }
        transpositionTable[ttIndex] = TTEntry(hash, depth, bestScore, flag, bestMove)

        return bestScore
    }

    private fun quiescenceSearch(board: Board, alphaParam: Int, beta: Int, ply: Int): Int {
        nodesVisited++
        var alpha = alphaParam
        val standPat = evaluatePosition(board)

        if (standPat >= beta) return beta
        if (standPat > alpha) alpha = standPat

        // Only search captures in quiescence
        val captures = GameRules.getLegalMoves(board).filter { it.capturedPiece != null || it.promotion != null }
        val orderedCaptures = captures.sortedByDescending { getCaptureScore(it) }

        for (move in orderedCaptures) {
            val nextBoard = board.clone()
            GameRules.makeMove(nextBoard, move)

            val score = -quiescenceSearch(nextBoard, -beta, -alpha, ply + 1)
            if (score >= beta) return beta
            if (score > alpha) alpha = score
        }

        return alpha
    }

    // Move Ordering heuristic
    private fun orderMoves(board: Board, moves: List<Move>, ply: Int, hashMove: Move?): List<Move> {
        return moves.sortedByDescending { move ->
            when {
                hashMove != null && move.isSameMove(hashMove) -> 1000000
                move.capturedPiece != null -> 100000 + getCaptureScore(move)
                move.promotion != null -> 90000
                ply < 64 && killerMoves[ply][0]?.isSameMove(move) == true -> 80000
                ply < 64 && killerMoves[ply][1]?.isSameMove(move) == true -> 70000
                else -> {
                    val fromIdx = move.from.row * 8 + move.from.col
                    val toIdx = move.to.row * 8 + move.to.col
                    historyHeuristic[fromIdx][toIdx]
                }
            }
        }
    }

    private fun getCaptureScore(move: Move): Int {
        val victimValue = move.capturedPiece?.type?.baseValue ?: 0
        val attackerValue = PieceType.PAWN.baseValue // Approximation
        return victimValue * 10 - attackerValue
    }

    // Full positional & material evaluation (from active player's perspective)
    fun evaluatePosition(board: Board): Int {
        var whiteMgScore = 0
        var blackMgScore = 0
        var whiteEgScore = 0
        var blackEgScore = 0
        var gamePhase = 0

        var whiteBishops = 0
        var blackBishops = 0

        val whitePawnsInCols = IntArray(8)
        val blackPawnsInCols = IntArray(8)

        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board.getPiece(r, c) ?: continue
                val isWhite = piece.color == PieceColor.WHITE
                val sqIdx = r * 8 + c
                val mappedIdx = if (isWhite) sqIdx else (7 - r) * 8 + c

                // Material & phase weight
                val value = piece.type.baseValue
                when (piece.type) {
                    PieceType.KNIGHT, PieceType.BISHOP -> gamePhase += 1
                    PieceType.ROOK -> gamePhase += 2
                    PieceType.QUEEN -> gamePhase += 4
                    else -> {}
                }

                // PST and Material
                var mg = value
                var eg = value

                when (piece.type) {
                    PieceType.PAWN -> {
                        mg += PAWN_PST_MG[mappedIdx]
                        eg += PAWN_PST_EG[mappedIdx]
                        if (isWhite) whitePawnsInCols[c]++ else blackPawnsInCols[c]++
                    }
                    PieceType.KNIGHT -> {
                        mg += KNIGHT_PST[mappedIdx]
                        eg += KNIGHT_PST[mappedIdx]
                    }
                    PieceType.BISHOP -> {
                        mg += BISHOP_PST[mappedIdx]
                        eg += BISHOP_PST[mappedIdx]
                        if (isWhite) whiteBishops++ else blackBishops++
                    }
                    PieceType.ROOK -> {
                        mg += ROOK_PST[mappedIdx]
                        eg += ROOK_PST[mappedIdx]
                        // 7th rank bonus
                        if (isWhite && r == 1) mg += 25
                        if (!isWhite && r == 6) mg += 25
                    }
                    PieceType.QUEEN -> {
                        mg += QUEEN_PST[mappedIdx]
                        eg += QUEEN_PST[mappedIdx]
                    }
                    PieceType.KING -> {
                        mg += KING_PST_MG[mappedIdx]
                        eg += KING_PST_EG[mappedIdx]
                    }
                }

                if (isWhite) {
                    whiteMgScore += mg
                    whiteEgScore += eg
                } else {
                    blackMgScore += mg
                    blackEgScore += eg
                }
            }
        }

        // Bishop pair bonus
        if (whiteBishops >= 2) {
            whiteMgScore += 35
            whiteEgScore += 45
        }
        if (blackBishops >= 2) {
            blackMgScore += 35
            blackEgScore += 45
        }

        // Pawn structure: Doubled & Isolated pawns
        for (c in 0..7) {
            if (whitePawnsInCols[c] > 1) {
                whiteMgScore -= 20 * (whitePawnsInCols[c] - 1)
                whiteEgScore -= 25 * (whitePawnsInCols[c] - 1)
            }
            if (blackPawnsInCols[c] > 1) {
                blackMgScore -= 20 * (blackPawnsInCols[c] - 1)
                blackEgScore -= 25 * (blackPawnsInCols[c] - 1)
            }
        }

        // Tapered evaluation interpolation
        val clampedPhase = min(24, gamePhase)
        val mgScore = whiteMgScore - blackMgScore
        val egScore = whiteEgScore - blackEgScore
        val eval = ((mgScore * clampedPhase) + (egScore * (24 - clampedPhase))) / 24

        return if (board.activeColor == PieceColor.WHITE) eval else -eval
    }

    // Evaluate static centipawns for White perspective (useful for review & eval bar)
    fun getWhitePerspectiveEval(board: Board): Int {
        val eval = evaluatePosition(board)
        return if (board.activeColor == PieceColor.WHITE) eval else -eval
    }
}
