package de.xuuniversity.co3.klobuddy.wc

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface WcDao {
    @Query("SELECT * FROM WcEntity")
    suspend fun getAll(): List<WcEntity>

    //ReducedWcEntity
    @Query("SELECT WcEntity.lavatoryID, WcEntity.description, WcEntity.latitude, WcEntity.longitude FROM WcEntity")
    suspend fun getAllReduced(): List<ReducedWcEntity>


    @Upsert
    suspend fun upsertWcEntity(wcEntity: WcEntity)

    @Query("SELECT * FROM WcEntity")
    fun getAllFlow(): Flow<List<WcEntity>>

    @Query("""
        SELECT WcEntity.* FROM WcEntity
        JOIN FavoriteEntity ON WcEntity.lavatoryID = FavoriteEntity.lavatoryID
        WHERE FavoriteEntity.userID = :userID
    """)
    suspend fun getAllFavoritesByUserID(userID: Int): List<WcEntity>

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