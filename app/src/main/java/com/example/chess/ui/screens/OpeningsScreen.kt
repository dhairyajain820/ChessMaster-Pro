package com.example.chess.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.Board
import com.example.chess.engine.GameRules
import com.example.chess.engine.Move
import com.example.chess.engine.PieceColor
import com.example.chess.engine.Square
import com.example.chess.openings.ChessOpening
import com.example.chess.openings.OpeningMoveStep
import com.example.chess.openings.OpeningRepertoire
import com.example.chess.ui.components.BoardTheme
import com.example.chess.ui.components.ChessBoardView
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OpeningsScreen(
    boardTheme: BoardTheme,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedOpeningIndex by remember { mutableIntStateOf(0) }
    val opening = OpeningRepertoire.allOpenings[selectedOpeningIndex]

    val board = remember { Board() }
    var boardRevision by remember { mutableIntStateOf(0) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var currentPlyIndex by remember { mutableIntStateOf(0) }

    var latestStepExplanation by remember { mutableStateOf<OpeningMoveStep?>(null) }
    var deviationMessage by remember { mutableStateOf<String?>(null) }
    var coachArrowMove by remember { mutableStateOf<Move?>(null) }
    var isLineCompleted by remember { mutableStateOf(false) }

    fun resetOpeningLine(op: ChessOpening) {
        board.setupInitialPosition()
        selectedSquare = null
        currentPlyIndex = 0
        latestStepExplanation = null
        deviationMessage = null
        coachArrowMove = null
        isLineCompleted = false
        boardRevision++

        // If player side is Black, White makes the first move automatically
        if (op.playerSide == PieceColor.BLACK) {
            coroutineScope.launch {
                delay(300)
                val firstWhiteStep = op.mainLine.explanations.firstOrNull()
                if (firstWhiteStep != null && firstWhiteStep.moveUci.length >= 4) {
                    val fromSq = Square.fromAlgebraic(firstWhiteStep.moveUci.substring(0, 2))
                    val toSq = Square.fromAlgebraic(firstWhiteStep.moveUci.substring(2, 4))
                    if (fromSq != null && toSq != null) {
                        val m = GameRules.getLegalMoves(board).find { it.from == fromSq && it.to == toSq }
                        if (m != null) {
                            GameRules.makeMove(board, m)
                            currentPlyIndex = 1
                            latestStepExplanation = firstWhiteStep
                            boardRevision++
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedOpeningIndex) {
        resetOpeningLine(opening)
    }

    val legalMovesForSelected = remember(selectedSquare, boardRevision) {
        if (selectedSquare != null) {
            GameRules.getLegalMoves(board).filter { it.from == selectedSquare }
        } else emptyList()
    }

    fun handlePlayerMove(move: Move) {
        val expectedStep = opening.mainLine.explanations.getOrNull(currentPlyIndex)
        val expectedUci = expectedStep?.moveUci?.take(4)
        val playedUci = move.uci.take(4)

        if (playedUci == expectedUci) {
            // Book Move!
            deviationMessage = null
            coachArrowMove = null
            latestStepExplanation = expectedStep
            GameRules.makeMove(board, move)
            currentPlyIndex++
            selectedSquare = null
            boardRevision++

            if (currentPlyIndex >= opening.mainLine.explanations.size) {
                isLineCompleted = true
            } else {
                // Play opponent reply from the book
                coroutineScope.launch {
                    delay(550)
                    val nextStep = opening.mainLine.explanations.getOrNull(currentPlyIndex)
                    if (nextStep != null && nextStep.moveUci.length >= 4) {
                        val fromSq = Square.fromAlgebraic(nextStep.moveUci.substring(0, 2))
                        val toSq = Square.fromAlgebraic(nextStep.moveUci.substring(2, 4))
                        if (fromSq != null && toSq != null) {
                            val oppMove = GameRules.getLegalMoves(board).find { it.from == fromSq && it.to == toSq }
                            if (oppMove != null) {
                                GameRules.makeMove(board, oppMove)
                                currentPlyIndex++
                                latestStepExplanation = nextStep
                                boardRevision++
                            }
                        }
                    }
                    if (currentPlyIndex >= opening.mainLine.explanations.size) {
                        isLineCompleted = true
                    }
                }
            }
        } else {
            // Deviation from book
            val rec = expectedStep?.moveSan ?: "book move"
            deviationMessage = "Out of Book: In the ${opening.name}, the main continuation is $rec. Try again to master the repertoire!"
            expectedStep?.let { step ->
                if (step.moveUci.length >= 4) {
                    val fromSq = Square.fromAlgebraic(step.moveUci.substring(0, 2))
                    val toSq = Square.fromAlgebraic(step.moveUci.substring(2, 4))
                    if (fromSq != null && toSq != null) {
                        coachArrowMove = Move(fromSq, toSq)
                    }
                }
            }
            selectedSquare = null
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
        // Opening Selection Chips
        Text(
            text = "Choose Opening Repertoire",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OpeningRepertoire.allOpenings.forEachIndexed { index, op ->
                val isSelected = selectedOpeningIndex == index
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedOpeningIndex = index }
                ) {
                    Text(
                        text = "${op.eco}: ${op.name.take(18)}..",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Opening Header Info
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${opening.name} (${opening.eco})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Play as ${if (opening.playerSide == PieceColor.WHITE) "White" else "Black"} • ${opening.difficulty}",
                            fontSize = 12.sp,
                            color = GoldPrimary
                        )
                    }

                    FilledTonalButton(
                        onClick = { resetOpeningLine(opening) },
                        modifier = Modifier.testTag("reset_opening_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restart")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = opening.shortSummary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Board
        ChessBoardView(
            board = board,
            isFlipped = (opening.playerSide == PieceColor.BLACK),
            selectedSquare = selectedSquare,
            legalMovesForSelected = legalMovesForSelected,
            highlightArrowMove = coachArrowMove,
            boardTheme = boardTheme,
            onSquareClick = { clickedSquare ->
                if (isLineCompleted) return@ChessBoardView

                val piece = board.getPiece(clickedSquare)
                if (selectedSquare == null) {
                    if (piece != null && piece.color == board.activeColor) {
                        selectedSquare = clickedSquare
                    }
                } else {
                    val matchingMove = legalMovesForSelected.find { it.to == clickedSquare }
                    if (matchingMove != null) {
                        handlePlayerMove(matchingMove)
                    } else if (piece != null && piece.color == board.activeColor) {
                        selectedSquare = clickedSquare
                    } else {
                        selectedSquare = null
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Feedback / Deviation Alert
        if (deviationMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CrimsonAccent.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Deviation", tint = CrimsonAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = deviationMessage!!,
                        color = CrimsonAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isLineCompleted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = EmeraldAccent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Main line mastered! You have memorized the core moves and ideas.",
                        color = EmeraldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Live Move Explanation Card
        latestStepExplanation?.let { step ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${step.ply}. ${step.moveSan}: ${step.moveTitle}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GoldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step.explanation,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Strategic Themes & Master Traps
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Key Strategic Plans",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                opening.strategicThemes.forEach { theme ->
                    Text(
                        text = "• $theme",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Traps to Watch Out For",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CrimsonAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                opening.commonTraps.forEach { trap ->
                    Text(
                        text = "⚠️ $trap",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
