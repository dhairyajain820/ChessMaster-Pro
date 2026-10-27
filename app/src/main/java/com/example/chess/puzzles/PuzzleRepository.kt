package com.example.chess.puzzles

import com.example.chess.engine.PieceColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object PuzzleRepository {

    val curatedPuzzles: List<ChessPuzzle> = listOf(
        // ================= EASY (600 - 1200) =================
        ChessPuzzle(
            id = "easy_1",
            title = "Back-Rank Checkmate",
            difficulty = PuzzleDifficulty.EASY,
            theme = "Back Rank Mate",
            fen = "6k1/5ppp/8/8/8/8/4QPPP/4R1K1 w - - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("e2e8"),
            solutionMovesSan = listOf("Qe8#"),
            winningStrategy = "The Black King is trapped behind its own defensive pawns on f7, g7, and h7 without a flight square (luft). White capitalizes by delivering checkmate directly on the back rank.",
            keyMovesExplanation = "1. Qe8# strikes the vulnerable 8th rank. Black cannot block or capture the Queen, resulting in immediate checkmate.",
            hint = "Look for an unprotected back rank."
        ),
        ChessPuzzle(
            id = "easy_2",
            title = "Royal Knight Fork",
            difficulty = PuzzleDifficulty.EASY,
            theme = "Knight Fork",
            fen = "r1b1k2r/pp1p1ppp/2n1pn2/q7/2B1P3/2N2N2/PP1Q1PPP/R3K2R w KQkq - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("c3b5", "a5d2", "f3d2"),
            solutionMovesSan = listOf("Nb5", "Qxd2+", "Nxd2"),
            winningStrategy = "White leaps the knight into b5, threatening Nc7+ which forks the Black King and Rook. Black is forced to trade Queens, leaving White with total control of the c7 outpost.",
            keyMovesExplanation = "1. Nb5! attacks c7 with dual threat. 1...Qxd2+ 2. Nxd2 leaves White threatening both Nc7+ winning the a8 rook and dominating the board.",
            hint = "Can you threaten a lethal jump to c7?"
        ),
        ChessPuzzle(
            id = "easy_3",
            title = "The Lethal Pin",
            difficulty = PuzzleDifficulty.EASY,
            theme = "Pin",
            fen = "r1bqk2r/pppp1ppp/2n5/4p3/1bB1n3/2N2N2/PPPP1PPP/R1BQR1K1 w kq - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("e1e4"),
            solutionMovesSan = listOf("Rxe4"),
            winningStrategy = "The Black knight on e4 is pinned to the king along the e-file. Because the d7 pawn cannot guard it without opening the king, White can safely recapture and win a piece.",
            keyMovesExplanation = "1. Rxe4 wins the centralized knight since Black cannot afford further pressure against the e8 king.",
            hint = "Exploit the open e-file against the uncastled King."
        ),
        ChessPuzzle(
            id = "easy_4",
            title = "Hanging Queen Tactic",
            difficulty = PuzzleDifficulty.EASY,
            theme = "Hanging Piece",
            fen = "r1b2rk1/pp3ppp/2n1p3/3q4/3P4/5N2/PP1Q1PPP/R3KB1R w KQ - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("f1d3"),
            solutionMovesSan = listOf("Bd3"),
            winningStrategy = "White rapidly develops with tempo while securing kingside castling and preparing a deadly battery toward h7.",
            keyMovesExplanation = "1. Bd3 activates the light-squared bishop, cementing White's center control and preparing kingside kingside attack.",
            hint = "Develop with purpose toward the Black kingside."
        ),
        ChessPuzzle(
            id = "easy_5",
            title = "Classic Skewer",
            difficulty = PuzzleDifficulty.EASY,
            theme = "Skewer",
            fen = "4k3/8/8/8/8/8/1r6/R3K3 w - - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("a1a8", "e8f7", "a8a7"),
            solutionMovesSan = listOf("Ra8+", "Kf7", "Ra7+"),
            winningStrategy = "A skewer occurs when a valuable piece (the King) is attacked and forced to move, exposing a less valuable piece behind it. White maneuvers the rook to capture Black's rook.",
            keyMovesExplanation = "1. Ra8+ forces the Black King away, setting up 2. Ra7+ which attacks both King and Rook along the 7th rank.",
            hint = "Force the King to move and expose the rook on the 2nd rank."
        ),
        ChessPuzzle(
            id = "easy_6",
            title = "Smothered Mate in One",
            difficulty = PuzzleDifficulty.EASY,
            theme = "Smothered Mate",
            fen = "6rk/5Npp/8/8/8/8/8/4K3 w - - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("f7h6"),
            solutionMovesSan = listOf("Nh6#"),
            winningStrategy = "When the enemy King is surrounded and suffocated by its own pieces (rook and pawns), a single knight check is fatal because the king has no empty squares to escape.",
            keyMovesExplanation = "1. Nh6# (or 1. Nf7#) directly checks the trapped King in the corner.",
            hint = "The King cannot move because its own army blocks every square."
        ),

        // ================= MEDIUM (1200 - 1800) =================
        ChessPuzzle(
            id = "med_1",
            title = "Greek Gift Sacrifice",
            difficulty = PuzzleDifficulty.MEDIUM,
            theme = "Sacrifice & Attack",
            fen = "r1bq1rk1/ppp2ppp/2np4/2b1p1N1/2B1P1n1/2NP4/PPP2PPP/R1BQ1RK1 w - - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("d1f3", "g4f6", "c3d5"),
            solutionMovesSan = listOf("Qf3", "Nf6", "Nd5"),
            winningStrategy = "White creates unstoppable converging pressure on the f7 square and the weakened dark squares around Black's castled king.",
            keyMovesExplanation = "1. Qf3! adds a third attacker to the f7 pawn. After 1...Nf6, 2. Nd5 brings maximum reinforcement.",
            hint = "Focus all your firepower on the f7 weakness."
        ),
        ChessPuzzle(
            id = "med_2",
            title = "Deflection on the Back Rank",
            difficulty = PuzzleDifficulty.MEDIUM,
            theme = "Deflection",
            fen = "2r3k1/5ppp/8/8/8/5Q2/5PPP/2r1R1K1 w - - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("f3e3", "c1e1", "e3e1"),
            solutionMovesSan = listOf("Qe3", "Rxe1+", "Qxe1"),
            winningStrategy = "Black's rook invaded on c1, but White uses Queen geometry to defend e1 while maintaining total back-rank control and simplification into a winning endgame.",
            keyMovesExplanation = "1. Qe3! protects the rook on e1. After the forced trades, White retains dominant control.",
            hint = "Defend your back rank with Queen coordination."
        ),
        ChessPuzzle(
            id = "med_3",
            title = "Discovered Double Attack",
            difficulty = PuzzleDifficulty.MEDIUM,
            theme = "Discovered Attack",
            fen = "r1b2rk1/pp3ppp/2n1pn2/q1bp4/2P5/2N1PN2/PP1BBPPP/R2QK2R w KQ - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("c3d5", "a5d8", "d5f6"),
            solutionMovesSan = listOf("Nd5", "Qd8", "Nxf6+"),
            winningStrategy = "Moving the knight from c3 unmasks a discovered attack from the bishop on e2 against the Black queen on a5, while the knight simultaneously strikes at d5 and f6.",
            keyMovesExplanation = "1. Nd5! attacks the Queen via the Bishop on e2, winning a key tempo and shattering Black's pawn structure after 2. Nxf6+.",
            hint = "Look for what piece is behind the knight on c3."
        ),
        ChessPuzzle(
            id = "med_4",
            title = "Boden's Mate Pattern",
            difficulty = PuzzleDifficulty.MEDIUM,
            theme = "Checkmate Pattern",
            fen = "2kr3r/ppp2ppp/2p5/8/4P1b1/2NPP3/PPP1B1Pq/R2K1R2 w - - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("e2g4", "c8b8", "d1d2"),
            solutionMovesSan = listOf("Bxg4+", "Kb8", "Kd2"),
            winningStrategy = "White eliminates Black's active bishop with check, securing the king position and defusing Black's attack.",
            keyMovesExplanation = "1. Bxg4+ checks the king and wins the bishop, turning Black's aggressive queen sortie into a losing tactical blunder.",
            hint = "Capture with check to win a tempo."
        ),

        // ================= HARD (1800 - 2400+) =================
        ChessPuzzle(
            id = "hard_1",
            title = "Morphy's Opera House Theme",
            difficulty = PuzzleDifficulty.HARD,
            theme = "Decoy & Mate",
            fen = "4kb1r/p2n1ppp/4p3/3p4/8/1P6/PBPP1PPP/R3K2R w KQk - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("e1e2", "f7f6", "h1c1"),
            solutionMovesSan = listOf("Ke2", "f6", "Rhc1"),
            winningStrategy = "Grandmaster king positioning: White brings the King to e2 to connect the rooks and prepare c-file invasion, demonstrating deep prophylactic calculation.",
            keyMovesExplanation = "1. Ke2! activates the entire back rank, allowing rooks to seize open central files with lethal speed.",
            hint = "Connect your rooks without losing coordination."
        ),
        ChessPuzzle(
            id = "hard_2",
            title = "The Legendary Queen Sacrifice",
            difficulty = PuzzleDifficulty.HARD,
            theme = "Queen Sacrifice",
            fen = "r1b2rk1/2p2ppp/p7/1p1N4/3P4/1BP3b1/PP3PPq/RNBQRK2 b - - 0 1",
            playerColor = PieceColor.BLACK,
            solutionMovesUci = listOf("h2h1", "f1e2", "c8g4"),
            solutionMovesSan = listOf("Qh1+", "Ke2", "Bg4+"),
            winningStrategy = "Black drives the White King into the open center with relentless checks. By bringing the light-squared bishop into the fray, White's queen is pinned and won.",
            keyMovesExplanation = "1...Qh1+ 2. Ke2 Bg4+ skewering the White Queen on d1. White is completely lost.",
            hint = "Drive the king forward and pin the queen."
        ),
        ChessPuzzle(
            id = "hard_3",
            title = "Deep Rook Clearance",
            difficulty = PuzzleDifficulty.HARD,
            theme = "Clearance",
            fen = "r2qk2r/ppp2ppp/2n1p3/3pP3/3P2b1/2PB1N2/P1P2PPP/R2QK2R w KQkq - 0 1",
            playerColor = PieceColor.WHITE,
            solutionMovesUci = listOf("h2h3", "g4h5", "g2g4"),
            solutionMovesSan = listOf("h3", "Bh5", "g4"),
            winningStrategy = "A forceful kingside pawn storm kicking the enemy bishop off the critical diagonal, gaining permanent space and opening lines for a decisive kingside breakthrough.",
            keyMovesExplanation = "1. h3 Bh5 2. g4 Bg6 neutralizes the pin on f3 and grants White decisive initiative.",
            hint = "Kick the pinning bishop immediately."
        )
    )

    fun getPuzzlesByDifficulty(difficulty: PuzzleDifficulty): List<ChessPuzzle> {
        return curatedPuzzles.filter { it.difficulty == difficulty }
    }

    suspend fun fetchDailyOnlinePuzzle(): ChessPuzzle = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://lichess.org/api/puzzle/daily")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val puzzleObj = root.getJSONObject("puzzle")
                val gameObj = root.getJSONObject("game")

                val id = puzzleObj.optString("id", "daily_online")
                val rating = puzzleObj.optInt("rating", 1500)
                val themes = puzzleObj.optJSONArray("themes")
                val themeName = if (themes != null && themes.length() > 0) themes.getString(0).replaceFirstChar { it.uppercase() } else "Tactics"

                val fen = puzzleObj.optString("fen", "r1bqkb1r/pppp1ppp/2n5/4p3/4n3/2N2N2/PPPP1PPP/R1BQKB1R w KQkq - 0 1")
                val movesArray = puzzleObj.optJSONArray("solution")
                val uciMoves = mutableListOf<String>()
                if (movesArray != null) {
                    for (i in 0 until movesArray.length()) {
                        uciMoves.add(movesArray.getString(i))
                    }
                }

                val diff = when {
                    rating < 1300 -> PuzzleDifficulty.EASY
                    rating < 1800 -> PuzzleDifficulty.MEDIUM
                    else -> PuzzleDifficulty.HARD
                }

                ChessPuzzle(
                    id = "online_$id",
                    title = "Lichess Daily Master Challenge ($rating ELO)",
                    difficulty = diff,
                    theme = themeName,
                    fen = fen,
                    playerColor = PieceColor.WHITE,
                    solutionMovesUci = uciMoves.ifEmpty { listOf("e4e5") },
                    solutionMovesSan = uciMoves.ifEmpty { listOf("e5") },
                    winningStrategy = "A real grandmaster tournament position selected by the community. Find the most punishing continuation to turn the advantage into a victory.",
                    keyMovesExplanation = "Key master sequence: ${uciMoves.joinToString(" ")}.",
                    hint = "Look for the most forcing move that leaves opponent with no good reply."
                )
            } else {
                curatedPuzzles.random()
            }
        } catch (e: Exception) {
            // Graceful fallback to curated offline puzzle
            curatedPuzzles.random()
        }
    }
}
