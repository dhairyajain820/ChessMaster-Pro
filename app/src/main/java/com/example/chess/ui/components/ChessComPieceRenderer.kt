package com.example.chess.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.chess.engine.Piece
import com.example.chess.engine.PieceColor
import com.example.chess.engine.PieceType

/**
 * High-fidelity Chess.com "Neo" style vector piece renderer.
 * Features ivory/clean white fills with crisp dark contours for White,
 * and obsidian/charcoal fills with sharp inner highlights for Black.
 */
@Composable
fun ChessComPiece(
    piece: Piece,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val isWhite = piece.color == PieceColor.WHITE
    val fillColor = if (isWhite) Color(0xFFFAF9F6) else Color(0xFF232328)
    val strokeColor = if (isWhite) Color(0xFF1E1E24) else Color(0xFFECEFF1)
    val accentColor = if (isWhite) Color(0xFFE2E8F0) else Color(0xFF3F3F46)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.92f)) {
            val w = this.size.width
            val h = this.size.height
            val strokeWidth = w * 0.045f

            when (piece.type) {
                PieceType.PAWN -> drawNeoPawn(w, h, fillColor, strokeColor, accentColor, strokeWidth, isWhite)
                PieceType.KNIGHT -> drawNeoKnight(w, h, fillColor, strokeColor, accentColor, strokeWidth, isWhite)
                PieceType.BISHOP -> drawNeoBishop(w, h, fillColor, strokeColor, accentColor, strokeWidth, isWhite)
                PieceType.ROOK -> drawNeoRook(w, h, fillColor, strokeColor, accentColor, strokeWidth, isWhite)
                PieceType.QUEEN -> drawNeoQueen(w, h, fillColor, strokeColor, accentColor, strokeWidth, isWhite)
                PieceType.KING -> drawNeoKing(w, h, fillColor, strokeColor, accentColor, strokeWidth, isWhite)
            }
        }
    }
}

private fun DrawScope.drawNeoPawn(
    w: Float, h: Float,
    fill: Color, stroke: Color, accent: Color, strokeW: Float, isWhite: Boolean
) {
    // 1. Base pedestal
    val base = Path().apply {
        moveTo(w * 0.22f, h * 0.88f)
        lineTo(w * 0.78f, h * 0.88f)
        quadraticTo(w * 0.74f, h * 0.78f, w * 0.62f, h * 0.74f)
        lineTo(w * 0.38f, h * 0.74f)
        quadraticTo(w * 0.26f, h * 0.78f, w * 0.22f, h * 0.88f)
        close()
    }
    drawPath(base, fill, style = Fill)
    drawPath(base, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))

    // 2. Collar & Stem
    val stem = Path().apply {
        moveTo(w * 0.38f, h * 0.74f)
        quadraticTo(w * 0.44f, h * 0.52f, w * 0.40f, h * 0.46f)
        lineTo(w * 0.60f, h * 0.46f)
        quadraticTo(w * 0.56f, h * 0.52f, w * 0.62f, h * 0.74f)
        close()
    }
    drawPath(stem, fill, style = Fill)
    drawPath(stem, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))

    // Collar ring
    drawOval(
        color = fill,
        topLeft = Offset(w * 0.35f, h * 0.43f),
        size = Size(w * 0.30f, h * 0.08f)
    )
    drawOval(
        color = stroke,
        topLeft = Offset(w * 0.35f, h * 0.43f),
        size = Size(w * 0.30f, h * 0.08f),
        style = Stroke(strokeW)
    )

    // 3. Spherical Head
    drawCircle(
        color = fill,
        radius = w * 0.17f,
        center = Offset(w * 0.50f, h * 0.30f)
    )
    drawCircle(
        color = stroke,
        radius = w * 0.17f,
        center = Offset(w * 0.50f, h * 0.30f),
        style = Stroke(strokeW)
    )

    // Gloss highlight
    drawCircle(
        color = accent.copy(alpha = 0.5f),
        radius = w * 0.05f,
        center = Offset(w * 0.44f, h * 0.25f)
    )
}

