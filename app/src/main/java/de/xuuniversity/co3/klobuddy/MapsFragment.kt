package de.xuuniversity.co3.klobuddy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import de.xuuniversity.co3.klobuddy.databinding.FragmentMapsBinding
import de.xuuniversity.co3.klobuddy.singletons.StatesSingleton
import de.xuuniversity.co3.klobuddy.wc.ReducedWcEntity
import de.xuuniversity.co3.klobuddy.wc.WcRepository
import kotlinx.coroutines.launch
import kotlin.math.*

const val FINE_PERMISSION_CODE = 1

// Coordinates of Berlin
private val DEFAULT_LOCATION = LatLng(52.519733068718935, 13.404793124702566)
private val RADIUS = 1.0

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var currentLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private var placedMarker : List<ReducedWcEntity> = listOf()

    private var cameraPosition: CameraPosition? = StatesSingleton.cameraPosition

    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getLastLocation()
    }


    override fun onPause() {
        StatesSingleton.cameraPosition = mMap.cameraPosition
        super.onPause()
    }

    private fun getLastLocation() {
        val task = if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_PERMISSION_CODE
            )
            return
        } else {
            fusedLocationProviderClient!!.lastLocation
        }
        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                val mapFragment = childFragmentManager
                    .findFragmentById(R.id.activity_map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = false
        mMap.setMaxZoomPreference(17f)
        mMap.setMinZoomPreference(12f)
        mMap.setLatLngBoundsForCameraTarget(LatLngBounds(LatLng(52.3, 13.0), LatLng(52.7, 13.8)))
        mMap.setOnCameraMoveListener {
            placeMarker(mMap.cameraPosition.target)
        }

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val location: LatLng = if (currentLocation != null) {
            LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        } else {
            DEFAULT_LOCATION
        }

        if(cameraPosition != null)
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition!!))
        else {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(16f))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        }

        mMap.addMarker(MarkerOptions()
            .position(location)
            .icon(BitmapDescriptorFactory.fromBitmap(Util.convertDrawableToBitmap(requireContext(), R.drawable.outline_my_location_24)))
            .title("Current Location")
        )

        placeMarker(DEFAULT_LOCATION)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(requireContext(), R.string.error_location_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterLocations(locations: List<ReducedWcEntity>, cameraPosition: LatLng): List<ReducedWcEntity> {
        val resultLocations = mutableListOf<ReducedWcEntity>()

        val baseLatRad = Math.toRadians(cameraPosition.latitude)
        val baseLonRad = Math.toRadians(cameraPosition.longitude)

        for (location in locations) {
            val latRad = Math.toRadians(location.latitude)
            val lonRad = Math.toRadians(location.longitude)

            val deltaLat = latRad - baseLatRad
            val deltaLon = lonRad - baseLonRad

            val a = sin(deltaLat / 2).pow(2) + cos(baseLatRad) * cos(latRad) * sin(deltaLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            val distanceInKilometers = 6371 * c // Radius of the Earth in kilometers

            if (distanceInKilometers <= RADIUS) {
                resultLocations.add(location)
            }
        }

        return resultLocations
    }

    private fun placeMarker(cameraPosition: LatLng){
        lifecycleScope.launch {
            val allReducedWcEntity = WcRepository.getAllReducedWcEntities(requireActivity())
            val newReducedWcEntities = allReducedWcEntity.toSet().minus(placedMarker.toSet()).toList()
            val filteredReducedWcEntities = filterLocations(newReducedWcEntities, cameraPosition)

            for (wc in filteredReducedWcEntities){

                mMap.addMarker(MarkerOptions()
                    .position(LatLng(wc.latitude, wc.longitude))
                    .title(wc.description)
                    .icon(BitmapDescriptorFactory.fromBitmap(Util.convertDrawableToBitmap(activity as Context, R.drawable.outline_wc_24)))
                )
            }

            placedMarker += filteredReducedWcEntities;
        }
    }
}
