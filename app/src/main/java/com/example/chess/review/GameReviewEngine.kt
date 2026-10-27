package com.example.chess.review

import com.example.chess.ai.AdvancedChessEngine
import com.example.chess.ai.EngineLevel
import com.example.chess.engine.Board
import com.example.chess.engine.GameRules
import com.example.chess.engine.Move
import com.example.chess.engine.PieceColor
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

enum class MoveClassification(val symbol: String, val displayName: String) {
    BRILLIANT("!!", "Brilliant"),
    BEST("*", "Best"),
    EXCELLENT("!", "Excellent"),
    GOOD("✓", "Good"),
    INACCURACY("?!", "Inaccuracy"),
    MISTAKE("?", "Mistake"),
    BLUNDER("??", "Blunder"),
    BOOK("📖", "Book Move")
}

data class MoveReviewItem(
    val ply: Int,
    val moveNumber: Int,
    val isWhite: Boolean,
    val playedMove: Move,
    val bestEngineMove: Move?,
    val evalBefore: Int, // Centipawns (White perspective)
    val evalAfter: Int,  // Centipawns (White perspective)
    val evalDelta: Int,  // Player perspective drop (loss is positive)
    val classification: MoveClassification,
    val comment: String
)

data class WeakPointAnalysis(
    val playerColor: PieceColor,
    val overallAccuracy: Int, // 0..100%
    val opponentAccuracy: Int,
    val whiteAccuracy: Int,
    val blackAccuracy: Int,
    val blundersCount: Int,
    val mistakesCount: Int,
    val inaccuraciesCount: Int,
    val bestMovesCount: Int,
    val brilliantMovesCount: Int,
    val primaryWeakness: String,
    val weakPointCategory: String,
    val detailedAdvice: String,
    val recommendedPuzzleTheme: String,
    val recommendedOpeningToStudy: String,
    val criticalTurnPly: Int?
)

data class FullGameReview(
    val items: List<MoveReviewItem>,
    val whiteAccuracy: Int,
    val blackAccuracy: Int,
    val weakPointAnalysis: WeakPointAnalysis
)

object GameReviewEngine {

