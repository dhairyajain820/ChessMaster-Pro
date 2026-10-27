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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.chess.review.FullGameReview
import com.example.chess.review.GameReviewEngine
import com.example.chess.review.MoveClassification
import com.example.chess.ui.components.BoardTheme
import com.example.chess.ui.components.CentipawnEvalBar
import com.example.chess.ui.components.ChessBoardView
import com.example.ui.theme.*

@Composable
fun ReviewScreen(
    movesToReview: List<Move>,
    playerColor: PieceColor,
    boardTheme: BoardTheme,
    onNavigateToTactics: () -> Unit,
    onNavigateToOpenings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate full review
    val review: FullGameReview = remember(movesToReview, playerColor) {
        val sampleMoves = if (movesToReview.isNotEmpty()) {
            movesToReview
        } else {
            // Default sample famous game for review demonstration
            val defaultSanList = listOf(
                "e4", "e5", "Nf3", "Nc6", "Bc4", "Bc5", "b4", "Bxb4", "c3", "Ba5",
                "d4", "exd4", "O-O", "d3", "Qb3", "Qf6", "e5", "Qg6", "Re1", "Nge7"
            )
            val b = Board()
            val list = mutableListOf<Move>()
            for (san in defaultSanList) {
                val legal = GameRules.getLegalMoves(b)
                val m = legal.find { it.san == san } ?: legal.firstOrNull()
                if (m != null) {
                    GameRules.makeMove(b, m)
                    list.add(m)
                }
            }
            list
        }
        GameReviewEngine.reviewGame(sampleMoves, playerColor)
    }

    var currentPlyIndex by remember { mutableIntStateOf(0) } // 0 = initial position, 1 = after 1st move, etc.
    val currentBoard = remember { Board() }

    fun updateBoardToPly(targetPly: Int) {
        currentBoard.setupInitialPosition()
        for (i in 0 until targetPly.coerceAtMost(review.items.size)) {
            val item = review.items[i]
            GameRules.makeMove(currentBoard, item.playedMove)
        }
        currentPlyIndex = targetPly
    }

    LaunchedEffect(currentPlyIndex) {
        updateBoardToPly(currentPlyIndex)
    }

    val currentItem = if (currentPlyIndex > 0 && currentPlyIndex <= review.items.size) {
        review.items[currentPlyIndex - 1]
    } else null

    val currentEval = currentItem?.evalAfter ?: 20
    val analysis = review.weakPointAnalysis

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header: Accuracy Scores
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your Accuracy",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${analysis.overallAccuracy}%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (analysis.overallAccuracy >= 80) EmeraldAccent else GoldPrimary
                    )
                    val estimatedElo = (600 + (analysis.overallAccuracy * 22)).coerceIn(800, 2650)
                    Text(
                        text = "Est. Performance: ~$estimatedElo ELO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Opponent Accuracy",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${analysis.opponentAccuracy}%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Engine Rating: ~1800",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Weak Points Diagnostic Card (AI Coach Insights)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Coach Diagnostic",
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Weak Point Diagnostic",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CrimsonAccent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = analysis.weakPointCategory,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Identified Flaw: ${analysis.primaryWeakness}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonAccent
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = analysis.detailedAdvice,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Move breakdown badges: Blunders, Mistakes, Inaccuracies, Best
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MoveStatBadge("??", "${analysis.blundersCount} Blunders", CrimsonAccent)
                    MoveStatBadge("?", "${analysis.mistakesCount} Mistakes", Color(0xFFF97316))
                    MoveStatBadge("?!", "${analysis.inaccuraciesCount} Inacc.", GoldPrimary)
                    MoveStatBadge("*", "${analysis.bestMovesCount} Best", EmeraldAccent)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actionable Practice Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToTactics,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("practice_tactics_button")
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = "Drill", modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Practice Tactics", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateToOpenings,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("study_openings_button")
                    ) {
                        Icon(Icons.Default.School, contentDescription = "Openings", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Study Openings", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Evaluation Bar
        CentipawnEvalBar(
            evalScoreCentipawns = currentEval,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Review Board
        val lastPlayedMove = currentItem?.playedMove
        val bestEngineMove = currentItem?.bestEngineMove
        ChessBoardView(
            board = currentBoard,
            isFlipped = (playerColor == PieceColor.BLACK),
            lastMove = lastPlayedMove,
            highlightArrowMove = bestEngineMove,
            boardTheme = boardTheme,
            onSquareClick = {}
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Step Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { updateBoardToPly(0) },
                enabled = currentPlyIndex > 0
            ) {
                Icon(Icons.Default.FastRewind, contentDescription = "Start")
            }

            IconButton(
                onClick = { if (currentPlyIndex > 0) updateBoardToPly(currentPlyIndex - 1) },
                enabled = currentPlyIndex > 0
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
            }

            Text(
                text = if (currentPlyIndex == 0) "Start" else "Move $currentPlyIndex / ${review.items.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            IconButton(
                onClick = { if (currentPlyIndex < review.items.size) updateBoardToPly(currentPlyIndex + 1) },
                enabled = currentPlyIndex < review.items.size
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }

            IconButton(
                onClick = { updateBoardToPly(review.items.size) },
                enabled = currentPlyIndex < review.items.size
            ) {
                Icon(Icons.Default.FastForward, contentDescription = "End")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Move Evaluation Details Card
        currentItem?.let { item ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val badgeColor = when (item.classification) {
                                MoveClassification.BRILLIANT -> BlueAccent
                                MoveClassification.BEST -> EmeraldAccent
                                MoveClassification.EXCELLENT -> EmeraldAccent
                                MoveClassification.GOOD -> Color(0xFF84CC16)
                                MoveClassification.INACCURACY -> GoldPrimary
                                MoveClassification.MISTAKE -> Color(0xFFF97316)
                                MoveClassification.BLUNDER -> CrimsonAccent
                                MoveClassification.BOOK -> GoldSecondary
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeColor
                            ) {
                                Text(
                                    text = item.classification.symbol,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${item.moveNumber}. ${if (!item.isWhite) "... " else ""}${item.playedMove.san} (${item.classification.displayName})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        item.bestEngineMove?.let { best ->
                            Text(
                                text = "Best: ${best.san}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.comment,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Jump to next blunder button
        FilledTonalButton(
            onClick = {
                val nextBlunderIndex = review.items.indexOfFirst {
                    it.ply > currentPlyIndex &&
                            (it.classification == MoveClassification.BLUNDER || it.classification == MoveClassification.MISTAKE)
                }
                if (nextBlunderIndex != -1) {
                    updateBoardToPly(nextBlunderIndex + 1)
                } else {
                    // Loop back to first blunder
                    val first = review.items.indexOfFirst {
                        it.classification == MoveClassification.BLUNDER || it.classification == MoveClassification.MISTAKE
                    }
                    if (first != -1) updateBoardToPly(first + 1)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("jump_to_blunder_button")
        ) {
            Icon(Icons.Default.BugReport, contentDescription = "Blunders", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Jump to Next Mistake / Blunder")
        }
    }
}

@Composable
fun MoveStatBadge(symbol: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
