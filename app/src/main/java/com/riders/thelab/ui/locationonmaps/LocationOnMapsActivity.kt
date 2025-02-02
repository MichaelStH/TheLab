package com.riders.thelab.ui.locationonmaps

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.res.Configuration
import android.graphics.Color
import android.location.Address
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.riders.thelab.R
import com.riders.thelab.core.common.utils.LabAddressesUtils
import com.riders.thelab.core.common.utils.LabAddressesUtils.getAddressToString
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import com.riders.thelab.core.common.utils.LabLocationManager
import com.riders.thelab.core.data.local.bean.MapsEnum
import com.riders.thelab.core.data.local.model.Permission
import com.riders.thelab.core.permissions.PermissionManager
import com.riders.thelab.core.ui.compose.base.BaseAppCompatActivity
import com.riders.thelab.core.ui.utils.UIManager
import com.riders.thelab.databinding.ActivityLocationOnMapsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class LocationOnMapsActivity
    : BaseAppCompatActivity(), OnMapReadyCallback, android.location.LocationListener {

    companion object {
        // location updates interval - 10sec
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

        // fastest updates interval - 5 sec
        // location updates will be received if another app is requesting the locations
        // than your app can handle
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
        private const val REQUEST_CHECK_SETTINGS = 100
    }

    private var _viewBinding: ActivityLocationOnMapsBinding? = null

    private val binding get() = _viewBinding!!

    private lateinit var mapFragment: SupportMapFragment
    private var mGeocoder: Geocoder? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mLocation: Location
    private lateinit var mLocationManager: LocationManager
    private var mCriteria: Criteria? = null
    private var mProvider: String? = null

    // bunch of location related apis
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null

    // boolean flag to toggle the ui
    private var mRequestingLocationUpdates: Boolean = false

    // location last updated time
    private var mLastUpdateTime: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        val nightModeFlags: Int = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                val w = window

                w.statusBarColor =
                    Color.TRANSPARENT
            }

            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }

        super.onCreate(savedInstanceState)
        _viewBinding = ActivityLocationOnMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PermissionManager
            .from(this@LocationOnMapsActivity)
            .request(Permission.Location)
            .rationale("Location is needed to discover some features")
            .checkPermission { granted: Boolean ->
                if (!granted) {
                    Timber.e("Permissions are denied. User may access to app with limited location related features")

                } else {
                    Timber.i("All permissions are granted")
                    mRequestingLocationUpdates = true
                    initLocationSettings()
                    setupMap()
                }
            }
    }

    @Deprecated("DEPRECATED - Use registerActivityForResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.e("onActivityResult()")
        // Check for the integer request code originally supplied to startResolutionForResult().
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    RESULT_OK -> Timber.e("User agreed to make required location settings changes.")
                    RESULT_CANCELED -> {
                        Timber.e("User chose not to make required location settings changes.")
                        mRequestingLocationUpdates = false
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        Timber.e("onPause()")
        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        // Resuming location updates depending on button state and
        // allowed permissions
        if (mRequestingLocationUpdates) {
            startLocationUpdates()
        }

        updateLocationUI()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                backPressed()
            }
        }
        return true
    }

    override fun backPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }


    /////////////////////////////////////
    //
    // IMPLEMENTS
    //
    /////////////////////////////////////
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     *
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        Timber.i("onMapReady()")
        mMap = googleMap

        // Used for finding current location with button
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        /*
            source :  https://stackoverflow.com/questions/36785542/how-to-change-the-position-of-my-location-button-in-google-maps-using-android-st
         */
        if (mapFragment.view != null) {
            Timber.d("Get the button view")
            // Get the button view
            val locationButton =
                (mapFragment.requireView().findViewById<View>("1".toInt()).parent as View)
                    .findViewById<View>("2".toInt())
            Timber.d("and next place it, on bottom right (as Google Maps app)")
            // and next place it, on bottom right (as Google Maps app)
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            Timber.d("position on right bottom")
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, 30, 260)
        }

        // Set a preference for minimum and maximum zoom.
        mMap.setMinZoomPreference(MapsEnum.WORLD.distance)
        mMap.setMaxZoomPreference(MapsEnum.DEFAULT_MAX_ZOOM.distance)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(MapsEnum.WORLD.distance))

        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            setLocationSettings()
            delay(800)
            hideLoading()
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onLocationChanged(location: Location) {

        // Remove markers
        mMap.clear()

        val latitude = location.latitude
        val longitude = location.longitude
        Timber.e("onLocationChanged() | latitude : ${location.latitude}, longitude: ${location.longitude}")

        val latLng = LatLng(location.latitude, location.longitude)

        if (!LabCompatibilityManager.isTiramisu()) {
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(
                        mGeocoder?.let { geocoder ->
                            val address = LabAddressesUtils
                                .getDeviceAddressLegacy(
                                    geocoder,
                                    location
                                )

                            address?.getAddressToString()

                        }
                    )
            )
        } else {
            mGeocoder?.let { geocoder ->
                LabAddressesUtils.getDeviceAddressAndroid13(
                    geocoder,
                    location
                ) { address ->

                    address?.let {
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(it.getAddressToString())
                        )
                    }
                }
            }
        }

        mMap.isMyLocationEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(750f), 2000, null)
        mMap.uiSettings.isScrollGesturesEnabled = true

        _viewBinding?.let {  binding.tvLocation.text = "Latitude:$latitude\nLongitude:$longitude" }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Timber.d("onStatusChanged()")
    }

    override fun onProviderEnabled(provider: String) {
        Timber.d("onProviderEnabled()")
    }

    override fun onProviderDisabled(provider: String) {
        Timber.d("onProviderDisabled()")
    }

    /////////////////////////////////////
    //
    // CLASS METHODS
    //
    /////////////////////////////////////
    private fun initLocationSettings() {
        Timber.i("initLocationSettings()")

        // Construct a FusedLocationProviderClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                // location is received
                mCurrentLocation = locationResult.lastLocation
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                updateLocationUI()
            }
        }
        mRequestingLocationUpdates = false
        @Suppress("DEPRECATION")
        mLocationRequest = LocationRequest().apply {
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().apply {
            addLocationRequest(mLocationRequest!!)
        }

        mLocationSettingsRequest = builder.build()
        mGeocoder = Geocoder(this, Locale.getDefault())
    }

    private fun setupMap() {
        Timber.i("setupMap()")
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.google_maps_fragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    private fun setLocationSettings() {
        Timber.i("setLocationSettings()")
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mCriteria = Criteria()
        mProvider = mLocationManager.getBestProvider(mCriteria!!, true)!!
        if (null == mProvider) {
            Timber.e("Cannot get location please enable position")
            val labLocationManager =
                LabLocationManager(activity = this@LocationOnMapsActivity, locationListener = this)
            labLocationManager.showSettingsAlert()
        } else {
            try {
                mLocation = mLocationManager.getLastKnownLocation(mProvider!!)!!
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            onLocationChanged(mLocation)
            try {
                mLocationManager.requestLocationUpdates(
                    mProvider!!,
                    20000,
                    0f,
                    this
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    private fun hideLoading() {
        if (binding.rlMapsLoading.visibility == View.VISIBLE)
            startAnimation(binding.rlMapsLoading)

    }

    private fun startAnimation(view: View) {
        Timber.d("startAnimation()")
        val animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        animFadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                Timber.d("onAnimationStart()")
            }

            override fun onAnimationEnd(animation: Animation) {
                Timber.d("onAnimationEnd()")
                view.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {
                Timber.d("onAnimationRepeat()")
            }
        })
        val mAnimationSet = AnimationSet(true)
        mAnimationSet.interpolator = AccelerateInterpolator()
        mAnimationSet.addAnimation(animFadeOut)
        view.startAnimation(mAnimationSet)
    }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mSettingsClient
            ?.checkLocationSettings(mLocationSettingsRequest!!)
            ?.addOnSuccessListener(this) { _: LocationSettingsResponse? ->

                Timber.i("All location settings are satisfied.")
                UIManager.showToast(this, "Started location updates!")

                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest!!,
                    mLocationCallback!!,
                    Looper.myLooper()!!
                )
                updateLocationUI()
            }
            ?.addOnFailureListener(this) { e: Exception ->
                val statusCode = (e as ApiException).statusCode
                if (statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                    Timber.i("Location settings are not satisfied. Attempting to upgrade location settings ")
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the
                        // result in onActivityResult().
                        val rae = e as ResolvableApiException
                        rae.startResolutionForResult(
                            this,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sie: SendIntentException) {
                        Timber.i("PendingIntent unable to execute request.")
                    }
                } else if (statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    val errorMessage =
                        "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings."
                    UIManager.showToast(this, errorMessage)
                    Timber.e(errorMessage)
                }
                updateLocationUI()
            }
    }

    private fun stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
            ?.removeLocationUpdates(mLocationCallback!!)
            ?.addOnCompleteListener(this) {
                UIManager.showToast(this, "Location updates stopped!")
            }
    }

    /**
     * Update the UI displaying the location data
     * and toggling the buttons
     */
    @SuppressLint("NewApi")
    private fun updateLocationUI() {
        Timber.d("updateLocationUI()")

        mCurrentLocation?.let { location ->
            Timber.e("Lat: ${mCurrentLocation!!.latitude} , ${mCurrentLocation!!.longitude}")
            mGeocoder = Geocoder(this, Locale.getDefault())

            mGeocoder?.let { geocoder ->

                var address: Address? = null

                if (!LabCompatibilityManager.isTiramisu()) {
                    address = LabAddressesUtils.getDeviceAddressLegacy(geocoder, location)
                    address?.let {
                        Timber.d("updateLocationUI | getDeviceAddressLegacy | Address: ${address.countryName}")
                    } ?: run { Timber.e("updateLocationUI | address object is null") }
                } else {
                    LabAddressesUtils.getDeviceAddressAndroid13(geocoder, location) {
                        if (null == address) {
                            Timber.e("updateLocationUI | address object is null")
                            return@getDeviceAddressAndroid13
                        }

                        Timber.d("updateLocationUI | getDeviceAddressAndroid13 | Address: $address")
                    }
                }
            } ?: run { Timber.e("Geocoder object is null") }
        } ?: run { Timber.e("Current location object is null") }
    }
}