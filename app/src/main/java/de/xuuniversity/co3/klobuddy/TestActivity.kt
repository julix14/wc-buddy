package de.xuuniversity.co3.klobuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import de.xuuniversity.co3.klobuddy.wc.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        lifecycleScope.launch {
            // This code will run on the background thread
            val db = withContext(Dispatchers.IO) {
                Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "WcEntity"
                ).build()
            }

            val wcDao = db.wcDao()
            val allWcs = withContext(Dispatchers.IO) {
                wcDao.getAll()
            }

            Log.d("WcDao", allWcs.toString())
        }
    }
}