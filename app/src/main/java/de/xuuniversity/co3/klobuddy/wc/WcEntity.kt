package de.xuuniversity.co3.klobuddy.wc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WcEntity(
    @PrimaryKey val lavatoryID: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double,
    val ratingCount: Int,
    val city: String? = null,
    val street: String? = null,
    val postalCode: Int? = null,
    val country: String? = null,
    val isHandicappedAccessible: Int? = null,
    val price: Double? = null,
    val canBePayedWithCoins: Int? = null,
    val canBePayedInApp: Int? = null,
    val canBePayedWithNFC: Int? = null,
    val hasChangingTable: Int? = null,
    val hasUrinal: Int? = null,
    val isOperatedBy: Int? = null,
    val modelTyp: Int? = null,
)
