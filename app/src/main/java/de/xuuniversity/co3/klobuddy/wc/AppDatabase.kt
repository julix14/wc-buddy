package de.xuuniversity.co3.klobuddy.wc

import androidx.room.Database
import androidx.room.RoomDatabase
import de.xuuniversity.co3.klobuddy.favorite.FavoriteEntity

@Database(
    version = 1,
    entities = [WcEntity::class, FavoriteEntity::class],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wcDao(): WcDao
}