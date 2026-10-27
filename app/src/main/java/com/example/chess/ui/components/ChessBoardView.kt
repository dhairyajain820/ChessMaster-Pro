package com.example.chess.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.Board
import com.example.chess.engine.GameRules
import com.example.chess.engine.Move
import com.example.chess.engine.Piece
import com.example.chess.engine.PieceColor
import com.example.chess.engine.PieceType
import com.example.chess.engine.Square
import com.example.ui.theme.*

enum class BoardTheme(val displayName: String, val lightSquare: Color, val darkSquare: Color) {
    WALNUT("Walnut Wood", BoardWoodLight, BoardWoodDark),
    TOURNAMENT_GREEN("Tournament Green", BoardEmeraldLight, BoardEmeraldDark),
    OBSIDIAN("Obsidian Slate", BoardObsidianLight, BoardObsidianDark),
    MIDNIGHT_NEON("Midnight Neon", BoardNeonLight, BoardNeonDark)
}

@Composable
fun ChessBoardView(
    board: Board,
    isFlipped: Boolean = false,
    selectedSquare: Square? = null,
    legalMovesForSelected: List<Move> = emptyList(),
    lastMove: Move? = null,
    highlightArrowMove: Move? = null,
    boardTheme: BoardTheme = BoardTheme.TOURNAMENT_GREEN,
    onSquareClick: (Square) -> Unit,
    modifier: Modifier = Modifier
) {
    val inCheckWhite = GameRules.isInCheck(board, PieceColor.WHITE)
    val inCheckBlack = GameRules.isInCheck(board, PieceColor.BLACK)
    val whiteKingSquare = if (inCheckWhite) board.findKing(PieceColor.WHITE) else null
    val blackKingSquare = if (inCheckBlack) board.findKing(PieceColor.BLACK) else null

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(boardTheme.darkSquare)
            .testTag("chess_board")
    ) {
        val squareSize = maxWidth / 8

        // Draw 8x8 Grid
        for (r in 0..7) {
            for (c in 0..7) {
                // Determine board square based on flip
                val displayRow = if (isFlipped) 7 - r else r
                val displayCol = if (isFlipped) 7 - c else c
                val square = Square(displayRow, displayCol)

                val isLight = square.isLightSquare
                val baseColor = if (isLight) boardTheme.lightSquare else boardTheme.darkSquare

                val isSelected = selectedSquare == square
                val isLastMove = lastMove?.from == square || lastMove?.to == square
                val isKingCheck = square == whiteKingSquare || square == blackKingSquare

                val legalMove = legalMovesForSelected.find { it.to == square }
                val isTargetOfLegalMove = legalMove != null
                val isCaptureTarget = isTargetOfLegalMove && (legalMove?.capturedPiece != null || legalMove?.isEnPassant == true)

                val squareColor = when {
                    isKingCheck -> CrimsonAccent.copy(alpha = 0.65f)
                    isSelected -> GoldPrimary.copy(alpha = 0.55f)
                    isLastMove -> GoldSecondary.copy(alpha = 0.35f)
                    else -> baseColor
                }

                Box(
                    modifier = Modifier
                        .size(squareSize)
                        .offset(x = squareSize * c, y = squareSize * r)
                        .background(squareColor)
                        .clickable { onSquareClick(square) }
                        .testTag("square_${square.toAlgebraic()}"),
                    contentAlignment = Alignment.Center
                ) {
                    // Coordinates text (on borders)
                    if (c == 0) {
                        Text(
                            text = square.rankNumber.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) boardTheme.darkSquare.copy(alpha = 0.7f) else boardTheme.lightSquare.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(2.dp)
                        )
                    }
                    if (r == 7) {
                        Text(
                            text = square.fileChar.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) boardTheme.darkSquare.copy(alpha = 0.7f) else boardTheme.lightSquare.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
                        )
                    }

                    // Chess piece
                    val piece = board.getPiece(square)
                    if (piece != null) {
                        ChessPieceGraphic(
                            piece = piece,
                            size = squareSize * 0.82f
                        )
                    }

                    // Move indicator dots and rings
                    if (isTargetOfLegalMove) {
                        if (isCaptureTarget) {
                            Box(
                                modifier = Modifier
                                    .size(squareSize * 0.88f)
                                    .border(4.dp, CrimsonAccent.copy(alpha = 0.75f), CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(squareSize * 0.30f)
                                    .background(EmeraldAccent.copy(alpha = 0.85f), CircleShape)
                            )
                        }
                    }
                }
            }
        }

        // Draw tactical / coach arrow if present
        if (highlightArrowMove != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val fromR = if (isFlipped) 7 - highlightArrowMove.from.row else highlightArrowMove.from.row
                val fromC = if (isFlipped) 7 - highlightArrowMove.from.col else highlightArrowMove.from.col
                val toR = if (isFlipped) 7 - highlightArrowMove.to.row else highlightArrowMove.to.row
                val toC = if (isFlipped) 7 - highlightArrowMove.to.col else highlightArrowMove.to.col

                val sqPx = size.width / 8f
                val start = Offset((fromC + 0.5f) * sqPx, (fromR + 0.5f) * sqPx)
                val end = Offset((toC + 0.5f) * sqPx, (toR + 0.5f) * sqPx)

                drawLine(
                    color = GoldPrimary.copy(alpha = 0.8f),
                    start = start,
                    end = end,
                    strokeWidth = 9.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun ChessPieceGraphic(
    piece: Piece,
    size: androidx.compose.ui.unit.Dp
) {
    ChessComPiece(
        piece = piece,
        size = size
    )
}
