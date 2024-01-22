package de.xuuniversity.co3.klobuddy.wc

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import de.xuuniversity.co3.klobuddy.favorite.FavoriteEntity
import de.xuuniversity.co3.klobuddy.singletons.RoomDatabaseSingleton
import de.xuuniversity.co3.klobuddy.singletons.StatesSingleton
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

        val userId = StatesSingleton.userId

        db.collection("toilettes")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    coroutineScope.launch {

                        val favoriteList: ArrayList<*>? = document.data["userFavorites"] as ArrayList<*>?
                        val isFavorite = favoriteList?.any { (it is Long && it.toInt() == userId) || (it is Int && it == userId) } ?: false

                        val userRatings = document.data["userRatings"] as? Map<String, Long> ?: emptyMap()
                        val userRating = userRatings[userId.toString()] ?: 0
                        var averageRating = userRatings.values.average()
                        val ratingCount = userRatings.values.count()
                        if(averageRating.isNaN()){
                            averageRating = 0.0
                        }

                        val wcEntity = WcEntity(
                            lavatoryID = document.data["LavatoryID"].toString(),
                            description = document.data["Description"].toString(),
                            latitude = document.data["Latitude"].toString().replace(",", ".").toDouble(),
                            longitude = document.data["Longitude"].toString().replace(",", ".").toDouble(),
                            averageRating = averageRating,
                            ratingCount = ratingCount,
                            userRating = userRating.toInt(),
                        )

                        val dbInstance = RoomDatabaseSingleton.getDatabase(context)
                        val dao = dbInstance.wcDao()
                        dao.upsertWcEntity(wcEntity)

                        if(isFavorite){
                            val favoriteEntity = FavoriteEntity(
                                userID = userId,
                                lavatoryID = document.data["LavatoryID"].toString()
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

    suspend fun saveUserRating (context: Context, lavatoryID: String, rating: Float){
        val userId = StatesSingleton.userId

        //Save locally
        val localDb = RoomDatabaseSingleton.getDatabase(context)
        val wcDao = localDb.wcDao()

        wcDao.saveUserRating(lavatoryID, rating.toInt())
        Log.d("DEBUG", "User rating saved locally")

        //Save online
        val firestore = Firebase.firestore

        val updates = mapOf("userRatings.$userId" to rating.toInt())

        firestore.collection("toilettes").document(lavatoryID)
            .update(updates)
            .addOnSuccessListener {
                Log.d("DEBUG", "User rating saved online")
            }
            .addOnFailureListener { exception ->
                Log.w("DEBUG", "Error getting documents.", exception)
            }
    }

    //Adds Wc local and online to favorites of user
    suspend fun addWcToFavorites(context: Context, lavatoryID: String, userID: Int){
        //Local
        val db = RoomDatabaseSingleton.getDatabase(context)
        val wcDao = db.wcDao()

        val favoriteEntity = FavoriteEntity(
            userID = userID,
            lavatoryID = lavatoryID
        )

        wcDao.upsertFavoriteEntity(favoriteEntity)

        //Online
        val firestore = Firebase.firestore
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        firestore.collection("toilettes")
            .whereEqualTo("LavatoryID", lavatoryID)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    coroutineScope.launch {
                        val favoriteListFromDatabase: ArrayList<*>? = document.data["userFavorites"] as ArrayList<*>?
                        val favoriteList = favoriteListFromDatabase?.toMutableList() ?: mutableListOf()
                        val isFavorite = favoriteList.any { (it is Long && it.toInt() == userID) || (it is Int && it == userID) }

                        if(!isFavorite){
                            favoriteList.add(userID)
                            document.reference.update("userFavorites", favoriteList)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DEBUG", "Error getting documents.", exception)
            }

    }

    //Removes Wc local and online from favorites of user
    suspend fun removeWcFromFavorites(context: Context, lavatoryID: String, userID: Int){
        //Local
        val db = RoomDatabaseSingleton.getDatabase(context)
        val wcDao = db.wcDao()

        wcDao.removeFavoriteEntity(lavatoryID, userID)

        //Online
        val firestore = Firebase.firestore
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        firestore.collection("toilettes")
            .whereEqualTo("LavatoryID", lavatoryID)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    coroutineScope.launch {
                        val favoriteListFromDatabase: ArrayList<*>? = document.data["userFavorites"] as ArrayList<*>?
                        val favoriteList = favoriteListFromDatabase?.toMutableList() ?: mutableListOf()
                        val isFavorite = favoriteList.any { (it is Long && it.toInt() == userID) || (it is Int && it == userID) }

                        Log.d("DEBUG", "isFavorite: $isFavorite")


                        if(isFavorite){
                            favoriteList.remove(userID.toLong())
                            document.reference.update("userFavorites", favoriteList)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DEBUG", "Error getting documents.", exception)
            }
    }

    suspend fun checkIfFavorite(context: Context, lavatoryID: String, userID: Int): Boolean {
        val db = RoomDatabaseSingleton.getDatabase(context)
        val wcDao = db.wcDao()

        return wcDao.checkIfFavorite(lavatoryID, userID)
    }

    suspend fun updateAverageRating (context: Context, lavatoryID: String, averageRating: Double, ratingCount: Int){
        val db = RoomDatabaseSingleton.getDatabase(context)
        val wcDao = db.wcDao()

        Log.d("DEBUG", "Average rating updates locally, lavatoryID: $lavatoryID, averageRating: $averageRating, ratingCount: $ratingCount")

        wcDao.updateAverageRating(lavatoryID, averageRating, ratingCount)
        Log.d("DEBUG", "Average rating updated locally, lavatoryID: $lavatoryID, averageRating: $averageRating, ratingCount: $ratingCount")
    }

    suspend fun getAllFavoritesByUserID(context: Context, userID: Int): List<WcEntity> {
        val db = RoomDatabaseSingleton.getDatabase(context)
        val wcDao = db.wcDao()

        return wcDao.getAllFavoritesByUserID(userID)
    }

}

