package com.example.chess.ui.screens

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.chess.tournament.FideTournament
import com.example.chess.tournament.TournamentRepository
import com.example.chess.ui.components.BoardTheme
import com.example.chess.ui.components.ChessBoardView
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

@Composable
fun TournamentsScreen(
    boardTheme: BoardTheme,
    onAnalyzeGame: (List<Move>, PieceColor) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTournamentIndex by remember { mutableIntStateOf(0) }
    val tournament = TournamentRepository.fideTournaments[selectedTournamentIndex]

    // Parse featured game PGN to moves
    val parsedMoves = remember(tournament) {
        val board = Board()
        val movesList = mutableListOf<Move>()
        val tokens = tournament.featuredGamePgn.split("\\s+".toRegex())
        for (token in tokens) {
            val san = token.replace("^\\d+\\.".toRegex(), "").trim()
            if (san.isNotEmpty() && !san.contains(".")) {
                val legal = GameRules.getLegalMoves(board)
                val m = legal.find { it.san == san }
                if (m != null) {
                    GameRules.makeMove(board, m)
                    movesList.add(m)
                }
            }
        }
        movesList
    }

    val previewBoard = remember(parsedMoves) {
        val b = Board()
        for (m in parsedMoves.take(16)) {
            GameRules.makeMove(b, m)
        }
        b
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "FIDE Tournaments",
                tint = GoldPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "FIDE Tournaments Hub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "World Championship Cycle & Live Super-GM Events",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tournament Selector Horizontal Scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TournamentRepository.fideTournaments.forEachIndexed { index, tourney ->
                val isSelected = selectedTournamentIndex == index
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedTournamentIndex = index }
                ) {
                    Text(
                        text = tourney.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tournament Overview Card
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
                    Text(
                        text = tournament.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (tournament.status == "Live") EmeraldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = tournament.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tournament.status == "Live") EmeraldAccent else GoldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", modifier = Modifier.size(14.dp), tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tournament.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = "Dates", modifier = Modifier.size(14.dp), tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tournament.dates, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = "Prize", modifier = Modifier.size(14.dp), tint = EmeraldAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tournament.prizeFund, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tournament.summary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Standings Table Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Official Standings (${tournament.currentRound})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GoldPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("#  Player", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Fed   FIDE   Pts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                tournament.standings.forEach { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${player.rank}.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.width(22.dp)
                            )
                            Text(
                                text = "${player.title} ${player.name}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "${player.federation}   ${player.fideRating}   ${player.score}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Featured Tournament Game Viewer
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
                    Column {
                        Text(
                            text = "Featured Grandmaster Game",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = tournament.featuredGameTitle,
                            fontSize = 12.sp,
                            color = GoldPrimary
                        )
                    }

                    Button(
                        onClick = {
                            onAnalyzeGame(parsedMoves, PieceColor.WHITE)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        modifier = Modifier.testTag("analyze_gm_game_button")
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = "Analyze", modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analyze", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Board snapshot
                ChessBoardView(
                    board = previewBoard,
                    boardTheme = boardTheme,
                    onSquareClick = {}
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PGN: ${tournament.featuredGamePgn.take(120)}...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
