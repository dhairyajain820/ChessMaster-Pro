package com.example.chess.data.sync

import android.content.Context
import android.util.Log
import com.example.R
import com.example.chess.data.AppDatabase
import com.example.chess.data.GameRecordEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class SyncStatus(
    val isSyncing: Boolean = false,
    val totalSynced: Int = 0,
    val lastSyncMessage: String = "Ready"
)

class FirestoreSyncManager(private val context: Context) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )
    private val appDb = AppDatabase.getDatabase(context)
    private val auth = Firebase.auth

    private val _syncState = MutableStateFlow(SyncStatus())
    val syncState: Flow<SyncStatus> = _syncState.asStateFlow()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun saveGameLocallyAndSync(game: GameRecordEntity): Long = withContext(Dispatchers.IO) {
        // 1. Insert into local Room database
        val localId = appDb.gameRecordDao().insertGame(game)

        // 2. If user is signed in with Google, sync immediately to Firestore
        val uid = getCurrentUserId()
        if (uid != null) {
            try {
                pushGameToFirestore(uid, game.copy(id = localId))
            } catch (e: Exception) {
                Log.w("FirestoreSync", "Offline / delayed sync for game #$localId: ${e.message}")
            }
        }
        localId
    }

    suspend fun syncAllLocalGamesToCloud(): Result<Int> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
            ?: return@withContext Result.failure(IllegalStateException("Sign in with Google required to sync games."))

        _syncState.value = _syncState.value.copy(isSyncing = true, lastSyncMessage = "Syncing local games to cloud...")

        try {
            val localGames = appDb.gameRecordDao().getAllGames().first()
            var count = 0

            for (game in localGames) {
                pushGameToFirestore(uid, game)
                count++
            }

            _syncState.value = SyncStatus(
                isSyncing = false,
                totalSynced = count,
                lastSyncMessage = "Successfully synced $count games to Cloud Firestore!"
            )
            Result.success(count)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing games to Firestore", e)
            _syncState.value = SyncStatus(
                isSyncing = false,
                lastSyncMessage = "Sync error: ${e.localizedMessage ?: "Unknown error"}"
            )
            Result.failure(e)
        }
    }

    private suspend fun pushGameToFirestore(uid: String, game: GameRecordEntity) {
        val docId = "game_${game.id}_${game.timestamp}"
        val docRef = db.collection("users").document(uid).collection("games").document(docId)

        val data = hashMapOf(
            "userId" to uid,
            "gameId" to docId,
            "opponentName" to game.opponentName,
            "engineLevel" to game.engineLevel,
            "playerColor" to game.playerColor,
            "gameResult" to game.gameResult,
            "movesSan" to game.movesSan,
            "movesCount" to game.movesCount.toLong(),
            "playerAccuracy" to game.playerAccuracy.toLong(),
            "primaryWeakness" to game.primaryWeakness,
            "pgn" to game.pgn,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        docRef.set(data, SetOptions.merge()).await()
    }

    fun observeCloudGames(uid: String): Flow<List<CloudGameRecord>> {
        return db.collection("users")
            .document(uid)
            .collection("games")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(CloudGameRecord::class.java)
            }
            .catch { e ->
                Log.e("FirestoreSync", "Failed to observe cloud games", e)
                emit(emptyList())
            }
    }
}
