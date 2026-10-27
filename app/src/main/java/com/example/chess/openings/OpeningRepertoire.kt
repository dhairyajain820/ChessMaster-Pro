package com.example.chess.openings

import com.example.chess.engine.PieceColor

object OpeningRepertoire {

    val allOpenings: List<ChessOpening> = listOf(
        // 1. RUY LOPEZ
        ChessOpening(
            id = "ruy_lopez",
            name = "Ruy Lopez (Spanish Opening)",
            eco = "C60",
            playerSide = PieceColor.WHITE,
            difficulty = "All Levels",
            shortSummary = "One of the oldest and most respected classical chess openings. White applies indirect pressure on e5 by attacking the knight on c6.",
            strategicThemes = listOf(
                "Pressure on Black's e5 pawn anchor",
                "Maintaining central pawn tension with c3 and d4",
                "Maneuvering the bishop to b3/c2 to target the f7/h7 diagonals"
            ),
            commonTraps = listOf(
                "Noah's Ark Trap: Black traps White's light-squared bishop with ...a6, ...b5, ...d6, ...c5, and ...c4"
            ),
            mainLine = OpeningLine(
                id = "ruy_main",
                name = "Morphy Defense (3...a6 4.Ba4 Nf6)",
                movesSan = listOf("e4", "e5", "Nf3", "Nc6", "Bb5", "a6", "Ba4", "Nf6", "O-O", "Be7"),
                movesUci = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6", "b5a4", "g8f6", "e1g1", "f8e7"),
                explanations = listOf(
                    OpeningMoveStep(1, "e4", "e2e4", true, "King's Pawn Opening", "Claims central control and opens diagonals for Queen and Bishop."),
                    OpeningMoveStep(2, "e5", "e7e5", false, "Classical Reply", "Black stakes equal claim in the center."),
                    OpeningMoveStep(3, "Nf3", "g1f3", true, "Knight Development", "Develops a knight toward the center with tempo, attacking e5."),
                    OpeningMoveStep(4, "Nc6", "b8c6", false, "Knight Defense", "Black defends the e5 pawn."),
                    OpeningMoveStep(5, "Bb5", "f1b5", true, "The Spanish Bishop", "Attacks the defender of e5 and creates positional tension."),
                    OpeningMoveStep(6, "a6", "a7a6", false, "Morphy Defense", "Puts the question to the bishop."),
                    OpeningMoveStep(7, "Ba4", "b5a4", true, "Bishop Retreat", "Maintains the pin and pressure along the a4-e8 diagonal."),
                    OpeningMoveStep(8, "Nf6", "g8f6", false, "Counterattack", "Black attacks White's e4 pawn."),
                    OpeningMoveStep(9, "O-O", "e1g1", true, "Kingside Castling", "Secures the King before expanding in the center with Re1 and c3-d4.")
                )
            )
        ),

        // 2. SICILIAN DEFENSE
        ChessOpening(
            id = "sicilian_najdorf",
            name = "Sicilian Defense (Najdorf)",
            eco = "B90",
            playerSide = PieceColor.BLACK,
            difficulty = "Intermediate / Master",
            shortSummary = "The sharpest and most popular response to 1.e4. Black fights asymmetrically for the center from the c-file.",
            strategicThemes = listOf(
                "Asymmetrical pawn structure granting Black queenside counterplay",
                "Control over the d5 outpost square",
                "Rapid counterattack with ...b5, ...Bb7, and ...Rc8"
            ),
            commonTraps = listOf(
                "Siberian Trap & Greek Gift sacrifices against an uncastled black king"
            ),
            mainLine = OpeningLine(
                id = "najdorf_main",
                name = "Najdorf Variation (5...a6)",
                movesSan = listOf("e4", "c5", "Nf3", "d6", "d4", "cxd4", "Nxd4", "Nf6", "Nc3", "a6"),
                movesUci = listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5d4", "f3d4", "g8f6", "b1c3", "a7a6"),
                explanations = listOf(
                    OpeningMoveStep(1, "e4", "e2e4", false, "King's Pawn", "White claims center space."),
                    OpeningMoveStep(2, "c5", "c7c5", true, "The Sicilian Defense", "Strikes at d4 from the flank, preventing White from easily occupying the full center."),
                    OpeningMoveStep(3, "Nf3", "g1f3", false, "Open Sicilian Prep", "Prepares the d4 central break."),
                    OpeningMoveStep(4, "d6", "d7d6", true, "Pawn Support", "Controls e5 and prepares knight development to f6."),
                    OpeningMoveStep(5, "d4", "d2d4", false, "Central Break", "White opens up lines."),
                    OpeningMoveStep(6, "cxd4", "c5d4", true, "Exchange", "Trades a flank pawn for a central pawn."),
                    OpeningMoveStep(7, "Nxd4", "f3d4", false, "Recapture", "Knight recaptures on d4."),
                    OpeningMoveStep(8, "Nf6", "g8f6", true, "Attacking e4", "Develops with tempo against White's e4 pawn."),
                    OpeningMoveStep(9, "Nc3", "b1c3", false, "Defending e4", "White defends e4 with the knight."),
                    OpeningMoveStep(10, "a6", "a7a6", true, "The Najdorf Move", "Controls the critical b5 square, preventing Nb5 or Bb5+, and prepares ...e5 or ...b5 expansion.")
                )
            )
        ),

        // 3. ITALIAN GAME
        ChessOpening(
            id = "italian_game",
            name = "Italian Game (Giuoco Piano)",
            eco = "C50",
            playerSide = PieceColor.WHITE,
            difficulty = "Beginner to Master",
            shortSummary = "Focuses on rapid development of minor pieces, immediate control of the center, and targeting Black's vulnerable f7 square.",
            strategicThemes = listOf(
                "Direct pressure on f7 via Bc4",
                "Building a classical pawn center with c3 and d4",
                "Creating quiet maneuvering setups with d3, c3, and Nbd2"
            ),
            commonTraps = listOf(
                "Légall's Mate: Queen sacrifice leading to checkmate with two knights and bishop on f7"
            ),
            mainLine = OpeningLine(
                id = "italian_main",
                name = "Giuoco Piano Main Line",
                movesSan = listOf("e4", "e5", "Nf3", "Nc6", "Bc4", "Bc5", "c3", "Nf6", "d3", "d6"),
                movesUci = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "f8c5", "c2c3", "g8f6", "d2d3", "d7d6"),
                explanations = listOf(
                    OpeningMoveStep(1, "e4", "e2e4", true, "King's Pawn", "Classic 1.e4 opening."),
                    OpeningMoveStep(2, "e5", "e7e5", false, "Symmetric Response", "Black mirrors White's claim in the center."),
                    OpeningMoveStep(3, "Nf3", "g1f3", true, "Knight Development", "Develops and attacks e5."),
                    OpeningMoveStep(4, "Nc6", "b8c6", false, "Defending e5", "Black guards e5."),
                    OpeningMoveStep(5, "Bc4", "f1c4", true, "The Italian Bishop", "Directs active fire straight at Black's weakest square, f7."),
                    OpeningMoveStep(6, "Bc5", "f8c5", false, "Giuoco Piano", "Black mirrors development with an active bishop on c5."),
                    OpeningMoveStep(7, "c3", "c2c3", true, "Preparing d4", "Prepares the d4 push to build an iron central pawn duo."),
                    OpeningMoveStep(8, "Nf6", "g8f6", false, "Counter-pressure", "Black targets e4."),
                    OpeningMoveStep(9, "d3", "d2d3", true, "Solid Center", "Solidifies e4 and opens diagonal for the c1 bishop.")
                )
            )
        ),

