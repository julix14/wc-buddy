package de.xuuniversity.co3.klobuddy.wc

import androidx.room.Dao
import androidx.room.Query

@Dao
fun interface WcDao {
    @Query("SELECT * FROM WcEntity")
    suspend fun getAll(): List<WcEntity>
}