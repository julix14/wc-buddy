package de.xuuniversity.co3.klobuddy.wc

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.xuuniversity.co3.klobuddy.favorite.WcFavoriteEntity

@Dao
interface WcDao {
    @Query("SELECT * FROM WcEntity")
    suspend fun getAll(): List<WcEntity>

    //ReducedWcEntity
    @Query("SELECT WcEntity.lavatoryID, WcEntity.description, WcEntity.latitude, WcEntity.longitude FROM WcEntity")
    suspend fun getAllReduced(): List<ReducedWcEntity>


    @Upsert
    suspend fun upsertWcEntity(wcEntity: WcEntity)

    @Query("""
        SELECT WcEntity.* FROM WcEntity
        JOIN FavoriteEntity ON WcEntity.lavatoryID = FavoriteEntity.lavatoryID
        WHERE FavoriteEntity.userID = :userID
    """)
    suspend fun getAllFavoritesByUserID(userID: Int): List<WcEntity>
}