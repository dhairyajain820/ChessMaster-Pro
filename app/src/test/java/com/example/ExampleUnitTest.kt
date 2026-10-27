package com.example

import com.example.chess.ai.AdvancedChessEngine
import com.example.chess.ai.EngineLevel
import com.example.chess.engine.Board
import com.example.chess.engine.GameRules
import com.example.chess.engine.PieceColor
import com.example.chess.openings.OpeningRepertoire
import com.example.chess.puzzles.PuzzleDifficulty
import com.example.chess.puzzles.PuzzleRepository
import com.example.chess.review.GameReviewEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun startingPosition_has20LegalMoves() {
        val board = Board()
        board.setupInitialPosition()
        val legalMoves = GameRules.getLegalMoves(board)
        assertEquals(20, legalMoves.size)
    }

    @Test
    fun advancedChessEngine_evaluatesAndFindsBestMove() {
        val board = Board()
        board.setupInitialPosition()
        val eval = AdvancedChessEngine.findBestMove(board, EngineLevel.INTERMEDIATE)
        assertNotNull(eval.bestMove)
        assertTrue(eval.nodesEvaluated > 0)
    }

    @Test
    fun puzzleRepository_hasDiversePuzzles() {
        val easy = PuzzleRepository.getPuzzlesByDifficulty(PuzzleDifficulty.EASY)
        val medium = PuzzleRepository.getPuzzlesByDifficulty(PuzzleDifficulty.MEDIUM)
        val hard = PuzzleRepository.getPuzzlesByDifficulty(PuzzleDifficulty.HARD)

        assertTrue(easy.isNotEmpty())
        assertTrue(medium.isNotEmpty())
        assertTrue(hard.isNotEmpty())
    }

    @Test
    fun openingRepertoire_hasRequiredOpenings() {
        val openings = OpeningRepertoire.allOpenings
        assertTrue(openings.any { it.id == "ruy_lopez" })
        assertTrue(openings.any { it.id == "sicilian_najdorf" })
        assertTrue(openings.any { it.id == "italian_game" })
        assertTrue(openings.any { it.id == "queens_gambit" })
    }

    @Test
    fun gameReviewEngine_generatesWeakPointAnalysis() {
        val board = Board()
        val moves = GameRules.getLegalMoves(board).take(4)
        val review = GameReviewEngine.reviewGame(moves, PieceColor.WHITE)

        assertNotNull(review.weakPointAnalysis.primaryWeakness)
        assertTrue(review.weakPointAnalysis.overallAccuracy in 30..100)
    }
}