private fun DrawScope.drawNeoKnight(
    w: Float, h: Float,
    fill: Color, stroke: Color, accent: Color, strokeW: Float, isWhite: Boolean
) {
    // Signature arched Chess.com knight horse profile
    val horse = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.72f, h * 0.80f)
        // Back of neck mane
        cubicTo(w * 0.74f, h * 0.60f, w * 0.78f, h * 0.40f, w * 0.66f, h * 0.22f) // Top of mane
        // Ears
        lineTo(w * 0.64f, h * 0.14f)
        lineTo(w * 0.56f, h * 0.22f)
        // Forehead down to snout
        lineTo(w * 0.42f, h * 0.28f)
        lineTo(w * 0.22f, h * 0.42f)
        // Muzzle
        lineTo(w * 0.20f, h * 0.50f)
        lineTo(w * 0.30f, h * 0.56f)
        // Jaw to chest
        lineTo(w * 0.44f, h * 0.54f)
        quadraticTo(w * 0.30f, h * 0.68f, w * 0.28f, h * 0.80f)
        close()
    }
    drawPath(horse, fill, style = Fill)
    drawPath(horse, stroke, style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Eye
    drawCircle(
        color = stroke,
        radius = w * 0.035f,
        center = Offset(w * 0.48f, h * 0.36f)
    )

    // Mane segment lines
    drawLine(
        color = stroke,
        start = Offset(w * 0.62f, h * 0.32f),
        end = Offset(w * 0.72f, h * 0.38f),
        strokeWidth = strokeW * 0.8f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = stroke,
        start = Offset(w * 0.60f, h * 0.48f),
        end = Offset(w * 0.70f, h * 0.54f),
        strokeWidth = strokeW * 0.8f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawNeoBishop(
    w: Float, h: Float,
    fill: Color, stroke: Color, accent: Color, strokeW: Float, isWhite: Boolean
) {
    // Pedestal
    val base = Path().apply {
        moveTo(w * 0.22f, h * 0.88f)
        lineTo(w * 0.78f, h * 0.88f)
        lineTo(w * 0.70f, h * 0.78f)
        lineTo(w * 0.30f, h * 0.78f)
        close()
    }
    drawPath(base, fill, style = Fill)
    drawPath(base, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))

    // Mitre Body
    val mitre = Path().apply {
        moveTo(w * 0.32f, h * 0.78f)
        cubicTo(w * 0.22f, h * 0.55f, w * 0.28f, h * 0.35f, w * 0.50f, h * 0.22f)
        cubicTo(w * 0.72f, h * 0.35f, w * 0.78f, h * 0.55f, w * 0.68f, h * 0.78f)
        close()
    }
    drawPath(mitre, fill, style = Fill)
    drawPath(mitre, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))

    // Top teardrop finial orb
    drawCircle(
        color = fill,
        radius = w * 0.065f,
        center = Offset(w * 0.50f, h * 0.18f)
    )
    drawCircle(
        color = stroke,
        radius = w * 0.065f,
        center = Offset(w * 0.50f, h * 0.18f),
        style = Stroke(strokeW)
    )

    // Bishop Slash cutout (iconic mitre slit)
    val slit = Path().apply {
        moveTo(w * 0.44f, h * 0.34f)
        lineTo(w * 0.58f, h * 0.48f)
    }
    drawPath(slit, stroke, style = Stroke(strokeW * 1.1f, cap = StrokeCap.Round))
}

