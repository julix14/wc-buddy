package de.xuuniversity.co3.klobuddy

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.xuuniversity.co3.klobuddy.singletons.RoomDatabaseSingleton
import de.xuuniversity.co3.klobuddy.wc.WcRepository
import kotlinx.coroutines.launch

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        lifecycleScope.launch {



            val favorites = WcRepository.getAllFavoritesByUserID(this@TestActivity, 1)


            for (favorite in favorites) {
                Log.d("DEBUG", favorite.toString())
            }

        }
    }
}