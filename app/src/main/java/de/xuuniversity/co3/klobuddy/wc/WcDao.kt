package de.xuuniversity.co3.klobuddy.wc

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface WcDao {
    @Query("SELECT * FROM WcEntity")
    suspend fun getAll(): List<WcEntity>

    @Upsert
    suspend fun upsertWcEntity(wcEntity: WcEntity)
}