package com.example.chess.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.Piece
import com.example.chess.engine.PieceColor
import com.example.chess.engine.PieceType
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

@Composable
fun PlayerHeaderCard(
    playerName: String,
    playerRating: String,
    isCurrentTurn: Boolean,
    capturedPieces: List<Piece>,
    materialAdvantage: Int,
    timeRemainingSec: Int?,
    isWhite: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCurrentTurn) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player details & captured
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (isWhite) Color.White else Color.Black)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = playerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "($playerRating)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Captured pieces list
            if (capturedPieces.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    val grouped = capturedPieces.groupBy { it.type }
                    for ((type, list) in grouped) {
                        Text(
                            text = list.first().unicodeSymbol.repeat(list.size),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (materialAdvantage > 0) {
                        Text(
                            text = "+$materialAdvantage",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                }
            }
        }

        // Clock timer
        if (timeRemainingSec != null) {
            val mins = timeRemainingSec / 60
            val secs = timeRemainingSec % 60
            val timeString = String.format("%02d:%02d", mins, secs)
            val isLowTime = timeRemainingSec < 30

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isLowTime) CrimsonAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Clock",
                    tint = if (isLowTime) CrimsonAccent else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = timeString,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isLowTime) CrimsonAccent else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CentipawnEvalBar(
    evalScoreCentipawns: Int,
    modifier: Modifier = Modifier
) {
    // Convert centipawns into White win percentage 0f..1f
    val clamped = evalScoreCentipawns.coerceIn(-1500, 1500)
    val wp = 0.5f + (0.5f * (2.0 / (1.0 + Math.exp(-0.00368208 * clamped)) - 1.0)).toFloat()
    val animatedWhiteFraction by animateFloatAsState(targetValue = wp.coerceIn(0.05f, 0.95f), label = "eval_bar")

    val scoreDisplay = when {
        evalScoreCentipawns > 15000 -> "+M${(20000 - evalScoreCentipawns + 1) / 2}"
        evalScoreCentipawns < -15000 -> "-M${(20000 + evalScoreCentipawns + 1) / 2}"
        else -> {
            val sign = if (evalScoreCentipawns > 0) "+" else ""
            String.format("%s%.1f", sign, evalScoreCentipawns / 100.0)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF27272A)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // White portion
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(animatedWhiteFraction)
                .background(Color.White),
            contentAlignment = Alignment.CenterStart
        ) {
            if (animatedWhiteFraction > 0.25f) {
                Text(
                    text = scoreDisplay,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        // Black portion
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f - animatedWhiteFraction)
                .background(Color(0xFF18181B)),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (animatedWhiteFraction <= 0.25f) {
                Text(
                    text = scoreDisplay,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }
    }
}
