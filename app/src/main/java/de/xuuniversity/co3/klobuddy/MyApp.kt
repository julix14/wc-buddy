package de.xuuniversity.co3.klobuddy

import android.app.Application
import de.xuuniversity.co3.klobuddy.wc.WcRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            WcRepository.upsertWcEntitiesFromFireStore(this@MyApp)
        }

    }
}