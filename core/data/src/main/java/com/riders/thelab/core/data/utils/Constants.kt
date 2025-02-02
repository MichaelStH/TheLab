package com.riders.thelab.core.data.utils

object Constants {

    const val DATASTORE_THE_LAB_FILE_NAME = "THE_LAB_DATASTORE"
    const val APP_SEARCH_WEATHER_NAMESPACE = "weather_search"
    const val APP_SEARCH_WEATHER_CITIES_DATABASE_NAME = "cities"

    const val EMULATOR_DEVICE_TAG = "sdk"

    const val ANDROID_RES_PATH = "android.resource://"
    const val SEPARATOR = "/"

    /////////////////////////////////////////////
    //
    // TheLab API
    //
    /////////////////////////////////////////////
    private const val HTTP = "http://"

    //    private const val IP_ADDRESS = "192.168.0.136"
//        private const val IP_ADDRESS = "192.168.0.48"
    private const val IP_ADDRESS = "192.168.1.99"

    //    private const val EMULATOR_IP_ADDRESS = "192.168.0.163"
    private const val EMULATOR_IP_ADDRESS = "192.168.0.48"

    private const val PORT = ":8101"

    const val BASE_ENDPOINT_THE_LAB_URL: String = "https://the-lab-back.herokuapp.com"
    /*HTTP + if (LabDeviceManager.getModel().trim().lowercase()
            .contains("sdk")
    ) EMULATOR_IP_ADDRESS else IP_ADDRESS + PORT*/


    //REST client Base URL
    const val BASE_ENDPOINT_YOUTUBE = "https://raw.githubusercontent.com"
    const val BASE_ENDPOINT_SEARCH = "https://ajax.googleapis.com"
    const val BASE_ENDPOINT_FIREBASE_CLOUD_MESSAGING = "https://fcm.googleapis.com/"
    const val BASE_ENDPOINT_GOOGLE_DRIVE_V3_API = "https://www.googleapis.com/drive/v3/files/"
    const val BASE_ENDPOINT_GOOGLE_DRIVE_VIEW = "https://drive.google.com/uc?export=view&id="
    const val BASE_ENDPOINT_GOOGLE_FIREBASE_API = "https://firebasestorage.googleapis.com/"
    const val BASE_ENDPOINT_GOOGLE_MAPS_API = "https://maps.googleapis.com/maps/api/"
    const val BASE_ENDPOINT_GOOGLE_PLACES = "https://maps.googleapis.com/maps/api/place/"
    const val BASE_ENDPOINT_WEATHER = "https://api.openweathermap.org"
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
    const val BASE_ENDPOINT_SPOTIFY_ACCOUNT = "https://accounts.spotify.com/api/"
    const val BASE_ENDPOINT_SPOTIFY_API = "https://api.spotify.com/"
    const val BASE_ENDPOINT_TMDB_API = "https://api.themoviedb.org/3/"
    const val BASE_ENDPOINT_TMDB_IMAGE_W_ORIGINAL = "https://image.tmdb.org/t/p/original"
    const val BASE_ENDPOINT_TMDB_IMAGE_W_500 = "https://image.tmdb.org/t/p/w500"
    const val URL_TMDB_WEBSITE: String = "https://www.themoviedb.org/?language=fr"
    const val BASE_ENDPOINT_FLIGHT_AWARE_API = "https://aeroapi.flightaware.com/aeroapi/"
    const val BASE_ENDPOINT_WIKIMEDIA_API = "https://en.wikipedia.org/"

    // Palette
    const val PALETTE_URL = "http://i.ytimg.com/vi/aNHOfJCphwk/maxresdefault.jpg"

    // Video
    const val VIDEO_BUNNY_URL =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    const val BASE_ENDPOINT_YOUTUBE_BASE_URL = "http://www.youtube.com/v/"
    const val BASE_ENDPOINT_YOUTUBE_WATCH_BASE_URL = "https://www.youtube.com/watch?v="
    const val BASE_ENDPOINT_YOUTUBE_SHORT_BASE_URL = "https://youtu.be/"

    // Activity Recognition
    const val BROADCAST_DETECTED_ACTIVITY = "activity_intent"
    const val DETECTION_INTERVAL_IN_MILLISECONDS = (30 * 1000).toLong()
    const val CONFIDENCE = 70

    const val SZ_SEPARATOR = "/"

    const val GPS_REQUEST = 5214

    const val NOTIFICATION_ID: Int = 45532
    const val NOTIFICATION_CHANNEL_ID: String = "45532"
    const val NOTIFICATION_MUSIC_ID: Int = 3432
    const val NOTIFICATION_MUSIC_CHANNEL_ID: String = "3432"

    // WebView
    const val WEB_URL: String = "WEB_URL"
}