package com.example.chess.engine

data class Square(
    val row: Int, // 0..7 (0 is 8th rank, 7 is 1st rank)
    val col: Int  // 0..7 (0 is 'a', 7 is 'h')
) {
    val isValid: Boolean
        get() = row in 0..7 && col in 0..7

    val rankNumber: Int
        get() = 8 - row

    val fileChar: Char
        get() = ('a'.code + col).toChar()

    val isLightSquare: Boolean
        get() = (row + col) % 2 == 0

    fun toAlgebraic(): String = "$fileChar$rankNumber"

    companion object {
        fun fromAlgebraic(alg: String): Square? {
            if (alg.length != 2) return null
            val fileChar = alg[0].lowercaseChar()
            val rankChar = alg[1]
            if (fileChar !in 'a'..'h' || rankChar !in '1'..'8') return null
            val col = fileChar - 'a'
            val row = 8 - (rankChar - '0')
            return Square(row, col)
        }
    }
}