    fun reviewGame(
        moves: List<Move>,
        playerColor: PieceColor = PieceColor.WHITE
    ): FullGameReview {
        val reviewItems = mutableListOf<MoveReviewItem>()
        val board = Board()
        board.setupInitialPosition()

        var currentEval = AdvancedChessEngine.getWhitePerspectiveEval(board)
        val playerLosses = mutableListOf<Double>()
        val opponentLosses = mutableListOf<Double>()

        var blunders = 0
        var mistakes = 0
        var inaccuracies = 0
        var bestCount = 0
        var brilliantCount = 0
        var criticalTurnPly: Int? = null
        var maxBlunderDrop = 0

        var openingBlunders = 0
        var middleBlunders = 0
        var endBlunders = 0

        for ((index, move) in moves.withIndex()) {
            val ply = index + 1
            val moveNum = (index / 2) + 1
            val isWhite = board.activeColor == PieceColor.WHITE
            val isPlayerMove = board.activeColor == playerColor

            // Find best engine move in this position
            val engineEval = AdvancedChessEngine.findBestMove(board, EngineLevel.INTERMEDIATE)
            val bestMove = engineEval.bestMove
            val evalBefore = currentEval

            // Play move
            GameRules.makeMove(board, move)
            val evalAfter = AdvancedChessEngine.getWhitePerspectiveEval(board)
            currentEval = evalAfter

            // Calculate drop from player perspective
            val evalDrop = if (isWhite) {
                (evalBefore - evalAfter).coerceAtLeast(0)
            } else {
                (evalAfter - evalBefore).coerceAtLeast(0)
            }

            if (isPlayerMove) {
                playerLosses.add(evalDrop.toDouble())
            } else {
                opponentLosses.add(evalDrop.toDouble())
            }

            // Classify move
            val classification: MoveClassification
            val comment: String

            val isSacrifice = move.capturedPiece == null &&
                    move.from.rankNumber in 3..6 &&
                    evalDrop <= 10 &&
                    (move.to.row in 1..6)

            when {
                evalDrop == 0 && isSacrifice && (index > 8) -> {
                    classification = MoveClassification.BRILLIANT
                    comment = "Brilliant positional move finding a decisive tactical resource!"
                    if (isPlayerMove) brilliantCount++
                }
                evalDrop <= 15 || (bestMove != null && move.isSameMove(bestMove)) -> {
                    classification = MoveClassification.BEST
                    comment = "The top computer move. Maintains optimal engine play."
                    if (isPlayerMove) bestCount++
                }
                evalDrop <= 45 -> {
                    classification = MoveClassification.EXCELLENT
                    comment = "Very strong and sound move keeping the advantage."
                }
                evalDrop <= 90 -> {
                    classification = MoveClassification.GOOD
                    comment = "A natural, solid continuation."
                }
                evalDrop <= 180 -> {
                    classification = MoveClassification.INACCURACY
                    val alt = bestMove?.san ?: "an alternative"
                    comment = "Slight inaccuracy. $alt was positionally crisper."
                    if (isPlayerMove) inaccuracies++
                }
                evalDrop <= 320 -> {
                    classification = MoveClassification.MISTAKE
                    val alt = bestMove?.san ?: "the top engine move"
                    comment = "Mistake that conceded positional advantage. Better was $alt."
                    if (isPlayerMove) mistakes++
                }
                else -> {
                    classification = MoveClassification.BLUNDER
                    val alt = bestMove?.san ?: "the winning move"
                    comment = "Blunder that drastically changed the game balance. $alt was required."
                    if (isPlayerMove) {
                        blunders++
                        if (index < 16) openingBlunders++
                        else if (index < 40) middleBlunders++
                        else endBlunders++

                        if (evalDrop > maxBlunderDrop) {
                            maxBlunderDrop = evalDrop
                            criticalTurnPly = ply
                        }
                    }
                }
            }

            reviewItems.add(
                MoveReviewItem(
                    ply = ply,
                    moveNumber = moveNum,
                    isWhite = isWhite,
                    playedMove = move,
                    bestEngineMove = bestMove,
                    evalBefore = evalBefore,
                    evalAfter = evalAfter,
                    evalDelta = evalDrop,
                    classification = classification,
                    comment = comment
                )
            )
        }

        // Calculate accuracy percentages
        val playerAvgLoss = if (playerLosses.isNotEmpty()) playerLosses.average() else 20.0
        val oppAvgLoss = if (opponentLosses.isNotEmpty()) opponentLosses.average() else 20.0

        val playerAccuracy = (100.0 * exp(-0.004 * playerAvgLoss)).toInt().coerceIn(35, 99)
        val oppAccuracy = (100.0 * exp(-0.004 * oppAvgLoss)).toInt().coerceIn(35, 99)

        val whiteAcc = if (playerColor == PieceColor.WHITE) playerAccuracy else oppAccuracy
        val blackAcc = if (playerColor == PieceColor.BLACK) playerAccuracy else oppAccuracy

        // Diagnose weak point
        val primaryWeakness: String
        val weakPointCategory: String
        val detailedAdvice: String
        val recTheme: String
        val recOpening: String

        when {
            blunders == 0 && mistakes <= 1 -> {
                primaryWeakness = "Solid Execution & Endgame Conversion"
                weakPointCategory = "Endgame Polish"
                detailedAdvice = "You played remarkably clean chess with almost zero tactical mistakes. To climb higher, focus on converting minor piece advantages into winning pawn structures in the late endgame."
                recTheme = "Endgame"
                recOpening = "Ruy Lopez"
            }
            openingBlunders >= middleBlunders && openingBlunders > 0 -> {
                primaryWeakness = "Opening Preparation & Center Control"
                weakPointCategory = "Opening Phase"
                detailedAdvice = "You suffered early deviations in the opening (moves 1-10) that gave your opponent rapid development and free center space. Memorizing opening principles and pawn levers will immediately boost your win rate."
                recTheme = "Pins"
                recOpening = "Italian Game"
            }
            middleBlunders >= 2 -> {
                primaryWeakness = "Tactical Awareness in Complex Middlegames"
                weakPointCategory = "Tactics & Board Vision"
                detailedAdvice = "Critical blunders occurred in the middle game where hanging pieces or tactical motifs (forks, pins, skewers) were overlooked. Before each move, always check all checks, captures, and threats (CCT)."
                recTheme = "Forks"
                recOpening = "Sicilian Defense"
            }
            endBlunders > 0 -> {
                primaryWeakness = "Endgame Precision & King Activity"
                weakPointCategory = "Endgame Technique"
                detailedAdvice = "In the simplified phase, your king remained too passive. In the endgame, the King is an attacking piece and must march into the center to escort passed pawns."
                recTheme = "Passed Pawns"
                recOpening = "Queen's Gambit"
            }
            else -> {
                primaryWeakness = "Defensive Calculation & Threat Detection"
                weakPointCategory = "Calculation"
                detailedAdvice = "Minor inaccuracies accumulated over the course of the game. Try asking yourself: 'What is my opponent threatening if they move next?' before confirming your move."
                recTheme = "Discovered Attacks"
                recOpening = "French Defense"
            }
        }

        val weakPointAnalysis = WeakPointAnalysis(
            playerColor = playerColor,
            overallAccuracy = playerAccuracy,
            opponentAccuracy = oppAccuracy,
            whiteAccuracy = whiteAcc,
            blackAccuracy = blackAcc,
            blundersCount = blunders,
            mistakesCount = mistakes,
            inaccuraciesCount = inaccuracies,
            bestMovesCount = bestCount,
            brilliantMovesCount = brilliantCount,
            primaryWeakness = primaryWeakness,
            weakPointCategory = weakPointCategory,
            detailedAdvice = detailedAdvice,
            recommendedPuzzleTheme = recTheme,
            recommendedOpeningToStudy = recOpening,
            criticalTurnPly = criticalTurnPly
        )

        return FullGameReview(
            items = reviewItems,
            whiteAccuracy = whiteAcc,
            blackAccuracy = blackAcc,
            weakPointAnalysis = weakPointAnalysis
        )
    }
}
