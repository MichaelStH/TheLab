package com.riders.thelab.utils

import android.content.Context
import com.riders.thelab.data.local.model.app.App
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

class Constants @Inject constructor(
    @ApplicationContext val context: Context
) {

    companion object {
        const val EMULATOR_DEVICE_TAG = "sdk"

        //REST client Base URL
        const val BASE_ENDPOINT_YOUTUBE = "https://raw.githubusercontent.com"
        const val BASE_ENDPOINT_SEARCH = "https://ajax.googleapis.com"
        const val BASE_ENDPOINT_GOOGLE_FIREBASE_API = " https://firebasestorage.googleapis.com/"
        const val BASE_ENDPOINT_GOOGLE_MAPS_API = "https://maps.googleapis.com/maps/api/"
        const val BASE_ENDPOINT_GOOGLE_PLACES = "https://maps.googleapis.com/maps/api/place/"
        const val BASE_ENDPOINT_WEATHER = "http://api.openweathermap.org"
        const val BASE_ENDPOINT_WEATHER_BULK_DOWNLOAD = "http://bulk.openweathermap.org/"
        const val WEATHER_BULK_DOWNLOAD_URL = "sample/city.list.json.gz"
        const val BASE_ENDPOINT_WEATHER_ICON = "http://openweathermap.org/img/wn/"
        const val BASE_ENDPOINT_WEATHER_FLAG = "http://openweathermap.org/images/flags/"
        const val WEATHER_ICON_SUFFIX = "@2x.png"
        const val WEATHER_FLAG_PNG_SUFFIX = ".png"
        const val WEATHER_COUNTRY_CODE_FRANCE = "FR"
        const val WEATHER_COUNTRY_CODE_GUADELOUPE = "GP"
        const val WEATHER_COUNTRY_CODE_MARTINIQUE = "MQ"
        const val WEATHER_COUNTRY_CODE_GUYANE = "GF"
        const val WEATHER_COUNTRY_CODE_REUNION = "RE"
        const val FIREBASE_DATABASE_NAME = "kat"

        // Palette
        const val PALETTE_URL = "http://i.ytimg.com/vi/aNHOfJCphwk/maxresdefault.jpg"

        // Activity Recognition
        const val BROADCAST_DETECTED_ACTIVITY = "activity_intent"
        const val DETECTION_INTERVAL_IN_MILLISECONDS = (30 * 1000).toLong()
        const val CONFIDENCE = 70

        const val SZ_SEPARATOR = "/"

        const val GPS_REQUEST = 5214
    }

    /**
     * Return app testing list
     */
    fun getActivityList(): List<App> {
        return ArrayList(AppBuilderUtils.buildActivities(context))
    }
}