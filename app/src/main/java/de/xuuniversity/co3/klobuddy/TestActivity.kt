package de.xuuniversity.co3.klobuddy

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.xuuniversity.co3.klobuddy.singletons.RoomDatabaseSingleton
import de.xuuniversity.co3.klobuddy.wc.WcEntity
import de.xuuniversity.co3.klobuddy.wc.WcRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        lifecycleScope.launch {

            val db = RoomDatabaseSingleton.getDatabase(this@TestActivity)
            val wcDao = db.wcDao()

            val flow = wcDao.getAllFavoritesByUserIDFlowDistinct(1)

            flow.collect {
                Log.d("DEBUG", it.toString())
            }

        }
    }

    fun button (view: View){
        val db = RoomDatabaseSingleton.getDatabase(this)
        val wcDao = db.wcDao()

        val entity = WcEntity(
            Random(123).nextInt(100000).toString(),
            "Test" + Random(123).nextInt(100000).toString(),
            1.0,
            1.0
        )

        runBlocking {
            launch {
                wcDao.upsertWcEntity(entity)
            }
        }

    }
}