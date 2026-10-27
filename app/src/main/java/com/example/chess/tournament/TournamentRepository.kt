package com.example.chess.tournament

object TournamentRepository {

    val fideTournaments: List<FideTournament> = listOf(
        FideTournament(
            id = "fide_candidates_2026",
            name = "FIDE Candidates Tournament",
            category = "World Championship Cycle",
            dates = "April 2026",
            location = "Toronto / Madrid",
            prizeFund = "€500,000",
            status = "Live",
            currentRound = "Round 11 of 14",
            summary = "The pinnacle 8-player double round-robin tournament to determine the official challenger for the FIDE World Chess Championship crown.",
            standings = listOf(
                FidePlayer(1, "Gukesh D", "GM", "IND", 2794, 8.5),
                FidePlayer(2, "Hikaru Nakamura", "GM", "USA", 2802, 8.0),
                FidePlayer(3, "Fabiano Caruana", "GM", "USA", 2805, 8.0),
                FidePlayer(4, "Ian Nepomniachtchi", "GM", "FID", 2770, 7.5),
                FidePlayer(5, "Alireza Firouzja", "GM", "FRA", 2767, 6.0),
                FidePlayer(6, "Praggnanandhaa R", "GM", "IND", 2755, 6.0),
                FidePlayer(7, "Vidit Gujrathi", "GM", "IND", 2730, 5.0),
                FidePlayer(8, "Nijat Abasov", "GM", "AZE", 2632, 3.0)
            ),
            featuredGameTitle = "Gukesh D vs Hikaru Nakamura (Round 7)",
            featuredGamePgn = "1. d4 Nf6 2. c4 e6 3. Nc3 Bb4 4. Qc2 O-O 5. a3 Bxc3+ 6. Qxc3 d5 7. Nf3 dxc4 8. Qxc4 b6 9. Bg5 Ba6 10. Qa4 Qd7 11. Qc2 c5 12. Bxf6 gxf6 13. dxc5 Rc8 14. Rd1 Qe7 15. Qe4 Bb7 16. Qg4+ Kh8 17. cxb6 axb6 18. e3 Nd7 19. Be2 Rc2 20. Qd4 Nc5"
        ),
        FideTournament(
            id = "fide_world_championship",
            name = "FIDE World Chess Championship Match",
            category = "World Championship",
            dates = "November - December 2026",
            location = "Singapore",
            prizeFund = "$2,500,000",
            status = "Upcoming",
            currentRound = "Match Prep",
            summary = "The 14-game classical match between the reigning World Champion Ding Liren and the official Challenger to decide the King of Chess.",
            standings = listOf(
                FidePlayer(1, "Ding Liren (Defending Champion)", "GM", "CHN", 2762, 0.0),
                FidePlayer(2, "Gukesh D (Challenger)", "GM", "IND", 2794, 0.0)
            ),
            featuredGameTitle = "Ding Liren vs Ian Nepomniachtchi (Game 6)",
            featuredGamePgn = "1. d4 Nf6 2. Nf3 d5 3. Bf4 c5 4. e3 Nc6 5. Nbd2 cxd4 6. exd4 Bf5 7. c3 e6 8. Bb5 Bd6 9. Bxd6 Qxd6 10. O-O O-O 11. Re1 h6 12. Ne5 Ne7 13. a4 a6 14. Bf1 Nd7 15. Nxd7 Qxd7 16. a5 Qc7"
        ),
        FideTournament(
            id = "tata_steel_masters",
            name = "Tata Steel Chess Masters",
            category = "Super-GM Classical",
            dates = "January 2026",
            location = "Wijk aan Zee, Netherlands",
            prizeFund = "€150,000",
            status = "Recent",
            currentRound = "Final Standings",
            summary = "The 'Wimbledon of Chess' featuring 14 of the world's most creative super grandmasters in a legendary classical test.",
            standings = listOf(
                FidePlayer(1, "Wei Yi", "GM", "CHN", 2772, 8.5),
                FidePlayer(2, "Gukesh D", "GM", "IND", 2765, 8.5),
                FidePlayer(3, "Anish Giri", "GM", "NED", 2762, 8.5),
                FidePlayer(4, "Nodirbek Abdusattorov", "GM", "UZB", 2784, 8.5),
                FidePlayer(5, "Alireza Firouzja", "GM", "FRA", 2760, 7.5),
                FidePlayer(6, "Vidit Gujrathi", "GM", "IND", 2747, 7.5),
                FidePlayer(7, "Praggnanandhaa R", "GM", "IND", 2743, 7.5)
            ),
            featuredGameTitle = "Nodirbek Abdusattorov vs Wei Yi",
            featuredGamePgn = "1. e4 c5 2. Nf3 d6 3. d4 cxd4 4. Nxd4 Nf6 5. Nc3 a6 6. h3 e6 7. g4 b5 8. Bg2 Bb7 9. O-O Nfd7 10. Re1 Be7 11. a4 b4 12. Nd5 exd5 13. exd5 O-O 14. Nf5 Bf6 15. Nxd6 Nc5"
        ),
        FideTournament(
            id = "fide_olympiad_2026",
            name = "45th FIDE Chess Olympiad",
            category = "Olympiad / Team",
            dates = "September 2026",
            location = "Budapest, Hungary",
            prizeFund = "Trophy & Olympic Medals",
            status = "Upcoming",
            currentRound = "National Selections",
            summary = "Over 190 national federations compete in the world's largest team chess festival for the Hamilton-Russell Cup.",
            standings = listOf(
                FidePlayer(1, "India Open Team", "Team", "IND", 2753, 0.0),
                FidePlayer(2, "United States Team", "Team", "USA", 2750, 0.0),
                FidePlayer(3, "China Open Team", "Team", "CHN", 2730, 0.0),
                FidePlayer(4, "Uzbekistan Team", "Team", "UZB", 2700, 0.0)
            ),
            featuredGameTitle = "Arjun Erigaisi vs Vincent Keymer",
            featuredGamePgn = "1. d4 Nf6 2. c4 e6 3. Nf3 d5 4. Nc3 Be7 5. Bf4 O-O 6. e3 c5 7. dxc5 Bxc5 8. a3 Nc6 9. Qc2 Qa5 10. O-O-O Be7 11. g4 dxc4 12. Bxc4 e5 13. Bg5 Bxg4 14. Bxf6 Bxf6 15. Qe4"
        )
    )

    fun getTournamentById(id: String): FideTournament? = fideTournaments.find { it.id == id }
}
