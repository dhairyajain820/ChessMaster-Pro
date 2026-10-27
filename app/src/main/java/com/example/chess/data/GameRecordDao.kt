package com.example.chess.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameRecordEntity): Long

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAllGames(): Flow<List<GameRecordEntity>>

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestGame(): GameRecordEntity?

    @Query("DELETE FROM game_records WHERE id = :gameId")
    suspend fun deleteGame(gameId: Long)

    @Query("SELECT COUNT(*) FROM game_records WHERE gameResult = 'WIN'")
    fun getWinCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_records WHERE gameResult = 'LOSS'")
    fun getLossCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_records WHERE gameResult = 'DRAW'")
    fun getDrawCount(): Flow<Int>
}
