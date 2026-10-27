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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.ai.AdvancedChessEngine
import com.example.chess.ai.ChessComBots
import com.example.chess.ai.EngineLevel
import com.example.chess.data.GameRecordEntity
import com.example.chess.data.sync.FirestoreSyncManager
import com.example.chess.engine.Board
import com.example.chess.engine.GameRules
import com.example.chess.engine.Move
import com.example.chess.engine.Piece
import com.example.chess.engine.PieceColor
import com.example.chess.engine.PieceType
import com.example.chess.engine.Square
import com.example.chess.ui.components.BoardTheme
import com.example.chess.ui.components.CentipawnEvalBar
import com.example.chess.ui.components.ChessBoardView
import com.example.chess.ui.components.CloudSyncHeader
import com.example.chess.ui.components.PlayerHeaderCard
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class GamePlayMode(val title: String) {
    VS_BOT("Chess.com Bots"),
    VS_AI("Stockfish Engine"),
    PASS_AND_PLAY("Pass & Play (Local)")
}

@Composable
fun PlayScreen(
    boardTheme: BoardTheme,
    onSelectBoardTheme: (BoardTheme) -> Unit,
    onStartGameReview: (List<Move>, PieceColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncManager = remember { FirestoreSyncManager(context) }

    // Game state
    val board = remember { Board() }
    var boardRevision by remember { mutableIntStateOf(0) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    val movesHistory = remember { mutableStateListOf<Move>() }
    val boardHistory = remember { mutableStateListOf<Board>() }
    val capturedByWhite = remember { mutableStateListOf<Piece>() }
    val capturedByBlack = remember { mutableStateListOf<Piece>() }

    // Settings & Personalities
    var playMode by remember { mutableStateOf(GamePlayMode.VS_BOT) }
    var selectedBot by remember { mutableStateOf(ChessComBots.allBots[1]) } // Nelson (1300)
    var botDialogue by remember { mutableStateOf(selectedBot.openingQuote) }
    var engineLevel by remember { mutableStateOf(EngineLevel.INTERMEDIATE) }
    var playerColor by remember { mutableStateOf(PieceColor.WHITE) }
    var isBoardFlipped by remember { mutableStateOf(false) }
    var isEngineThinking by remember { mutableStateOf(false) }
    var currentEvalCentipawns by remember { mutableIntStateOf(20) }
    var suggestedHintMove by remember { mutableStateOf<Move?>(null) }

    // Game status
    var gameOverDialogVisible by remember { mutableStateOf(false) }
    var gameOverMessage by remember { mutableStateOf("") }
    var showPromotionDialog by remember { mutableStateOf<Pair<Square, Square>?>(null) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val activeEngineLevel = if (playMode == GamePlayMode.VS_BOT) selectedBot.engineLevel else engineLevel

    fun resetGame() {
        board.setupInitialPosition()
        selectedSquare = null
        movesHistory.clear()
        boardHistory.clear()
        capturedByWhite.clear()
        capturedByBlack.clear()
        boardRevision++
        currentEvalCentipawns = 20
        suggestedHintMove = null
        botDialogue = selectedBot.openingQuote
        isBoardFlipped = (playerColor == PieceColor.BLACK)

        // If player is Black, opponent moves first
        if (playMode != GamePlayMode.PASS_AND_PLAY && playerColor == PieceColor.BLACK) {
            coroutineScope.launch {
                isEngineThinking = true
                delay(400)
                val eval = withContext(Dispatchers.Default) {
                    AdvancedChessEngine.findBestMove(board, activeEngineLevel)
                }
                eval.bestMove?.let { aiMove ->
                    aiMove.capturedPiece?.let { cap -> capturedByWhite.add(cap) }
                    boardHistory.add(board.clone())
                    GameRules.makeMove(board, aiMove)
                    movesHistory.add(aiMove)
                    currentEvalCentipawns = AdvancedChessEngine.getWhitePerspectiveEval(board)
                }
                isEngineThinking = false
                boardRevision++
            }
        }
    }

    LaunchedEffect(playerColor, playMode, selectedBot) {
        resetGame()
    }

    // Material count
    val whiteVal = capturedByWhite.sumOf { it.type.baseValue }
    val blackVal = capturedByBlack.sumOf { it.type.baseValue }
    val whiteAdv = ((whiteVal - blackVal) / 100).coerceAtLeast(0)
    val blackAdv = ((blackVal - whiteVal) / 100).coerceAtLeast(0)

    val lastMove = movesHistory.lastOrNull()
    val legalMovesForSelected = remember(selectedSquare, boardRevision) {
        if (selectedSquare != null) {
            GameRules.getLegalMoves(board).filter { it.from == selectedSquare }
        } else emptyList()
    }

    fun persistGameFinished(result: String) {
        coroutineScope.launch {
            val record = GameRecordEntity(
                opponentName = if (playMode == GamePlayMode.VS_BOT) selectedBot.name else "Engine ${engineLevel.displayName}",
                engineLevel = activeEngineLevel.name,
                playerColor = playerColor.name,
                gameResult = result,
                movesSan = movesHistory.joinToString(" ") { it.san },
                movesCount = movesHistory.size,
                playerAccuracy = 82, // Calculated in review
                primaryWeakness = "Middlegame Tactics",
                pgn = movesHistory.joinToString(" ") { it.san }
            )
            syncManager.saveGameLocallyAndSync(record)
        }
    }

    fun checkGameOverConditions() {
        if (GameRules.isCheckmate(board)) {
            val winner = board.activeColor.opposite
            gameOverMessage = "Checkmate! $winner wins the game."
            gameOverDialogVisible = true
            val res = if (winner == playerColor) "WIN" else "LOSS"
            persistGameFinished(res)
        } else if (GameRules.isStalemate(board)) {
            gameOverMessage = "Stalemate! Game drawn."
            gameOverDialogVisible = true
            persistGameFinished("DRAW")
        } else if (GameRules.isInsufficientMaterial(board)) {
            gameOverMessage = "Draw by insufficient material."
            gameOverDialogVisible = true
            persistGameFinished("DRAW")
        } else if (board.halfmoveClock >= 100) {
            gameOverMessage = "Draw by 50-move rule."
            gameOverDialogVisible = true
            persistGameFinished("DRAW")
        }
    }

    fun executePlayerMove(move: Move) {
        val movingPiece = board.getPiece(move.from) ?: return
        val isPromo = movingPiece.type == PieceType.PAWN &&
                ((movingPiece.color == PieceColor.WHITE && move.to.row == 0) ||
                        (movingPiece.color == PieceColor.BLACK && move.to.row == 7))

        if (isPromo && move.promotion == null) {
            showPromotionDialog = Pair(move.from, move.to)
            return
        }

        move.capturedPiece?.let {
            if (movingPiece.color == PieceColor.WHITE) capturedByWhite.add(it)
            else capturedByBlack.add(it)
        }

        boardHistory.add(board.clone())
        GameRules.makeMove(board, move)
        movesHistory.add(move)
        selectedSquare = null
        suggestedHintMove = null
        boardRevision++

        // Bot speech reaction
        if (playMode == GamePlayMode.VS_BOT) {
            botDialogue = selectedBot.inGameQuotes.random()
        }

        coroutineScope.launch {
            currentEvalCentipawns = withContext(Dispatchers.Default) {
                AdvancedChessEngine.getWhitePerspectiveEval(board)
            }
        }

        checkGameOverConditions()

        // Bot / AI response
        if (!gameOverDialogVisible && playMode != GamePlayMode.PASS_AND_PLAY && board.activeColor != playerColor) {
            coroutineScope.launch {
                isEngineThinking = true
                delay(350)
                val eval = withContext(Dispatchers.Default) {
                    AdvancedChessEngine.findBestMove(board, activeEngineLevel)
                }
                eval.bestMove?.let { aiMove ->
                    aiMove.capturedPiece?.let { cap ->
                        if (board.activeColor == PieceColor.WHITE) capturedByWhite.add(cap)
                        else capturedByBlack.add(cap)
                    }
                    boardHistory.add(board.clone())
                    GameRules.makeMove(board, aiMove)
                    movesHistory.add(aiMove)
                    currentEvalCentipawns = AdvancedChessEngine.getWhitePerspectiveEval(board)
                }
                isEngineThinking = false
                boardRevision++
                checkGameOverConditions()
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
        // Room to Cloud Sync Bar
        CloudSyncHeader(syncManager = syncManager)

        Spacer(modifier = Modifier.height(10.dp))

        // Mode Selector: Chess.com Bots vs Engine vs Pass & Play
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (playMode == GamePlayMode.VS_BOT) "${selectedBot.avatarEmoji} ${selectedBot.name} (${selectedBot.elo})" else playMode.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (playMode == GamePlayMode.VS_BOT) selectedBot.personality else "${engineLevel.displayName} (~${engineLevel.elo} ELO)",
                    fontSize = 12.sp,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Row {
                IconButton(
                    onClick = { showThemeMenu = true },
                    modifier = Modifier.testTag("theme_button")
                ) {
                    Icon(Icons.Default.ColorLens, contentDescription = "Board Theme", tint = MaterialTheme.colorScheme.primary)
                }

                DropdownMenu(expanded = showThemeMenu, onDismissRequest = { showThemeMenu = false }) {
                    BoardTheme.values().forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme.displayName) },
                            onClick = {
                                onSelectBoardTheme(theme)
                                showThemeMenu = false
                            }
                        )
                    }
                }

                IconButton(
                    onClick = { isBoardFlipped = !isBoardFlipped },
                    modifier = Modifier.testTag("flip_board_button")
                ) {
                    Icon(Icons.Default.Cached, contentDescription = "Flip Board", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mode Toggle Tabs (Bots vs Engine vs Pass & Play)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GamePlayMode.values().forEach { mode ->
                val isSelected = playMode == mode
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { playMode = mode }
                ) {
                    Text(
                        text = mode.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chess.com Bot Carousel if VS_BOT mode
        if (playMode == GamePlayMode.VS_BOT) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChessComBots.allBots.forEach { bot ->
                    val isBotSelected = selectedBot.id == bot.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isBotSelected) GoldPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedBot = bot }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = bot.avatarEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = bot.name,
                                    fontWeight = if (isBotSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (isBotSelected) GoldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${bot.elo} ELO",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bot Dialogue Speech Bubble (Iconic Chess.com dialogue)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedBot.avatarEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\"$botDialogue\"",
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Evaluation Bar
        CentipawnEvalBar(
            evalScoreCentipawns = currentEvalCentipawns,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Opponent (Top) Header Card
        val topIsWhite = isBoardFlipped
        val oppName = if (playMode == GamePlayMode.VS_BOT) "${selectedBot.name} (${selectedBot.elo})" else if (playMode == GamePlayMode.VS_AI) "Stockfish (${engineLevel.displayName})" else (if (topIsWhite) "White" else "Black")
        PlayerHeaderCard(
            playerName = oppName,
            playerRating = if (playMode == GamePlayMode.VS_BOT) "${selectedBot.elo}" else if (topIsWhite) "White" else "${engineLevel.elo}",
            isCurrentTurn = board.activeColor == (if (topIsWhite) PieceColor.WHITE else PieceColor.BLACK),
            capturedPieces = if (topIsWhite) capturedByWhite else capturedByBlack,
            materialAdvantage = if (topIsWhite) whiteAdv else blackAdv,
            timeRemainingSec = null,
            isWhite = topIsWhite
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Chess Board with Neo Pieces
        ChessBoardView(
            board = board,
            isFlipped = isBoardFlipped,
            selectedSquare = selectedSquare,
            legalMovesForSelected = legalMovesForSelected,
            lastMove = lastMove,
            highlightArrowMove = suggestedHintMove,
            boardTheme = boardTheme,
            onSquareClick = { clickedSquare ->
                if (isEngineThinking) return@ChessBoardView

                val pieceOnSquare = board.getPiece(clickedSquare)
                if (selectedSquare == null) {
                    if (pieceOnSquare != null && pieceOnSquare.color == board.activeColor) {
                        selectedSquare = clickedSquare
                    }
                } else {
                    val matchingMove = legalMovesForSelected.find { it.to == clickedSquare }
                    if (matchingMove != null) {
                        executePlayerMove(matchingMove)
                    } else if (pieceOnSquare != null && pieceOnSquare.color == board.activeColor) {
                        selectedSquare = clickedSquare
                    } else {
                        selectedSquare = null
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Player (Bottom) Header Card
        val bottomIsWhite = !isBoardFlipped
        PlayerHeaderCard(
            playerName = if (playMode == GamePlayMode.PASS_AND_PLAY) (if (bottomIsWhite) "White" else "Black") else "You",
            playerRating = if (bottomIsWhite) "White" else "Black",
            isCurrentTurn = board.activeColor == (if (bottomIsWhite) PieceColor.WHITE else PieceColor.BLACK),
            capturedPieces = if (bottomIsWhite) capturedByWhite else capturedByBlack,
            materialAdvantage = if (bottomIsWhite) whiteAdv else blackAdv,
            timeRemainingSec = null,
            isWhite = bottomIsWhite
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Move History Strip
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Moves: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                if (movesHistory.isEmpty()) {
                    Text("Game just started", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    movesHistory.chunked(2).forEachIndexed { moveIndex, pair ->
                        val moveNum = moveIndex + 1
                        val wSan = pair.firstOrNull()?.san ?: ""
                        val bSan = pair.getOrNull(1)?.san ?: ""
                        Text(
                            text = "$moveNum. $wSan ${if (bSan.isNotEmpty()) bSan else ""}  ",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons: Undo, Hint, Review Game, Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(
                onClick = {
                    if (boardHistory.size >= 1 && movesHistory.isNotEmpty()) {
                        val undoCount = if (playMode != GamePlayMode.PASS_AND_PLAY && boardHistory.size >= 2) 2 else 1
                        repeat(undoCount) {
                            if (boardHistory.isNotEmpty() && movesHistory.isNotEmpty()) {
                                boardHistory.removeAt(boardHistory.lastIndex)
                                val removedMove = movesHistory.removeAt(movesHistory.lastIndex)
                                removedMove.capturedPiece?.let {
                                    if (capturedByWhite.contains(it)) capturedByWhite.remove(it)
                                    else capturedByBlack.remove(it)
                                }
                            }
                        }
                        if (boardHistory.isNotEmpty()) {
                            val prevBoard = boardHistory.last()
                            board.loadFen(prevBoard.toFen())
                        } else {
                            board.setupInitialPosition()
                        }
                        selectedSquare = null
                        suggestedHintMove = null
                        boardRevision++
                    }
                },
                enabled = movesHistory.isNotEmpty() && !isEngineThinking,
                modifier = Modifier.testTag("undo_button")
            ) {
                Icon(Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo")
            }

            FilledTonalButton(
                onClick = {
                    coroutineScope.launch {
                        val eval = withContext(Dispatchers.Default) {
                            AdvancedChessEngine.findBestMove(board, EngineLevel.GRANDMASTER)
                        }
                        suggestedHintMove = eval.bestMove
                    }
                },
                enabled = !isEngineThinking,
                modifier = Modifier.testTag("hint_button")
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Hint", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hint")
            }

            Button(
                onClick = {
                    onStartGameReview(movesHistory.toList(), playerColor)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                enabled = movesHistory.isNotEmpty(),
                modifier = Modifier.testTag("review_button")
            ) {
                Icon(Icons.Default.Assessment, contentDescription = "Review", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Review", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(onClick = { resetGame() }, modifier = Modifier.testTag("reset_button")) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
            }
        }
    }

    // Pawn Promotion Dialog
    if (showPromotionDialog != null) {
        val (from, to) = showPromotionDialog!!
        AlertDialog(
            onDismissRequest = { showPromotionDialog = null },
            title = { Text("Promote Pawn to:") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val color = board.getPiece(from)?.color ?: PieceColor.WHITE
                    val pieces = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
                    pieces.forEach { pieceType ->
                        Button(
                            onClick = {
                                val move = Move(
                                    from = from,
                                    to = to,
                                    promotion = pieceType,
                                    capturedPiece = board.getPiece(to)
                                )
                                showPromotionDialog = null
                                executePlayerMove(move)
                            }
                        ) {
                            Text(Piece(pieceType, color).unicodeSymbol, fontSize = 24.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Game Over Dialog
    if (gameOverDialogVisible) {
        AlertDialog(
            onDismissRequest = { gameOverDialogVisible = false },
            title = { Text("Game Finished", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = gameOverMessage, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your game has been saved locally and backed up to Cloud Firestore. Review your game to analyze your weaknesses!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        gameOverDialogVisible = false
                        onStartGameReview(movesHistory.toList(), playerColor)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Review Game & Weak Points", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { gameOverDialogVisible = false }) {
                    Text("Close")
                }
            }
        )
    }
}
