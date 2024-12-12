package com.riders.thelab.core.common.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.lang.ref.WeakReference

class LabLocationManager(val context: Context) : LocationListener {

    private var mContext: Context = context

    // Declaring a Location Manager
    private var locationManager: LocationManager

    // flag for GPS status
    private var isGPSEnabled = false

    // flag for network status
    private var isNetworkEnabled = false

    // flag for GPS status
    private var canGetLocation = false

    // location
    private var location: Location? = null

    private var mLocationListener: LocationListener? = null

    private var mWeakReference: WeakReference<Activity>? = null

    constructor(
        activity: Activity,
        locationListener: LocationListener
    ) : this(activity.applicationContext) {
        this.mContext = activity
        this.mWeakReference = WeakReference(activity)
        this.mLocationListener = locationListener
    }

    init {
        Timber.d("LabLocationManager | init")
        locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    /////////////////////////////////////
    //
    // Composable states
    //
    /////////////////////////////////////
    private var _locationState: MutableStateFlow<Location?> = MutableStateFlow(null)
    val locationState: StateFlow<Location?> = _locationState

     fun updateLocationState(location: Location?) {
        Timber.d("updateLocationState() | location: $location")
        this._locationState.value = location
    }

    /////////////////////////////////////
    //
    // CLASS METHODS
    //
    /////////////////////////////////////
    fun setLocationListener() {
        mLocationListener = this
    }

    @WorkerThread
    fun getCurrentLocation(): Location? {
        Timber.d("getCurrentLocation()")

        return if (!canGetLocation()) {
            // no network provider is enabled
            Timber.e("no network provider is enabled")
            null
        } else {
            try {
                // if Network Enabled get lat/long using Network
                if (isNetworkEnabled) {
                    getLocationViaNetwork()
                }

                this.location?.let { updateLocationState(it) }

                // return location object
                return this.location
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    getLocationViaGPS()
                }

                this.location?.let { updateLocationState(it) }

                // return location object
                return this.location
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationViaNetwork() {
        mContext?.let {
            mLocationListener?.let { listener ->
                ContextCompat.getMainExecutor(it).execute {
                    // This is where your UI code goes.
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                        listener
                    )
                }
            }
        }

        Timber.d("Network Enabled")
        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
            updateLocationState(it)
            this@LabLocationManager.location = it
        } ?: run { Timber.e("Unable to get location via network provider") }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationViaGPS() {
        mContext?.let {
            mLocationListener?.let { listener ->
                ContextCompat.getMainExecutor(it).execute {
                    // This is where your UI code goes.
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                        listener
                    )
                }
            }
        }

        Timber.d("GPS Enabled")
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
            updateLocationState(it)
            this@LabLocationManager.location = it
        } ?: run { Timber.e("Unable to get location via gps provider") }
    }


    /**
     * Stop using GPS listener Calling this function will stop using GPS in your
     * app
     */
    fun stopUsingGPS() {
        locationManager.removeUpdates(this@LabLocationManager)
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    fun canGetLocation(): Boolean {
        try {
            Timber.d("canGetLocation() | isProviderEnabled(LocationManager.GPS_PROVIDER)")
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Timber.e("isGPSEnabled | exception message: ${ex.message}")
        }
        try {
            Timber.d("canGetLocation() | isProviderEnabled(LocationManager.NETWORK_PROVIDER)")
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Timber.e("isNetworkEnabled | exception message: ${ex.message}")
        }

        canGetLocation = isNetworkEnabled || isGPSEnabled

        return canGetLocation
    }

    /**
     * Function to show settings alert dialog On pressing Settings button will
     * lauch Settings Options
     */
    fun showSettingsAlert() {
        mContext?.let {
            AlertDialog.Builder(it).apply {

                // Setting Dialog Title
                setTitle("GPS is settings")

                // Setting Dialog Message
                setMessage("GPS is not enabled. Do you want to go to settings menu?")

                // On pressing Settings button
                setPositiveButton(
                    "Settings"
                ) { _: DialogInterface?, _: Int ->
                    val intent = Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                    context.startActivity(intent)
                }

                // on pressing cancel button
                setNegativeButton(
                    "Cancel"
                ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            }
                // Showing Alert Message
                .show()
        }
    }

    /////////////////////////////////////
    //
    // IMPLEMENTS
    //
    /////////////////////////////////////
    override fun onLocationChanged(location: Location) {
        Timber.d("onLocationChanged : $location")

        updateLocationState(location)
        this.location = location
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith(
            "Timber.d(\"onStatusChanged : \$provider, \$status\")",
            "timber.log.Timber"
        )
    )
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Timber.d("onStatusChanged : $provider, $status")
    }

    override fun onProviderDisabled(provider: String) {
        Timber.e("onProviderDisabled() | provider: $provider")

        /*mWeakReference?.let { ref ->

            // Get activity from weak reference activity object
            Timber.e("Get activity from weak reference activity object and call its methods")
            ref.get()?.let { activity ->
                when (activity) {
                    is WeatherActivity -> {
                        Timber.d("call WeatherActivity methods")
                        activity.updateLocationIcon(false)
                        activity.lifecycleScope.launch {
                            LocationProviderChangedEvent().triggerEvent(false)
                        }
                    }

                    else -> {
                        Timber.e("Else branch")
                    }
                }
            }
        }*/
        /*GlobalScope.launch {
            LocationProviderChangedEvent().triggerEvent(false)
        }*/
    }

    override fun onProviderEnabled(provider: String) {
        Timber.d("onProviderEnabled() | provider: $provider")
        /*mWeakReference?.let { ref ->

            // Get activity from weak reference activity object
            Timber.d("Get activity from weak reference activity object and call its methods")
            ref.get()?.let { activity ->
                when (activity) {
                    is WeatherActivity -> {
                        Timber.d("call WeatherActivity methods")
                        activity.updateLocationIcon(true)
                        activity.lifecycleScope.launch {
                            LocationProviderChangedEvent().triggerEvent(true)
                        }
                    }

                    else -> {
                        Timber.e("Else branch")
                    }
                }
            }
        }*/
    }


    /////////////////////////////////////
    //
    // COMPANION / INNER CLASSES
    //
    /////////////////////////////////////
    companion object {
        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters

        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong() // 1 minute

        private const val ACTIVITY_NAME_WEATHER = "WeatherActivity"

        @SuppressLint("StaticFieldLeak")
        private var mInstance: LabLocationManager? = null

        fun getInstance(activity: Activity): LabLocationManager =
            mInstance ?: LabLocationManager(activity).also {
                mInstance = it
            }

        fun getInstance(
            activity: Activity,
            locationListener: LocationListener
        ): LabLocationManager = mInstance ?: LabLocationManager(activity, locationListener).also {
            mInstance = it
        }
    }
}