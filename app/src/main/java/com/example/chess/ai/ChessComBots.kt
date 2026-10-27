package com.example.chess.ai

data class ChessBot(
    val id: String,
    val name: String,
    val elo: Int,
    val avatarEmoji: String,
    val personality: String,
    val description: String,
    val openingQuote: String,
    val inGameQuotes: List<String>,
    val blunderQuote: String,
    val engineLevel: EngineLevel
)

object ChessComBots {

    val allBots: List<ChessBot> = listOf(
        ChessBot(
            id = "bot_martin",
            name = "Martin",
            elo = 250,
            avatarEmoji = "👨‍👦",
            personality = "Friendly Beginner",
            description = "A friendly dad learning chess along with his kids. Often drops pieces, but always has fun!",
            openingQuote = "Hello! Have fun and don't worry, my kids usually beat me too!",
            inGameQuotes = listOf(
                "Is that move legal? Wow, nice one!",
                "Wait, was that pawn supposed to stay there?",
                "You're playing great today!",
                "Oops! Did I drop a piece again?"
            ),
            blunderQuote = "Haha, I didn't see that coming at all!",
            engineLevel = EngineLevel.BEGINNER
        ),
        ChessBot(
            id = "bot_nelson",
            name = "Nelson",
            elo = 1300,
            avatarEmoji = "👑",
            personality = "The Queen Rusher",
            description = "The most notorious bot on Chess.com! He loves bringing his Queen out on move 2 (Qh5) to attack your f7 square.",
            openingQuote = "Prepare yourself! My Queen is coming out to attack immediately!",
            inGameQuotes = listOf(
                "My Queen goes wherever she pleases!",
                "Check! Can your King survive this pressure?",
                "Careful with your f7 pawn... it looks juicy!",
                "You survived my Queen attack? Not for long!"
            ),
            blunderQuote = "What?! How did you trap my Queen?!",
            engineLevel = EngineLevel.CASUAL
        ),
        ChessBot(
            id = "bot_wendy",
            name = "Wendy",
            elo = 1500,
            avatarEmoji = "👩‍💼",
            personality = "Positional Strategist",
            description = "Calm, composed, and positional. Wendy never rushes and slowly squeezes weaknesses.",
            openingQuote = "Welcome! Let's play a quiet, deeply strategic chess game.",
            inGameQuotes = listOf(
                "Notice how your bishop lacks active diagonals?",
                "Solidifying my center pawn chain step by step.",
                "Rook on the open file—rule number one of good chess.",
                "Patience and endgames are where games are truly won."
            ),
            blunderQuote = "Interesting counter-play. I must reassess my defense.",
            engineLevel = EngineLevel.INTERMEDIATE
        ),
        ChessBot(
            id = "bot_antonio",
            name = "Antonio",
            elo = 1800,
            avatarEmoji = "⚡",
            personality = "Tactical Striker",
            description = "Fierce attacker who loves piece sacrifices, open files, and kingside pawn storms.",
            openingQuote = "I hope you brought your best defense, because I am attacking full throttle!",
            inGameQuotes = listOf(
                "A sacrifice to tear open your king's pawn shield!",
                "Do you feel the pressure building on g7?",
                "Attack is the best defense in modern chess!",
                "Calculating five moves ahead... looks like mate soon."
            ),
            blunderQuote = "Incredible defense! You found the only refutation.",
            engineLevel = EngineLevel.MASTER
        ),
        ChessBot(
            id = "bot_coach_danny",
            name = "Master Coach",
            elo = 2200,
            avatarEmoji = "🎓",
            personality = "Grandmaster Coach",
            description = "The official Chess.com coach bot. Gives you real-time tips, tactical pointers, and evaluates your moves.",
            openingQuote = "Welcome to the board! Remember the golden rule: Checks, Captures, and Threats on every move.",
            inGameQuotes = listOf(
                "💡 Coach Tip: Look for undefended pieces in your opponent's camp.",
                "💡 Coach Tip: Castle early to connect your rooks and safeguard your King.",
                "💡 Coach Tip: A knight on the rim is dim—keep your knights centralized!",
                "💡 Coach Tip: When ahead in material, simplify into an easy endgame."
            ),
            blunderQuote = "Sharp tactical awareness! That was grandmaster level finding.",
            engineLevel = EngineLevel.GRANDMASTER
        )
    )

    fun getBotById(id: String): ChessBot = allBots.find { it.id == id } ?: allBots.first()
}
