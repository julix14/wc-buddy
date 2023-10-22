package de.xuuniversity.co3.klobuddy.wc

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    entities = [WcEntity::class],
    views = [ReducedWcEntity::class],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wcDao(): WcDao

}