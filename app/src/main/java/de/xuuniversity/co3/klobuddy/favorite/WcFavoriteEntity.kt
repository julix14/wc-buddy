package de.xuuniversity.co3.klobuddy.favorite

import androidx.room.Embedded
import androidx.room.Relation
import de.xuuniversity.co3.klobuddy.wc.WcEntity

data class WcFavoriteEntity(
    @Embedded val wcEntity: WcEntity,
    @Relation(
        parentColumn = "lavatoryID",
        entityColumn = "lavatoryID"
    )
    val favorites: List<FavoriteEntity>
)
