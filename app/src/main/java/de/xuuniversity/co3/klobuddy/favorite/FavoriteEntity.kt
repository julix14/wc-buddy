package de.xuuniversity.co3.klobuddy.favorite

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import de.xuuniversity.co3.klobuddy.wc.WcEntity

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = WcEntity::class,
            parentColumns = ["lavatoryID"],
            childColumns = ["lavatoryID"]
        )
    ]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userID: Int,
    val lavatoryID: String,
)
