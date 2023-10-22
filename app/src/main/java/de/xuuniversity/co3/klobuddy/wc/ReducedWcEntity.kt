package de.xuuniversity.co3.klobuddy.wc

import androidx.room.DatabaseView

@DatabaseView(
    "SELECT WcEntity.lavatoryID, WcEntity.description, WcEntity.latitude, WcEntity.longitude FROM WcEntity"
)
data class ReducedWcEntity(
    val lavatoryID: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)