        // 4. QUEEN'S GAMBIT DECLINED
        ChessOpening(
            id = "queens_gambit",
            name = "Queen's Gambit Declined (QGD)",
            eco = "D30",
            playerSide = PieceColor.WHITE,
            difficulty = "All Levels",
            shortSummary = "One of the most foundational 1.d4 openings. White offers the c4 pawn to divert Black's central e-pawn and establish total central control.",
            strategicThemes = listOf(
                "Dominating the d4 and e4 central squares",
                "Restricting Black's light-squared bishop on c8",
                "Minority attack on the queenside with b4-b5"
            ),
            commonTraps = listOf(
                "Elephant Trap: Black sacrifices a knight on d5 to win White's queen on d1 with ...Bb4+"
            ),
            mainLine = OpeningLine(
                id = "qgd_main",
                name = "Orthodox Defense",
                movesSan = listOf("d4", "d5", "c4", "e6", "Nc3", "Nf6", "Bg5", "Be7", "e3", "O-O"),
                movesUci = listOf("d2d4", "d7d5", "c2c4", "e7e6", "b1c3", "g8f6", "c1g5", "f8e7", "e2e3", "e8g8"),
                explanations = listOf(
                    OpeningMoveStep(1, "d4", "d2d4", true, "Queen's Pawn Opening", "Takes central space and creates solid pawn structure."),
                    OpeningMoveStep(2, "d5", "d7d5", false, "Equal Claim", "Black contests the center."),
                    OpeningMoveStep(3, "c4", "c2c4", true, "The Gambit Offer", "Attacks d5 to trade a wing pawn for a center pawn."),
                    OpeningMoveStep(4, "e6", "e7e6", false, "Declining the Gambit", "Solidly reinforces d5 with a rock-solid pawn chain."),
                    OpeningMoveStep(5, "Nc3", "b1c3", true, "Knight Pressure", "Pours additional pressure on d5 and controls e4."),
                    OpeningMoveStep(6, "Nf6", "g8f6", false, "Developing Defense", "Guards d5."),
                    OpeningMoveStep(7, "Bg5", "c1g5", true, "Pinning the Knight", "Pins Black's key defender of d5."),
                    OpeningMoveStep(8, "Be7", "f8e7", false, "Breaking the Pin", "Prepares kingside castling."),
                    OpeningMoveStep(9, "e3", "e2e3", true, "Fortifying the Center", "Protects c4 and frees the f1 bishop.")
                )
            )
        ),

