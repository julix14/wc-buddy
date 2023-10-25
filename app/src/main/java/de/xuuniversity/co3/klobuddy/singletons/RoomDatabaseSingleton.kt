package de.xuuniversity.co3.klobuddy.singletons

import android.content.Context
import androidx.room.Room
import de.xuuniversity.co3.klobuddy.wc.AppDatabase

object RoomDatabaseSingleton {

    private var db: AppDatabase? = null

    fun getDatabase(applicationContext: Context) : AppDatabase {
        if(db != null) return db as AppDatabase

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "WcEntity",
        )
            .createFromAsset("database/PopulationDatabase.db")
            .build()

        return db as AppDatabase
    }

}