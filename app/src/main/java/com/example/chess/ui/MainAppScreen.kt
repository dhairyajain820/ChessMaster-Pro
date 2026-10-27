package com.example.chess.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.Move
import com.example.chess.engine.PieceColor
import com.example.chess.ui.components.BoardTheme
import com.example.chess.ui.screens.OpeningsScreen
import com.example.chess.ui.screens.PlayScreen
import com.example.chess.ui.screens.ReviewScreen
import com.example.chess.ui.screens.TacticsScreen
import com.example.chess.ui.screens.TournamentsScreen
import com.example.ui.theme.GoldPrimary

enum class AppNavDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PLAY("Play", Icons.Default.SportsEsports),
    TACTICS("Tactics", Icons.Default.Psychology),
    OPENINGS("Openings", Icons.Default.AutoStories),
    REVIEW("Review", Icons.Default.Assessment),
    FIDE("FIDE", Icons.Default.EmojiEvents)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    var currentTab by remember { mutableStateOf(AppNavDestination.PLAY) }
    var selectedBoardTheme by remember { mutableStateOf(BoardTheme.TOURNAMENT_GREEN) }

    // State passed into ReviewScreen when triggered from Play or Tournaments
    var reviewMovesList by remember { mutableStateOf<List<Move>>(emptyList()) }
    var reviewPlayerColor by remember { mutableStateOf(PieceColor.WHITE) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Grandmaster Chess",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Dark / Light Mode Toggle Button
                    IconButton(
                        onClick = onToggleDarkTheme,
                        modifier = Modifier.testTag("theme_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = GoldPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                AppNavDestination.values().forEach { destination ->
                    val isSelected = currentTab == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavDestination.PLAY -> {
                    PlayScreen(
                        boardTheme = selectedBoardTheme,
                        onSelectBoardTheme = { selectedBoardTheme = it },
                        onStartGameReview = { moves, color ->
                            reviewMovesList = moves
                            reviewPlayerColor = color
                            currentTab = AppNavDestination.REVIEW
                        }
                    )
                }
                AppNavDestination.TACTICS -> {
                    TacticsScreen(
                        boardTheme = selectedBoardTheme
                    )
                }
                AppNavDestination.OPENINGS -> {
                    OpeningsScreen(
                        boardTheme = selectedBoardTheme
                    )
                }
                AppNavDestination.REVIEW -> {
                    ReviewScreen(
                        movesToReview = reviewMovesList,
                        playerColor = reviewPlayerColor,
                        boardTheme = selectedBoardTheme,
                        onNavigateToTactics = { currentTab = AppNavDestination.TACTICS },
                        onNavigateToOpenings = { currentTab = AppNavDestination.OPENINGS }
                    )
                }
                AppNavDestination.FIDE -> {
                    TournamentsScreen(
                        boardTheme = selectedBoardTheme,
                        onAnalyzeGame = { moves, color ->
                            reviewMovesList = moves
                            reviewPlayerColor = color
                            currentTab = AppNavDestination.REVIEW
                        }
                    )
                }
            }
        }
    }
}
