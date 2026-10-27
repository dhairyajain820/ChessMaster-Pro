package com.example.chess.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.Board
import com.example.chess.engine.GameRules
import com.example.chess.engine.Move
import com.example.chess.engine.PieceColor
import com.example.chess.engine.Square
import com.example.chess.puzzles.ChessPuzzle
import com.example.chess.puzzles.PuzzleRepository
import com.example.chess.ui.components.BoardTheme
import com.example.chess.ui.components.ChessBoardView
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PuzzleRushScreen(
    boardTheme: BoardTheme,
    onBackToTactics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allPuzzles = remember { PuzzleRepository.curatedPuzzles }

    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(12) }
    var strikes by remember { mutableIntStateOf(0) }
    var puzzleIndex by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    val currentPuzzle = allPuzzles[puzzleIndex % allPuzzles.size]
    val board = remember { Board() }
    var boardRevision by remember { mutableIntStateOf(0) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var moveIndexInSolution by remember { mutableIntStateOf(0) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var feedbackColor by remember { mutableStateOf(EmeraldAccent) }

    fun loadCurrentRushPuzzle() {
        val p = allPuzzles[puzzleIndex % allPuzzles.size]
        board.loadFen(p.fen)
        selectedSquare = null
        moveIndexInSolution = 0
        boardRevision++
    }

    LaunchedEffect(puzzleIndex) {
        loadCurrentRushPuzzle()
    }

    fun restartRush() {
        score = 0
        strikes = 0
        puzzleIndex = 0
        isGameOver = false
        feedbackMessage = null
        loadCurrentRushPuzzle()
    }

    val legalMovesForSelected = remember(selectedSquare, boardRevision) {
        if (selectedSquare != null) {
            GameRules.getLegalMoves(board).filter { it.from == selectedSquare }
        } else emptyList()
    }

    fun handleMove(move: Move) {
        val playedUci = move.uci.take(4)
        val expectedUci = currentPuzzle.solutionMovesUci.getOrNull(moveIndexInSolution)?.take(4)

        if (playedUci == expectedUci) {
            // Correct move!
            GameRules.makeMove(board, move)
            moveIndexInSolution++
            selectedSquare = null
            boardRevision++

            if (moveIndexInSolution >= currentPuzzle.solutionMovesUci.size) {
                // Puzzle passed!
                score++
                if (score > highScore) highScore = score
                feedbackMessage = "Correct! +1"
                feedbackColor = EmeraldAccent
                coroutineScope.launch {
                    delay(350)
                    feedbackMessage = null
                    puzzleIndex++
                }
            } else {
                // Auto opponent response
                coroutineScope.launch {
                    delay(300)
                    val oppUci = currentPuzzle.solutionMovesUci.getOrNull(moveIndexInSolution)
                    if (oppUci != null && oppUci.length >= 4) {
                        val fromSq = Square.fromAlgebraic(oppUci.substring(0, 2))
                        val toSq = Square.fromAlgebraic(oppUci.substring(2, 4))
                        if (fromSq != null && toSq != null) {
                            val oppMove = GameRules.getLegalMoves(board).find { it.from == fromSq && it.to == toSq }
                            if (oppMove != null) {
                                GameRules.makeMove(board, oppMove)
                                moveIndexInSolution++
                                boardRevision++
                            }
                        }
                    }
                    if (moveIndexInSolution >= currentPuzzle.solutionMovesUci.size) {
                        score++
                        if (score > highScore) highScore = score
                        feedbackMessage = "Passed! +1"
                        feedbackColor = EmeraldAccent
                        delay(350)
                        feedbackMessage = null
                        puzzleIndex++
                    }
                }
            }
        } else {
            // Strike!
            strikes++
            feedbackMessage = "Strike! ❌"
            feedbackColor = CrimsonAccent
            selectedSquare = null

            if (strikes >= 3) {
                isGameOver = true
            } else {
                coroutineScope.launch {
                    delay(600)
                    feedbackMessage = null
                    puzzleIndex++ // Advance to next puzzle upon strike
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Rush Header: Score, High Score, Strikes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = "Puzzle Rush",
                    tint = GoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Puzzle Rush",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "High Score: $highScore",
                        fontSize = 11.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Score Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$score",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PTS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3-Strikes Indicator (❌ ❌ ❌)
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val isStruck = index < strikes
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Strike",
                        tint = if (isStruck) CrimsonAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Feedback / Streak Toast
        feedbackMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = feedbackColor.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = msg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = feedbackColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Active Puzzle Board
        ChessBoardView(
            board = board,
            isFlipped = (currentPuzzle.playerColor == PieceColor.BLACK),
            selectedSquare = selectedSquare,
            legalMovesForSelected = legalMovesForSelected,
            boardTheme = boardTheme,
            onSquareClick = { clickedSquare ->
                if (isGameOver) return@ChessBoardView

                val piece = board.getPiece(clickedSquare)
                if (selectedSquare == null) {
                    if (piece != null && piece.color == board.activeColor) {
                        selectedSquare = clickedSquare
                    }
                } else {
                    val matchingMove = legalMovesForSelected.find { it.to == clickedSquare }
                    if (matchingMove != null) {
                        handleMove(matchingMove)
                    } else if (piece != null && piece.color == board.activeColor) {
                        selectedSquare = clickedSquare
                    } else {
                        selectedSquare = null
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBackToTactics,
                modifier = Modifier.testTag("back_to_tactics_button")
            ) {
                Text("Back to Training")
            }

            Button(
                onClick = { restartRush() },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.testTag("restart_rush_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restart Rush", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Game Over Dialog (3 Strikes)
    if (isGameOver) {
        AlertDialog(
            onDismissRequest = { isGameOver = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rush Complete!")
                }
            },
            text = {
                Column {
                    Text(
                        text = "You scored $score puzzles solved!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (score >= highScore) "🔥 New Personal Best!" else "High score remains $highScore. Great tactical sprint!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { restartRush() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onBackToTactics) {
                    Text("Done")
                }
            }
        )
    }
}
