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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.xuuniversity.co3.klobuddy.databinding.FragmentMapsBinding
import de.xuuniversity.co3.klobuddy.singletons.StatesSingleton
import de.xuuniversity.co3.klobuddy.wc.WcEntity
import de.xuuniversity.co3.klobuddy.wc.WcRepository
import kotlinx.coroutines.launch
import kotlin.math.*

const val FINE_PERMISSION_CODE = 1
const val RADIUS = 1.0

// Coordinates of Berlin
private val _defaultLocation = LatLng(52.519733068718935, 13.404793124702566)

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var currentLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private var placedMarker : List<WcEntity> = listOf()
    private val markersMap = mutableMapOf<String, Marker>()


    private var cameraPosition: CameraPosition? = StatesSingleton.cameraPosition
    private lateinit var wcInformationBottomSheet: LinearLayout

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

        wcInformationBottomSheet = view.findViewById(R.id.bottom_sheet_layout)
        wcInformationBottomSheet.visibility = View.GONE
    }


    override fun onPause() {
        if(::mMap.isInitialized){
            StatesSingleton.cameraPosition = mMap.cameraPosition
        }

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
        setupMapUI(mMap)
        setupCamera(mMap)
        setupBottomSheet(mMap)

        placeMarker(_defaultLocation, RADIUS)
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

    private fun filterLocations(locations: List<WcEntity>, cameraPosition: LatLng, radius: Double): List<WcEntity> {
        val resultLocations = mutableListOf<WcEntity>()

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

            if (distanceInKilometers <= radius) {
                resultLocations.add(location)
            }
        }

        return resultLocations
    }

    private fun placeMarker(cameraPosition: LatLng, radius: Double){
        val userId = StatesSingleton.userId

        lifecycleScope.launch {
            val allReducedWcEntity = WcRepository.getAllWcEntities(requireActivity())
            val newReducedWcEntities = allReducedWcEntity.toSet().minus(placedMarker.toSet()).toList()
            val filteredReducedWcEntities = filterLocations(newReducedWcEntities, cameraPosition, radius)

            for (wc in filteredReducedWcEntities){

                val isFavorite = WcRepository.checkIfFavorite(requireContext(), wc.lavatoryID, userId)

                val marker = mMap.addMarker(MarkerOptions()
                    .position(LatLng(wc.latitude, wc.longitude))
                    .title(wc.description)
                    .icon(BitmapDescriptorFactory.fromBitmap(Util.convertDrawableToBitmap(activity as Context, R.drawable.outline_wc_24)))
                )
                marker?.tag = mapOf("entity" to wc, "favorite" to isFavorite)

                markersMap[wc.lavatoryID] = marker!!

            }

            placedMarker += filteredReducedWcEntities
        }
    }

    private fun setupMapUI(mMap: GoogleMap){
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = false
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setMaxZoomPreference(17f)
        mMap.setMinZoomPreference(12f)
        mMap.setLatLngBoundsForCameraTarget(LatLngBounds(LatLng(52.3, 13.0), LatLng(52.7, 13.8)))
    }

    private fun setupCamera(mMap: GoogleMap){
        mMap.setOnCameraMoveListener {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            // TODO: Chris need to implement this in the map, bc i am to scrared to destroy it
            val defaultZoomLevel =
                sharedPreferences.getString("zoom_level_preference", "14")?.toFloat()
            Log.d("DEBUG", "Zoom Level: $defaultZoomLevel")
            val zoomLevel = mMap.cameraPosition.zoom
            val radius: Double
            when {
                zoomLevel < 13 -> {
                    radius = RADIUS * 11
                }

                zoomLevel < 14 -> {
                    radius = RADIUS * 7
                }

                zoomLevel < 15 -> {
                    radius = RADIUS * 4
                }

                zoomLevel < 16 -> {
                    radius = RADIUS * 2
                }

                else -> {
                    radius = RADIUS
                }
            }

            placeMarker(mMap.cameraPosition.target, radius)

            //Close bottom sheet if open
            if (wcInformationBottomSheet.visibility == View.VISIBLE) {
                wcInformationBottomSheet.visibility = View.GONE
                BottomSheetBehavior.from(wcInformationBottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        val location: LatLng = if (currentLocation != null) {
            LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        } else {
            _defaultLocation
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
    }

    private fun setupBottomSheet(mMap: GoogleMap){
        mMap.setOnMarkerClickListener {
            if (it.tag == null) return@setOnMarkerClickListener (false)

            val tag = it.tag as Map<*, *>
            val tagWc = tag["entity"] as WcEntity
            val markerTag = markersMap[tagWc.lavatoryID]?.tag as Map<*, *>? ?: return@setOnMarkerClickListener (false)
            val wc = markerTag["entity"] as WcEntity
            val favorite = markerTag["favorite"] as Boolean

            setupBottomSheetContent(wc, favorite)

            mMap.animateCamera(
                CameraUpdateFactory.newLatLng(LatLng(wc.latitude, wc.longitude)),
                250,
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        wcInformationBottomSheet.visibility = View.VISIBLE
                    }

                    override fun onCancel() {
                        wcInformationBottomSheet.visibility = View.GONE
                    }
                })

            true
        }
    }

    private fun setupBottomSheetContent(wc: WcEntity, initialFavorite: Boolean){
        var favorite = initialFavorite
        // Write data to bottom sheet
        view?.findViewById<TextView>(R.id.wc_description)?.text = wc.description
        view?.findViewById<TextView>(R.id.wc_rating)?.text = String.format("%.1f",wc.averageRating)
        view?.findViewById<TextView>(R.id.wc_price)?.text = wc.price.toString()
        view?.findViewById<TextView>(R.id.wc_address)?.text = wc.street
        view?.findViewById<TextView>(R.id.wc_postal_code)?.text = wc.postalCode.toString()
        view?.findViewById<TextView>(R.id.wc_changing_table)?.text = wc.hasChangingTable.toString()
        view?.findViewById<TextView>(R.id.wc_urinal)?.text = wc.hasUrinal.toString()
        view?.findViewById<Button>(R.id.wc_toggle_favorite)?.text =
            if (!favorite) "Add to favorites" else "Remove from favorites"

        view?.findViewById<Button>(R.id.wc_toggle_favorite)?.let { button ->
            button.setOnClickListener {
                lifecycleScope.launch {
                    val userId = StatesSingleton.userId

                    if (favorite) {
                        Log.d("DEBUG", "Delete favorite")
                        WcRepository.removeWcFromFavorites(
                            requireContext(),
                            wc.lavatoryID,
                            userId
                        )
                    } else {
                        Log.d("DEBUG", "Add favorite")
                        WcRepository.addWcToFavorites(requireContext(), wc.lavatoryID, userId)
                    }

                    favorite = !favorite
                    activity?.runOnUiThread {
                        button.text =
                            if (!favorite) "Add to favorites" else "Remove from favorites"
                    }
                }
            }
        }

        var isProgrammaticChange = false
        val oldUserRating = wc.userRating ?: 0
        view?.findViewById<RatingBar>(R.id.ratingBar)?.setOnRatingBarChangeListener { _, rating, _ ->
            if(!isProgrammaticChange){
                lifecycleScope.launch {
                    WcRepository.saveUserRating(requireContext(), wc.lavatoryID, rating)
                    var ratingCount = wc.ratingCount
                    var averageRating: Double = wc.averageRating
                    Log.d("DEBUG", "Old Rating: $oldUserRating, New Rating: $averageRating")
                    if(oldUserRating != 0){
                        averageRating = removeFromAverageRating(averageRating, ratingCount, oldUserRating)
                        ratingCount--
                        Log.d("DEBUG", "Old Rating: $oldUserRating, New Rating: $averageRating")
                    }
                    averageRating = addToAverageRating(averageRating, ratingCount, rating.toInt())
                    ratingCount++
                    Log.d("DEBUG", "Old Rating: $oldUserRating, New Rating: $averageRating")

                    //Update Rating
                    val updatedWc = wc.copy(userRating = rating.toInt(), averageRating = averageRating, ratingCount = ratingCount)
                    markersMap[wc.lavatoryID]?.tag = mapOf("entity" to updatedWc, "favorite" to favorite)

                    //Save in local DB
                    WcRepository.updateAverageRating(requireContext(), wc.lavatoryID, averageRating, ratingCount)

                    //Display averageRating in UI
                    view?.findViewById<TextView>(R.id.wc_rating)?.text = String.format("%.1f", averageRating)
                }
            }
        }

        isProgrammaticChange = true
        view?.findViewById<RatingBar>(R.id.ratingBar)?.rating = oldUserRating.toFloat()
        isProgrammaticChange = false
    }

    private fun addToAverageRating(averageRating: Double, ratingCount: Int, newUserRating: Int): Double {
        val newAverageRating = (ratingCount * averageRating + newUserRating) / (ratingCount + 1)
        return newAverageRating.let { if (it.isNaN()) 0.0 else it}
    }

    private fun removeFromAverageRating(averageRating: Double, ratingCount: Int, oldUserRating: Int): Double {
        val newAverageRating = (ratingCount * averageRating - oldUserRating) / (ratingCount - 1)
        return newAverageRating.let { if (it.isNaN()) 0.0 else it}
    }
}