        // 5. FRENCH DEFENSE
        ChessOpening(
            id = "french_defense",
            name = "French Defense",
            eco = "C00",
            playerSide = PieceColor.BLACK,
            difficulty = "Intermediate",
            shortSummary = "A hypermodern, counter-punching opening. Black creates an impregnable pawn chain on d5/e6 and strikes back at White's d4 center with ...c5.",
            strategicThemes = listOf(
                "Counterattacking White's d4 pawn base with ...c5, ...Nc6, and ...Qb6",
                "Solving the 'French Bishop' problem on c8 via ...b6 or ...Bd7-e8-h5",
                "Solid king defense against kingside pawn storms"
            ),
            commonTraps = listOf(
                "Milner-Barry Gambit tactical sacrifices on d5 and e6"
            ),
            mainLine = OpeningLine(
                id = "french_advance",
                name = "Advance Variation",
                movesSan = listOf("e4", "e6", "d4", "d5", "e5", "c5", "c3", "Nc6", "Nf3", "Qb6"),
                movesUci = listOf("e2e4", "e7e6", "d2d4", "d7d5", "e4e5", "c7c5", "c2c3", "b8c6", "g1f3", "d8b6"),
                explanations = listOf(
                    OpeningMoveStep(1, "e4", "e2e4", false, "King's Pawn", "White stakes initial territory."),
                    OpeningMoveStep(2, "e6", "e7e6", true, "French Setup", "Prepares the immediate central challenge ...d5."),
                    OpeningMoveStep(3, "d4", "d2d4", false, "Broad Center", "White occupies both central squares."),
                    OpeningMoveStep(4, "d5", "d7d5", true, "Central Challenge", "Directly attacks e4."),
                    OpeningMoveStep(5, "e5", "e4e5", false, "The Advance", "Locks the center and secures kingside space."),
                    OpeningMoveStep(6, "c5", "c7c5", true, "The Flank Counter", "Immediately attacks the root of White's pawn chain on d4."),
                    OpeningMoveStep(7, "c3", "c2c3", false, "Pawn Wedge", "Reinforces d4."),
                    OpeningMoveStep(8, "Nc6", "b8c6", true, "Increasing Pressure", "Adds another attacker to d4."),
                    OpeningMoveStep(9, "Nf3", "g1f3", false, "Defending d4", "White develops to defend."),
                    OpeningMoveStep(10, "Qb6", "d8b6", true, "Triple Battery", "Attacks b2 and puts unbearable pressure on d4.")
                )
            )
        ),

