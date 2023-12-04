package de.xuuniversity.co3.klobuddy.wc

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import de.xuuniversity.co3.klobuddy.favorite.FavoriteEntity
import de.xuuniversity.co3.klobuddy.singletons.RoomDatabaseSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WcRepository {
    suspend fun getAllWcEntities(activity: Activity): List<WcEntity> {
        val db = RoomDatabaseSingleton.getDatabase(activity as Context)
        val wcDao = db.wcDao()

        return wcDao.getAll()
    }

    suspend fun upsertWcEntitiesFromFireStore(context: Context){
        val db = Firebase.firestore

        val coroutineScope = CoroutineScope(Dispatchers.IO)

        // TODO: Hardcoded userId
        val userId: Int = 1

        db.collection("WcEntity")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    coroutineScope.launch {

                        val favoriteList: ArrayList<*>? = document.data["userFavorites"] as ArrayList<*>?
                        val isFavorite = favoriteList?.any { (it is Long && it.toInt() == userId) || (it is Int && it == userId) } ?: false

                        val wcEntity = WcEntity(
                            document.data["lavatoryID"].toString(),
                            document.data["description"].toString(),
                            document.data["latitude"].toString().toDouble(),
                            document.data["longitude"].toString().toDouble(),
                            0.0,
                            0,
                        )

                        val dbInstance = RoomDatabaseSingleton.getDatabase(context)
                        val dao = dbInstance.wcDao()
                        dao.upsertWcEntity(wcEntity)

                        if(isFavorite){
                            val favoriteEntity = FavoriteEntity(
                                userID = userId,
                                lavatoryID = document.data["lavatoryID"].toString()
                            )

                            dao.upsertFavoriteEntity(favoriteEntity)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DEBUG", "Error getting documents.", exception)
            }
    }

    suspend fun getAllFavoritesByUserID(context: Context, userID: Int): List<WcEntity> {
        val db = RoomDatabaseSingleton.getDatabase(context)
        val wcDao = db.wcDao()

        return wcDao.getAllFavoritesByUserID(userID)
    }

}
