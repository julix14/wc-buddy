package de.xuuniversity.co3.klobuddy.wc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import de.xuuniversity.co3.klobuddy.R
import de.xuuniversity.co3.klobuddy.singletons.RoomDatabaseSingleton
import kotlinx.coroutines.launch

class Test : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val db = Firebase.firestore

        db.collection("WcEntity")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    lifecycleScope.launch {
                        val db = RoomDatabaseSingleton.getDatabase(applicationContext)


                        var wcEntity = WcEntity(
                            document.id,
                            document.data["description"].toString(),
                            document.data["latitude"].toString().toDouble(),
                            document.data["longitude"].toString().toDouble()
                        )

                        val dao = db.wcDao()
                        val wcDao = dao.upsertWcEntity(wcEntity)



                        Log.d("TEST", "${document.id} => ${document.data}")

                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("TEST", "Error getting documents.", exception)
            }
    }
}