package com.riders.thelab.core.location

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.riders.thelab.utils.Constants.GPS_REQUEST
import timber.log.Timber

/**
 * Reference :
 * Android Turn on GPS programmatically - Medium
 * https://droidbyme.medium.com/android-turn-on-gps-programmatically-d585cf29c1ef
 */
class GpsUtils(private val context: Context) {

    private var mContext: Context? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var locationManager: LocationManager? = null
    private var locationRequest: LocationRequest? = null

    init {

        this.mContext = context
        this.locationManager =
            mContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        this.mSettingsClient = LocationServices.getSettingsClient(mContext!!)
        this.locationRequest =
            LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, (10 * 1000).toLong())
                .setMinUpdateIntervalMillis((2 * 1000).toLong())
                .build()
        /*locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = (10 * 1000).toLong()
        locationRequest?.fastestInterval = (2 * 1000).toLong()*/

        /*val builder: LocationSettingsRequest.Builder =
            LocationSettingsRequest
                .Builder()
            .addLocationRequest(locationRequest!!)
        this.mLocationSettingsRequest = builder.build()*/
        this.mLocationSettingsRequest =
            LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)
                .setAlwaysShow(true) //this is the key ingredient
            .build()
        //**************************
        //**************************
        // builder.setAlwaysShow(true) //this is the key ingredient

        //**************************
    }

    // method for turn on GPS
    fun turnGPSOn(onGpsListener: OnGpsListener) {
        Timber.d("turnGPSOn()")

        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
            onGpsListener.gpsStatus(true)
        } else {
            mSettingsClient
                ?.checkLocationSettings(mLocationSettingsRequest!!)
                ?.addOnSuccessListener((context as Activity)) {
                    //  GPS is already enable, callback GPS status through listener
                    onGpsListener.gpsStatus(true)
                }
                ?.addOnFailureListener(context) { exception ->

                    when ((exception as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {

                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                val rae: ResolvableApiException =
                                    exception as ResolvableApiException
                                rae.startResolutionForResult(context, GPS_REQUEST)

                            } catch (sie: IntentSender.SendIntentException) {
                                Timber.i("PendingIntent unable to execute request.")
                            }
                        }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                            val errorMessage =
                                "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings."
                            Timber.e(errorMessage)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
        }
    }
}