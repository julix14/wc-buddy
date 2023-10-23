package de.xuuniversity.co3.klobuddy.wc

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import de.xuuniversity.co3.klobuddy.singletons.RoomDatabaseSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WcRepository {
    suspend fun getAllReducedWcEntities(activity: Activity): List<ReducedWcEntity> {
        val db = RoomDatabaseSingleton.getDatabase(activity as Context)
        val wcDao = db.wcDao()

        return wcDao.getAllReduced()
    }

    suspend fun upsertReducedWcEntitiesFromFireStore(context: Context){
        val db = Firebase.firestore

        val coroutineScope = CoroutineScope(Dispatchers.IO)

        db.collection("WcEntity")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    coroutineScope.launch {

                        val dbInstance = RoomDatabaseSingleton.getDatabase(context)
                        // ToDO: Add type checks
                        val wcEntity = WcEntity(
                            document.id,
                            document.data["description"].toString(),
                            document.data["latitude"].toString().toDouble(),
                            document.data["longitude"].toString().toDouble()
                        )
                        val dao = dbInstance.wcDao()
                        dao.upsertWcEntity(wcEntity)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DEBUG", "Error getting documents.", exception)
            }
    }

    suspend fun getAllFavoritesByUserID(activity: Activity, userID: Int): List<WcEntity> {
        val db = RoomDatabaseSingleton.getDatabase(activity as Context)
        val wcDao = db.wcDao()

        return wcDao.getAllFavoritesByUserID(userID)
    }

}