private fun DrawScope.drawNeoRook(
    w: Float, h: Float,
    fill: Color, stroke: Color, accent: Color, strokeW: Float, isWhite: Boolean
) {
    // Pedestal
    val base = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.72f, h * 0.78f)
        lineTo(w * 0.28f, h * 0.78f)
        close()
    }
    drawPath(base, fill, style = Fill)
    drawPath(base, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))

    // Tower walls
    val tower = Path().apply {
        moveTo(w * 0.30f, h * 0.78f)
        lineTo(w * 0.34f, h * 0.42f)
        lineTo(w * 0.66f, h * 0.42f)
        lineTo(w * 0.70f, h * 0.78f)
        close()
    }
    drawPath(tower, fill, style = Fill)
    drawPath(tower, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))

    // Castle Crenellations Header
    val parapet = Path().apply {
        moveTo(w * 0.24f, h * 0.42f)
        lineTo(w * 0.24f, h * 0.22f)
        lineTo(w * 0.36f, h * 0.22f)
        lineTo(w * 0.36f, h * 0.30f)
        lineTo(w * 0.44f, h * 0.30f)
        lineTo(w * 0.44f, h * 0.22f)
        lineTo(w * 0.56f, h * 0.22f)
        lineTo(w * 0.56f, h * 0.30f)
        lineTo(w * 0.64f, h * 0.30f)
        lineTo(w * 0.64f, h * 0.22f)
        lineTo(w * 0.76f, h * 0.22f)
        lineTo(w * 0.76f, h * 0.42f)
        close()
    }
    drawPath(parapet, fill, style = Fill)
    drawPath(parapet, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))
}

private fun DrawScope.drawNeoQueen(
    w: Float, h: Float,
    fill: Color, stroke: Color, accent: Color, strokeW: Float, isWhite: Boolean
) {
    // Pedestal
    val base = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.70f, h * 0.78f)
        lineTo(w * 0.30f, h * 0.78f)
        close()
    }
    drawPath(base, fill, style = Fill)
    drawPath(base, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))

    // Flared Crown Body
    val crown = Path().apply {
        moveTo(w * 0.32f, h * 0.78f)
        quadraticTo(w * 0.34f, h * 0.55f, w * 0.18f, h * 0.32f) // Left spike
        lineTo(w * 0.35f, h * 0.44f)
        lineTo(w * 0.50f, h * 0.26f) // Center tall spike
        lineTo(w * 0.65f, h * 0.44f)
        lineTo(w * 0.82f, h * 0.32f) // Right spike
        quadraticTo(w * 0.66f, h * 0.55f, w * 0.68f, h * 0.78f)
        close()
    }
    drawPath(crown, fill, style = Fill)
    drawPath(crown, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))

    // Pearls / Orbs on crown tips
    val pearls = listOf(
        Offset(w * 0.18f, h * 0.30f),
        Offset(w * 0.50f, h * 0.24f),
        Offset(w * 0.82f, h * 0.30f)
    )
    for (p in pearls) {
        drawCircle(color = fill, radius = w * 0.045f, center = p)
        drawCircle(color = stroke, radius = w * 0.045f, center = p, style = Stroke(strokeW))
    }
}

private fun DrawScope.drawNeoKing(
    w: Float, h: Float,
    fill: Color, stroke: Color, accent: Color, strokeW: Float, isWhite: Boolean
) {
    // Pedestal
    val base = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.70f, h * 0.78f)
        lineTo(w * 0.30f, h * 0.78f)
        close()
    }
    drawPath(base, fill, style = Fill)
    drawPath(base, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))

    // Robe Body
    val body = Path().apply {
        moveTo(w * 0.30f, h * 0.78f)
        cubicTo(w * 0.22f, h * 0.56f, w * 0.24f, h * 0.36f, w * 0.50f, h * 0.26f)
        cubicTo(w * 0.76f, h * 0.36f, w * 0.78f, h * 0.56f, w * 0.70f, h * 0.78f)
        close()
    }
    drawPath(body, fill, style = Fill)
    drawPath(body, stroke, style = Stroke(strokeW, join = StrokeJoin.Round))

    // Royal Cross Finial
    val crossH = Path().apply {
        moveTo(w * 0.40f, h * 0.17f)
        lineTo(w * 0.60f, h * 0.17f)
    }
    val crossV = Path().apply {
        moveTo(w * 0.50f, h * 0.10f)
        lineTo(w * 0.50f, h * 0.26f)
    }
    drawPath(crossV, stroke, style = Stroke(strokeW * 1.3f, cap = StrokeCap.Round))
    drawPath(crossH, stroke, style = Stroke(strokeW * 1.3f, cap = StrokeCap.Round))
}
