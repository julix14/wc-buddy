package de.xuuniversity.co3.klobuddy

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
import com.google.maps.android.clustering.ClusterManager
import de.xuuniversity.co3.klobuddy.databinding.FragmentMapsBinding
import de.xuuniversity.co3.klobuddy.singletons.StatesSingleton
import de.xuuniversity.co3.klobuddy.wc.WcEntity
import de.xuuniversity.co3.klobuddy.wc.WcEntityClusterItem
import de.xuuniversity.co3.klobuddy.wc.WcRepository
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

const val RADIUS = 1.0

// Coordinates of Berlin
private val _defaultLocation = LatLng(52.51430023974372, 13.410996312009937)

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var currentLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private var cameraPosition: CameraPosition? = StatesSingleton.cameraPosition
    private lateinit var wcInformationBottomSheet: CardView
    private lateinit var clusterManager: ClusterManager<WcEntityClusterItem>
    private var placedWcEntities: Set<String> = setOf()

    private val lastMapCallTime = AtomicLong(0)
    private val delayBetweenMapCalls = 500

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        checkLocationPermissionAndRetrieveLocation()

        wcInformationBottomSheet = view.findViewById(R.id.bottom_sheet_layout)
        wcInformationBottomSheet.visibility = View.GONE
    }


    override fun onPause() {
        if (::mMap.isInitialized) {
            StatesSingleton.cameraPosition = mMap.cameraPosition
        }

        super.onPause()
    }

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation()
            } else {
                showLocationPermissionExplanation()
            }
        }

    private fun checkLocationPermissionAndRetrieveLocation() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show an explanation to the user and try again
                showLocationPermissionExplanation()
            }

            else -> {
                locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getLastLocation() {
        val locationRequest = LocationRequest.Builder(10000L)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                if (!isAdded || activity == null || context == null) {
                    return
                }

                currentLocation = locationResult.lastLocation
                val mapFragment =
                    childFragmentManager.findFragmentById(R.id.activity_map) as SupportMapFragment
                mapFragment.getMapAsync(this@MapsFragment)
            }
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMapUI(mMap)
        setupCamera(mMap)

        cluster(mMap)
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun cluster(mMap: GoogleMap) {
        clusterManager = ClusterManager(context, mMap)

        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        clusterManager.setOnClusterItemClickListener { item ->
            setupBottomSheet(mMap, item)
            true
        }

        val zoomLevel = mMap.cameraPosition.zoom
        addItems(getRadiusForZoomLevel(zoomLevel))
    }

    private fun addItems(radius: Double) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMapCallTime.get() < delayBetweenMapCalls) {
            return
        }
        lastMapCallTime.set(currentTime)

        lifecycleScope.launch {
            val allWcEntities = WcRepository.getAllWcEntities(requireActivity())
            val onlyNewWcEntities =
                allWcEntities.filterNot { it.lavatoryID in placedWcEntities }
            val filteredWcEntities =
                filterLocations(
                    onlyNewWcEntities,
                    StatesSingleton.cameraPosition?.target ?: _defaultLocation,
                    radius
                )

            for (wc in filteredWcEntities) {

                if (wc.lavatoryID in placedWcEntities) {
                    continue
                }

                val isFavorite =
                    WcRepository.checkIfFavorite(
                        requireContext(),
                        wc.lavatoryID,
                        StatesSingleton.userId
                    )

                val clusterItem = WcEntityClusterItem(wc, isFavorite)
                clusterManager.addItem(clusterItem)

                placedWcEntities = placedWcEntities.plus(wc.lavatoryID)
                Log.d("DEBUG", "Placed WcEntities: ${clusterItem.getWcEntity().lavatoryID}")
            }
            clusterManager.cluster()

        }

    }

    private fun filterLocations(
        locations: List<WcEntity>,
        cameraPosition: LatLng,
        radius: Double
    ): List<WcEntity> {
        val resultLocations = mutableListOf<WcEntity>()

        val baseLatRad = Math.toRadians(cameraPosition.latitude)
        val baseLonRad = Math.toRadians(cameraPosition.longitude)

        for (location in locations) {
            val latRad = Math.toRadians(location.latitude)
            val lonRad = Math.toRadians(location.longitude)

            val deltaLat = latRad - baseLatRad
            val deltaLon = lonRad - baseLonRad

            val a =
                sin(deltaLat / 2).pow(2) + cos(baseLatRad) * cos(latRad) * sin(deltaLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            val distanceInKilometers = 6371 * c // Radius of the Earth in kilometers

            if (distanceInKilometers <= radius) {
                resultLocations.add(location)
            }
        }

        return resultLocations
    }

    private fun setupMapUI(mMap: GoogleMap) {
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

    private fun setupCamera(mMap: GoogleMap) {
        mMap.setOnCameraMoveListener {
            val zoomLevel = mMap.cameraPosition.zoom
            addItems(getRadiusForZoomLevel(zoomLevel))

            //Close bottom sheet if open
            if (wcInformationBottomSheet.visibility == View.VISIBLE) {
                wcInformationBottomSheet.visibility = View.GONE
                BottomSheetBehavior.from(wcInformationBottomSheet).state =
                    BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        val location: LatLng = if (currentLocation != null) {
            LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        } else {
            _defaultLocation
        }

        if (cameraPosition != null)
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition!!))
        else {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val defaultZoomLevel =
                sharedPreferences.getString("zoom_level_preference", "14")?.toFloat()
            Log.d("DEBUG", "Zoom Level: $defaultZoomLevel")
            mMap.moveCamera(CameraUpdateFactory.zoomTo(defaultZoomLevel!!))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

            // Location is out of bounds
            if (location.latitude < 52.3 || location.latitude > 52.7 || location.longitude < 13.0 || location.longitude > 13.8) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(_defaultLocation))
                showOutOfBoundsExplanation()
            }
        }

        mMap.addMarker(
            MarkerOptions()
                .position(location)
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        Util.convertDrawableToBitmap(
                            requireContext(),
                            R.drawable.outline_my_location_24
                        )
                    )
                )
                .title("Current Location")
        )
    }

    private fun setupBottomSheet(mMap: GoogleMap, item: WcEntityClusterItem) {
        val wc = item.getWcEntity()

        val favorite = item.isFavorite()

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
    }

    private fun setupBottomSheetContent(wc: WcEntity, initialFavorite: Boolean) {

        val icons = HashMap(
            mapOf(
                "changing_table" to R.drawable.baseline_baby_changing_station_24,
                "urinal" to R.drawable.baseline_urinal_24,
                "wheelchair" to R.drawable.baseline_accessible_24,
                "poorRating" to R.drawable.icon_selection_menu_star,
                "middleRating" to R.drawable.baseline_star_half_24,
                "goodRating" to R.drawable.outline_star_24,
                "fee" to R.drawable.baseline_attach_money_24,
                "no_fee" to R.drawable.baseline_money_off_24
            )
        )

        Log.d("DEBUG", "Wc: $wc")
        // Select icons to display
        val iconList = mutableListOf<String>()
        if (wc.hasChangingTable == 1) iconList.add("changing_table")
        if (wc.hasUrinal == 1) iconList.add("urinal")
        if (wc.isHandicappedAccessible == 1) iconList.add("wheelchair")
        when (wc.averageRating) {
            in 0.0..1.9 -> iconList.add("poorRating")
            in 2.0..3.9 -> iconList.add("middleRating")
            in 4.0..5.0 -> iconList.add("goodRating")
        }
        if (wc.price != null) {
            when (wc.price) {
                0.0 -> iconList.add("no_fee")
                else -> iconList.add("fee")
            }
        }
        handleThemeOnPeekIcons(iconList, icons)
        fillBottomSheetContent(wc, iconList)
        handleFavoriteButtonAndRating(wc, initialFavorite)
        handleRatingBar(wc)
    }


    private fun fillBottomSheetContent(wc: WcEntity, iconList: List<String>) {
        // Write data to bottom sheet
        view?.findViewById<TextView>(R.id.wc_bottom_sheet_description)?.text = wc.description
        if (wc.street != null && wc.postalCode != null && wc.city != null) {
            val address = arrayOf(wc.street, wc.postalCode, wc.city).joinToString(" ")
            val addressView = view?.findViewById<TextView>(R.id.wc_bottom_sheet_address)

            addressView?.text = address
            addressView?.visibility = View.VISIBLE
        }

        view?.findViewById<TextView>(R.id.wc_bottom_sheet_average_rating)?.text =
            String.format("%.1f", wc.averageRating)

        //Information Icon Text
        for (icon in iconList) {
            when (icon) {
                "changing_table" -> view?.findViewById<TextView>(R.id.wc_bottom_sheet_changing_table)?.visibility =
                    View.VISIBLE

                "urinal" -> view?.findViewById<TextView>(R.id.wc_bottom_sheet_urinal)?.visibility =
                    View.VISIBLE

                "wheelchair" -> view?.findViewById<TextView>(R.id.wc_bottom_sheet_wheelchair_accessible)?.visibility =
                    View.VISIBLE
            }
        }

        //Price
        if ((wc.price == null) || (wc.price.equals(0.0))) {
            view?.findViewById<TextView>(R.id.wc_bottom_sheet_price)?.text =
                getString(R.string.no_fee)
        } else {
            val price = wc.price.toString() + "€"
            view?.findViewById<TextView>(R.id.wc_bottom_sheet_price)?.text = price
        }
        handleThemeOnDrawableLeft()

    }

    private fun handleThemeOnDrawableLeft() {
        for (item in view?.findViewById<LinearLayout>(R.id.bottom_sheet_content)?.children!!) {
            if (item is TextView) {
                if (item.compoundDrawables[0] == null) continue
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    // Apply the ColorStateList to the Drawable wrapper
                    DrawableCompat.setTintList(
                        item.compoundDrawables[0],
                        ColorStateList.valueOf(Color.WHITE)
                    )

                } else {
                    // Remove the color filter to show the original color of the Drawable wrapper
                    DrawableCompat.setTintList(
                        item.compoundDrawables[0],
                        ColorStateList.valueOf(Color.BLACK)
                    )
                }
            }
        }
    }

    private fun handleThemeOnPeekIcons(iconList: List<String>, icons: HashMap<String, Int>) {
        val iconWrapper = view?.findViewById<LinearLayout>(R.id.bottom_sheet_icons)
        iconWrapper?.removeAllViews()

        for (icon in iconList) {
            val iconView = ImageView(requireContext())
            iconView.setImageResource(icons[icon] ?: continue)
            // Check if dark mode is enabled
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                // Apply the ColorStateList to the Drawable wrapper
                DrawableCompat.setTintList(iconView.drawable, ColorStateList.valueOf(Color.WHITE))

            } else {
                // Remove the color filter to show the original color of the Drawable wrapper
                DrawableCompat.setTintList(iconView.drawable, ColorStateList.valueOf(Color.BLACK))
            }
            iconWrapper?.addView(iconView)

        }

    }

    private fun handleFavoriteButtonAndRating(wc: WcEntity, initialFavorite: Boolean) {
        var favorite = initialFavorite
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


    }

    private fun handleRatingBar(wc: WcEntity) {
        var isProgrammaticChange = false
        val oldUserRating = wc.userRating ?: 0
        view?.findViewById<RatingBar>(R.id.ratingBar)
            ?.setOnRatingBarChangeListener { _, rating, _ ->
                if (!isProgrammaticChange) {
                    lifecycleScope.launch {
                        WcRepository.saveUserRating(requireContext(), wc.lavatoryID, rating)
                        var ratingCount = wc.ratingCount
                        var averageRating: Double = wc.averageRating
                        Log.d("DEBUG", "Old Rating: $oldUserRating, New Rating: $averageRating")
                        if (oldUserRating != 0) {
                            averageRating =
                                removeFromAverageRating(averageRating, ratingCount, oldUserRating)
                            ratingCount--
                            Log.d("DEBUG", "Old Rating: $oldUserRating, New Rating: $averageRating")
                        }
                        averageRating =
                            addToAverageRating(averageRating, ratingCount, rating.toInt())
                        ratingCount++
                        Log.d("DEBUG", "Old Rating: $oldUserRating, New Rating: $averageRating")

                        //Update entity
                        wc.userRating = rating.toInt()
                        wc.averageRating = averageRating
                        wc.ratingCount = ratingCount

                        //Save in local DB
                        WcRepository.updateAverageRating(
                            requireContext(),
                            wc.lavatoryID,
                            averageRating,
                            ratingCount
                        )

                        //Display averageRating in UI
                        view?.findViewById<TextView>(R.id.wc_bottom_sheet_average_rating)?.text =
                            String.format("%.1f", averageRating)
                    }
                }
            }

        isProgrammaticChange = true
        view?.findViewById<RatingBar>(R.id.ratingBar)?.rating = oldUserRating.toFloat()
        isProgrammaticChange = false
    }

    private fun addToAverageRating(
        averageRating: Double,
        ratingCount: Int,
        newUserRating: Int
    ): Double {
        val newAverageRating = (ratingCount * averageRating + newUserRating) / (ratingCount + 1)
        return newAverageRating.let { if (it.isNaN()) 0.0 else it }
    }

    private fun removeFromAverageRating(
        averageRating: Double,
        ratingCount: Int,
        oldUserRating: Int
    ): Double {
        val newAverageRating = (ratingCount * averageRating - oldUserRating) / (ratingCount - 1)
        return newAverageRating.let { if (it.isNaN()) 0.0 else it }
    }

    private fun showLocationPermissionExplanation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.location_permission_title)
            .setMessage(R.string.location_permission_message)
            .setPositiveButton("OK") { _, _ ->
                locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .create()
            .show()
    }

    private fun showOutOfBoundsExplanation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.out_of_bounds_title)
            .setMessage(R.string.out_of_bounds_message)
            .setPositiveButton("OK") { _, _ ->
                return@setPositiveButton
            }
            .create()
            .show()
    }

    private fun getRadiusForZoomLevel(zoomLevel: Float): Double {
        return when {
            zoomLevel < 13 -> {
                RADIUS * 11
            }

            zoomLevel < 14 -> {
                RADIUS * 7
            }

            zoomLevel < 15 -> {
                RADIUS * 4
            }

            zoomLevel < 16 -> {
                RADIUS * 2
            }

            else -> {
                RADIUS
            }
        }
    }
}
