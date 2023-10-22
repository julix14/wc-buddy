package de.xuuniversity.co3.klobuddy

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.xuuniversity.co3.klobuddy.databinding.ActivityMapsBinding
import de.xuuniversity.co3.klobuddy.singletons.RoomDatabaseSingleton
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

const val FINE_PERMISSION_CODE = 1
//Coordinates of Berlin
const val DEFAULT_LATITUDE = 52.519733068718935
const val DEFAULT_LONGITUDE = 13.404793124702566
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var currentLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    private fun getLastLocation() {
        val task = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_PERMISSION_CODE
            )
            return
        } else {
            fusedLocationProviderClient!!.lastLocation
        }
        task.addOnSuccessListener { location ->
            if(location != null) {
                currentLocation = location
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.activity_map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = false

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val location : LatLng = if(currentLocation != null){
            LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        } else {
            LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
        }

        mMap.moveCamera(CameraUpdateFactory.zoomTo(16f))
        mMap.addMarker(MarkerOptions()
            .position(location)
            //Use XML as Icon
            .icon(BitmapDescriptorFactory.fromBitmap(Util.convertDrawableToBitmap(this, R.drawable.outline_my_location_24)))
            .title("Current Location")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        lifecycleScope.launch {
            val db = RoomDatabaseSingleton.getDatabase(applicationContext)

            val wcDao = db.wcDao()
            val allWcs = async {
                wcDao.getAll()
            }

            for (wc in allWcs.await()) {
                mMap.addMarker(MarkerOptions()
                    .position(LatLng(wc.latitude, wc.longitude))
                    .title(wc.description)
                    .icon(BitmapDescriptorFactory.fromBitmap(Util.convertDrawableToBitmap(this@MapsActivity, R.drawable.outline_wc_24)))
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(this, R.string.error_location_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }
}