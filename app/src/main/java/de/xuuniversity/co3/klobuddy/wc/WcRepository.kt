package de.xuuniversity.co3.klobuddy.wc

import android.app.Activity
import android.content.Context
import de.xuuniversity.co3.klobuddy.singletons.RoomDatabaseSingleton

object WcRepository {
    suspend fun getAllReducedWcEntities(activity: Activity): List<ReducedWcEntity> {
        val db = RoomDatabaseSingleton.getDatabase(activity as Context)
        val wcDao = db.wcDao()

        return wcDao.getAllReduced()
    }
}