        // 6. CARO-KANN DEFENSE
        ChessOpening(
            id = "caro_kann",
            name = "Caro-Kann Defense",
            eco = "B12",
            playerSide = PieceColor.BLACK,
            difficulty = "All Levels",
            shortSummary = "Renowned for its granite-like solidity. Unlike the French Defense, Black prepares ...d5 with 1...c6, keeping the c8 bishop completely free.",
            strategicThemes = listOf(
                "Flawless pawn structure with zero weaknesses",
                "Active deployment of the light-squared bishop to f5 or g4",
                "Superior endgame pawn majorities"
            ),
            commonTraps = listOf(
                "Smothered mate trap after 1.e4 c6 2.d4 d5 3.Nc3 dxe4 4.Nxe4 Nd7 5.Qe2 Ngf6 6.Nd6#"
            ),
            mainLine = OpeningLine(
                id = "caro_classical",
                name = "Classical Variation (4...Bf5)",
                movesSan = listOf("e4", "c6", "d4", "d5", "Nc3", "dxe4", "Nxe4", "Bf5", "Ng3", "Bg6"),
                movesUci = listOf("e2e4", "c7c6", "d2d4", "d7d5", "b1c3", "d5e4", "c3e4", "c8f5", "e4g3", "f5g6"),
                explanations = listOf(
                    OpeningMoveStep(1, "e4", "e2e4", false, "King's Pawn", "White takes the center."),
                    OpeningMoveStep(2, "c6", "c7c6", true, "Caro-Kann", "Prepares the solid ...d5 counter-strike."),
                    OpeningMoveStep(3, "d4", "d2d4", false, "Dual Center", "White establishes both pawns in center."),
                    OpeningMoveStep(4, "d5", "d7d5", true, "Strike", "Strikes at White's e4 pawn."),
                    OpeningMoveStep(5, "Nc3", "b1c3", false, "Classical Move", "Develops and defends e4."),
                    OpeningMoveStep(6, "dxe4", "d5e4", true, "Exchange", "Trades pawns."),
                    OpeningMoveStep(7, "Nxe4", "c3e4", false, "Recapture", "Knight recaptures."),
                    OpeningMoveStep(8, "Bf5", "c8f5", true, "Active Bishop", "Crucial move: develops the bishop outside the pawn chain before playing ...e6."),
                    OpeningMoveStep(9, "Ng3", "e4g3", false, "Knight Retort", "Attacks the bishop."),
                    OpeningMoveStep(10, "Bg6", "f5g6", true, "Safe Retreat", "Tucks bishop away safely.")
                )
            )
        ),

        // 7. LONDON SYSTEM
        ChessOpening(
            id = "london_system",
            name = "London System",
            eco = "D02",
            playerSide = PieceColor.WHITE,
            difficulty = "Beginner to Master",
            shortSummary = "An ultra-reliable, universally applicable system against almost any Black setup. White develops the dark-squared bishop to f4 before locking the pawn chain with e3.",
            strategicThemes = listOf(
                "Unshakeable d4-e3-c3 pawn pyramid",
                "Posting a dominant knight on the e5 outpost",
                "Kingside attacking chances with Bd3, Qf3/Qh5"
            ),
            commonTraps = listOf(
                "Black plays an early ...Qb6 hitting b2, met by solid Nc3 or Qb3"
            ),
            mainLine = OpeningLine(
                id = "london_main",
                name = "Modern London Main Line",
                movesSan = listOf("d4", "d5", "Bf4", "Nf6", "e3", "c5", "c3", "Nc6", "Nd2", "e6"),
                movesUci = listOf("d2d4", "d7d5", "c1f4", "g8f6", "e2e3", "c7c5", "c2c3", "b8c6", "b1d2", "e7e6"),
                explanations = listOf(
                    OpeningMoveStep(1, "d4", "d2d4", true, "Queen's Pawn", "First step in the London setup."),
                    OpeningMoveStep(2, "d5", "d7d5", false, "Symmetric Defense", "Black matches central space."),
                    OpeningMoveStep(3, "Bf4", "c1f4", true, "The London Bishop", "The defining move: activates the bishop before playing e3."),
                    OpeningMoveStep(4, "Nf6", "g8f6", false, "Knight Development", "Black develops."),
                    OpeningMoveStep(5, "e3", "e2e3", true, "Pawn Anchor", "Supports d4 and opens diagonal for the light-squared bishop."),
                    OpeningMoveStep(6, "c5", "c7c5", false, "Challenging d4", "Black attacks the center."),
                    OpeningMoveStep(7, "c3", "c2c3", true, "Pawn Pyramid", "The hallmark London pyramid, cementing d4 forever."),
                    OpeningMoveStep(8, "Nc6", "b8c6", false, "Adding Pressure", "Black increases pressure on d4."),
                    OpeningMoveStep(9, "Nd2", "b1d2", true, "Flexible Knight", "Develops without blocking the c-pawn and controls e4.")
                )
            )
        )
    )

    fun getOpeningById(id: String): ChessOpening? = allOpenings.find { it.id == id }
}
