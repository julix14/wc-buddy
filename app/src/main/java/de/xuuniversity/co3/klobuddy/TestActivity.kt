package de.xuuniversity.co3.klobuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.room.RoomDatabase
import de.xuuniversity.co3.klobuddy.singletons.RoomDatabaseSingleton
import de.xuuniversity.co3.klobuddy.wc.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val resultTextView = findViewById<TextView>(R.id.textView)

        lifecycleScope.launch {
            // This code will run on the background thread
            /*
            val db = withContext(Dispatchers.IO) {
                Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "WcEntity"
                )
                .createFromAsset("database/App.db")
                .build()
            }
            */

            val db = RoomDatabaseSingleton.getDatabase(applicationContext)

            Log.d("WcDao", db.toString())

            val wcDao = db.wcDao()
            val allWcs = withContext(Dispatchers.IO) {
                wcDao.getAll()
            }

            val resultText = allWcs.toString()
            resultTextView.text = resultText

            Log.d("WcDao", allWcs.toString())
        }
    }
}