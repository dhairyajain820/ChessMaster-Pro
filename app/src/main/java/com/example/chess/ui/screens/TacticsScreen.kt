package com.example.chess.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.Board
import com.example.chess.engine.GameRules
import com.example.chess.engine.Move
import com.example.chess.engine.PieceColor
import com.example.chess.engine.Square
import com.example.chess.puzzles.ChessPuzzle
import com.example.chess.puzzles.PuzzleDifficulty
import com.example.chess.puzzles.PuzzleRepository
import com.example.chess.ui.components.BoardTheme
import com.example.chess.ui.components.ChessBoardView
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TacticsScreen(
    boardTheme: BoardTheme,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedDifficultyTab by remember { mutableIntStateOf(0) } // 0: Easy, 1: Medium, 2: Hard, 3: Online Daily

    val currentDifficulty = when (selectedDifficultyTab) {
        0 -> PuzzleDifficulty.EASY
        1 -> PuzzleDifficulty.MEDIUM
        else -> PuzzleDifficulty.HARD
    }

    var currentPuzzlesList by remember { mutableStateOf(PuzzleRepository.getPuzzlesByDifficulty(currentDifficulty)) }
    var currentPuzzleIndex by remember { mutableIntStateOf(0) }
    var currentPuzzle by remember { mutableStateOf(currentPuzzlesList.first()) }

    // Board & solving state
    val board = remember { Board() }
    var boardRevision by remember { mutableIntStateOf(0) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var currentMoveIndexInSolution by remember { mutableIntStateOf(0) }

    var isSolved by remember { mutableStateOf(false) }
    var isIncorrectMove by remember { mutableStateOf(false) }
    var isShowingHint by remember { mutableStateOf(false) }
    var isSolutionRevealed by remember { mutableStateOf(false) }
    var isFetchingOnline by remember { mutableStateOf(false) }
    var hintSquareToMove by remember { mutableStateOf<Move?>(null) }

    fun loadPuzzle(puzzle: ChessPuzzle) {
        currentPuzzle = puzzle
        board.loadFen(puzzle.fen)
        selectedSquare = null
        currentMoveIndexInSolution = 0
        isSolved = false
        isIncorrectMove = false
        isShowingHint = false
        isSolutionRevealed = false
        hintSquareToMove = null
        boardRevision++
    }

    LaunchedEffect(selectedDifficultyTab) {
        if (selectedDifficultyTab == 3) {
            // Online Daily
            coroutineScope.launch {
                isFetchingOnline = true
                val onlinePuzzle = PuzzleRepository.fetchDailyOnlinePuzzle()
                isFetchingOnline = false
                loadPuzzle(onlinePuzzle)
            }
        } else {
            val list = PuzzleRepository.getPuzzlesByDifficulty(currentDifficulty)
            currentPuzzlesList = list
            currentPuzzleIndex = 0
            loadPuzzle(list.first())
        }
    }

    val legalMovesForSelected = remember(selectedSquare, boardRevision) {
        if (selectedSquare != null) {
            GameRules.getLegalMoves(board).filter { it.from == selectedSquare }
        } else emptyList()
    }

    fun handleMove(move: Move) {
        val playedUci = move.uci.take(4)
        val expectedUci = currentPuzzle.solutionMovesUci.getOrNull(currentMoveIndexInSolution)?.take(4)

        if (playedUci == expectedUci) {
            // Correct move!
            GameRules.makeMove(board, move)
            currentMoveIndexInSolution++
            selectedSquare = null
            isIncorrectMove = false
            hintSquareToMove = null
            boardRevision++

            if (currentMoveIndexInSolution >= currentPuzzle.solutionMovesUci.size) {
                // Completed!
                isSolved = true
            } else {
                // Play opponent automated response
                coroutineScope.launch {
                    delay(500)
                    val oppUci = currentPuzzle.solutionMovesUci.getOrNull(currentMoveIndexInSolution)
                    if (oppUci != null && oppUci.length >= 4) {
                        val fromSq = Square.fromAlgebraic(oppUci.substring(0, 2))
                        val toSq = Square.fromAlgebraic(oppUci.substring(2, 4))
                        if (fromSq != null && toSq != null) {
                            val oppMove = GameRules.getLegalMoves(board).find { it.from == fromSq && it.to == toSq }
                            if (oppMove != null) {
                                GameRules.makeMove(board, oppMove)
                                currentMoveIndexInSolution++
                                boardRevision++
                            }
                        }
                    }
                    if (currentMoveIndexInSolution >= currentPuzzle.solutionMovesUci.size) {
                        isSolved = true
                    }
                }
            }
        } else {
            // Wrong move
            isIncorrectMove = true
            selectedSquare = null
        }
    }

    var isPuzzleRushActive by remember { mutableStateOf(false) }

    if (isPuzzleRushActive) {
        PuzzleRushScreen(
            boardTheme = boardTheme,
            onBackToTactics = { isPuzzleRushActive = false },
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Banner: Training vs Puzzle Rush
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tactics Trainer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Solve master positions by theme",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = { isPuzzleRushActive = true },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = GoldPrimary.copy(alpha = 0.2f)),
                modifier = Modifier.testTag("launch_puzzle_rush_button")
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = "Puzzle Rush", tint = GoldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Puzzle Rush", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Difficulty Tabs
        TabRow(
            selectedTabIndex = selectedDifficultyTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            val tabs = listOf("Easy", "Medium", "Hard", "Online Daily")
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedDifficultyTab == index,
                    onClick = { selectedDifficultyTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedDifficultyTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Puzzle Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentPuzzle.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Theme: ${currentPuzzle.theme}",
                        fontSize = 12.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${if (currentPuzzle.playerColor == PieceColor.WHITE) "White" else "Black"} to Move",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (selectedDifficultyTab != 3) {
                IconButton(
                    onClick = {
                        val nextIdx = (currentPuzzleIndex + 1) % currentPuzzlesList.size
                        currentPuzzleIndex = nextIdx
                        loadPuzzle(currentPuzzlesList[nextIdx])
                    },
                    modifier = Modifier.testTag("next_puzzle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NavigateNext,
                        contentDescription = "Next Puzzle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isFetchingOnline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        } else {
            // Interactive Board
            ChessBoardView(
                board = board,
                isFlipped = (currentPuzzle.playerColor == PieceColor.BLACK),
                selectedSquare = selectedSquare,
                legalMovesForSelected = legalMovesForSelected,
                highlightArrowMove = hintSquareToMove,
                boardTheme = boardTheme,
                onSquareClick = { clickedSquare ->
                    if (isSolved || isSolutionRevealed) return@ChessBoardView

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
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Feedback Banner
        if (isIncorrectMove) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CrimsonAccent.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Not the best move! Try finding a more forcing continuation.",
                        color = CrimsonAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isSolved) {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Puzzle Solved! Excellent calculation.",
                            color = EmeraldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "You found the winning master combination.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Action Buttons: Hint, Reveal Solution, Next
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(
                onClick = {
                    isShowingHint = true
                    val nextExpectedUci = currentPuzzle.solutionMovesUci.getOrNull(currentMoveIndexInSolution)
                    if (nextExpectedUci != null && nextExpectedUci.length >= 4) {
                        val fromSq = Square.fromAlgebraic(nextExpectedUci.substring(0, 2))
                        val toSq = Square.fromAlgebraic(nextExpectedUci.substring(2, 4))
                        if (fromSq != null && toSq != null) {
                            hintSquareToMove = Move(fromSq, toSq)
                        }
                    }
                },
                enabled = !isSolved,
                modifier = Modifier.testTag("puzzle_hint_button")
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Hint", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hint")
            }

            OutlinedButton(
                onClick = {
                    isSolutionRevealed = true
                    // Play all solution moves on the board
                    board.loadFen(currentPuzzle.fen)
                    for (uci in currentPuzzle.solutionMovesUci) {
                        if (uci.length >= 4) {
                            val fromSq = Square.fromAlgebraic(uci.substring(0, 2))
                            val toSq = Square.fromAlgebraic(uci.substring(2, 4))
                            if (fromSq != null && toSq != null) {
                                val m = GameRules.getLegalMoves(board).find { it.from == fromSq && it.to == toSq }
                                if (m != null) GameRules.makeMove(board, m)
                            }
                        }
                    }
                    boardRevision++
                },
                modifier = Modifier.testTag("reveal_solution_button")
            ) {
                Icon(Icons.Default.Help, contentDescription = "Solution", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Solution")
            }

            Button(
                onClick = {
                    if (selectedDifficultyTab == 3) {
                        coroutineScope.launch {
                            isFetchingOnline = true
                            val onlinePuzzle = PuzzleRepository.fetchDailyOnlinePuzzle()
                            isFetchingOnline = false
                            loadPuzzle(onlinePuzzle)
                        }
                    } else {
                        val nextIdx = (currentPuzzleIndex + 1) % currentPuzzlesList.size
                        currentPuzzleIndex = nextIdx
                        loadPuzzle(currentPuzzlesList[nextIdx])
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.testTag("puzzle_next_button")
            ) {
                Text("Next", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hint Card if active
        if (isShowingHint) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 Coach Hint",
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentPuzzle.hint,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Winning Strategy & Key Moves Explanation Card (always visible when solved or revealed)
        if (isSolved || isSolutionRevealed) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Winning Strategy",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Winning Strategy & Key Moves",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentPuzzle.winningStrategy,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Master Line: ${currentPuzzle.solutionMovesSan.joinToString(" ")}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldAccent
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentPuzzle.keyMovesExplanation,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
