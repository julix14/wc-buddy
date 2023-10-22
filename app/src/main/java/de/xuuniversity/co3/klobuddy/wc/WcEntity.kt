package de.xuuniversity.co3.klobuddy.wc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WcEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
)
