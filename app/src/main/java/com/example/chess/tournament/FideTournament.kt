package com.example.chess.tournament

data class FidePlayer(
    val rank: Int,
    val name: String,
    val title: String, // GM, WGM, etc.
    val federation: String,
    val fideRating: Int,
    val score: Double
)

data class FideTournament(
    val id: String,
    val name: String,
    val category: String, // "World Championship Cycle", "Super-GM Classical", "Olympiad"
    val dates: String,
    val location: String,
    val prizeFund: String,
    val status: String, // "Live", "Upcoming", "Recent"
    val currentRound: String,
    val summary: String,
    val standings: List<FidePlayer>,
    val featuredGamePgn: String,
    val featuredGameTitle: String
)
