package de.xuuniversity.co3.klobuddy.wc

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WcEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract  fun wcDao(): WcDao
}