package de.xuuniversity.co3.klobuddy.wc

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import de.xuuniversity.co3.klobuddy.favorite.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface WcDao {
    @Query("SELECT * FROM WcEntity")
    suspend fun getAll(): List<WcEntity>


    @Upsert
    suspend fun upsertWcEntity(wcEntity: WcEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertFavoriteEntity(favoriteEntity: FavoriteEntity)

    //Remove Favorite Entity
    @Query("DELETE FROM FavoriteEntity WHERE lavatoryID = :lavatoryID AND userID = :userID")
    suspend fun removeFavoriteEntity(lavatoryID: String?, userID: Int?)

    @Query("SELECT * FROM WcEntity")
    fun getAllFlow(): Flow<List<WcEntity>>

    @Query("""
        SELECT WcEntity.* FROM WcEntity
        JOIN FavoriteEntity ON WcEntity.lavatoryID = FavoriteEntity.lavatoryID
        WHERE FavoriteEntity.userID = :userID
    """)
    suspend fun getAllFavoritesByUserID(userID: Int): List<WcEntity>

    @Query("SELECT COUNT(*) > 0 FROM FavoriteEntity WHERE userID = :userID AND lavatoryID = :lavatoryID")
    suspend fun checkIfFavorite(lavatoryID: String?, userID: Int?): Boolean

    @Query("""
        SELECT WcEntity.* FROM WcEntity
        JOIN FavoriteEntity ON WcEntity.lavatoryID = FavoriteEntity.lavatoryID
        WHERE FavoriteEntity.userID = :userID
    """)
    fun getAllFavoritesByUserIDFlow(userID: Int): Flow<List<WcEntity>>

    fun getAllFavoritesByUserIDFlowDistinct(userID: Int): Flow<List<WcEntity>>{
        return getAllFavoritesByUserIDFlow(userID).distinctUntilChanged()
    }

}