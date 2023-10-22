package de.xuuniversity.co3.klobuddy.wc

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface WcDao {
    @Query("SELECT * FROM WcEntity")
    suspend fun getAll(): List<WcEntity>

    //ReducedWcEntity
    @Query("SELECT WcEntity.lavatoryID, WcEntity.description, WcEntity.latitude, WcEntity.longitude FROM WcEntity")
    suspend fun getAllReduced(): List<ReducedWcEntity>


    @Upsert
    suspend fun upsertWcEntity(wcEntity: WcEntity)
}