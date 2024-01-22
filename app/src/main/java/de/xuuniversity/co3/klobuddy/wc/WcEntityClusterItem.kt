package de.xuuniversity.co3.klobuddy.wc

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class WcEntityClusterItem (
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val userRating: Int? = 0,
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
    val modelTyp: Int? = null,): ClusterItem {



    override fun getPosition(): LatLng {
        return LatLng(latitude, longitude)
    }

    override fun getTitle(): String {
        return description
    }

    override fun getSnippet(): String {
        return "Rating: $averageRating"
    }

    override fun getZIndex(): Float? {
        return 0f
    }

    init {
        println("WcEntityClusterItem: $description")

    }


}