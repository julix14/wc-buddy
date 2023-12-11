package de.xuuniversity.co3.klobuddy.singletons

import com.google.android.gms.maps.model.CameraPosition
import de.xuuniversity.co3.klobuddy.wc.WcEntity

object StatesSingleton {
    var cameraPosition : CameraPosition? = null
    var favoriteWCEntities : List<WcEntity> = listOf()
    var userId : Int = 1
}