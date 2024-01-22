package de.xuuniversity.co3.klobuddy.wc

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class WcEntityClusterItem(
    private val wcEntity: WcEntity,
    private val isFavorite: Boolean = false,
) : ClusterItem {


    override fun getPosition(): LatLng {
        return LatLng(wcEntity.latitude, wcEntity.longitude)
    }

    override fun getTitle(): String {
        return wcEntity.description
    }

    override fun getSnippet(): String {
        return ""
    }

    override fun getZIndex(): Float? {
        return 0f
    }

    fun getWcEntity(): WcEntity {
        return wcEntity
    }

    fun isFavorite(): Boolean {
        return isFavorite
    }
}