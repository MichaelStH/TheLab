package com.riders.thelab.core.data.remote

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.location.Location
import android.os.Environment
import android.widget.Toast
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import com.riders.thelab.core.data.BuildConfig
import com.riders.thelab.core.data.local.model.Download
import com.riders.thelab.core.data.local.model.SpotifyRequestToken
import com.riders.thelab.core.data.remote.api.ArtistsAPIService
import com.riders.thelab.core.data.remote.api.FlightApiService
import com.riders.thelab.core.data.remote.api.GoogleAPIService
import com.riders.thelab.core.data.remote.api.SpotifyAPIService
import com.riders.thelab.core.data.remote.api.SpotifyAccountAPIService
import com.riders.thelab.core.data.remote.api.TMDBApiService
import com.riders.thelab.core.data.remote.api.TheLabBackApiService
import com.riders.thelab.core.data.remote.api.WeatherApiService
import com.riders.thelab.core.data.remote.api.WeatherBulkApiService
import com.riders.thelab.core.data.remote.api.WikimediaApiService
import com.riders.thelab.core.data.remote.api.YoutubeApiService
import com.riders.thelab.core.data.remote.dto.ApiResponse
import com.riders.thelab.core.data.remote.dto.UserDto
import com.riders.thelab.core.data.remote.dto.youtube.VideoDto
import com.riders.thelab.core.data.remote.dto.artist.Artist
import com.riders.thelab.core.data.remote.dto.flight.Airport
import com.riders.thelab.core.data.remote.dto.flight.AirportFlightsResponse
import com.riders.thelab.core.data.remote.dto.flight.AirportsResponse
import com.riders.thelab.core.data.remote.dto.flight.AirportsSearchResponse
import com.riders.thelab.core.data.remote.dto.flight.FlightType
import com.riders.thelab.core.data.remote.dto.flight.Operator
import com.riders.thelab.core.data.remote.dto.flight.OperatorResponse
import com.riders.thelab.core.data.remote.dto.flight.SearchByRouteResponse
import com.riders.thelab.core.data.remote.dto.flight.SearchFlightResponse
import com.riders.thelab.core.data.remote.dto.spotify.SpotifyResponse
import com.riders.thelab.core.data.remote.dto.spotify.SpotifyToken
import com.riders.thelab.core.data.remote.dto.tmdb.TMDBCreditsResponse
import com.riders.thelab.core.data.remote.dto.tmdb.TMDBMovieResponse
import com.riders.thelab.core.data.remote.dto.tmdb.TMDBTvShowsResponse
import com.riders.thelab.core.data.remote.dto.tmdb.TMDBVideoResponse
import com.riders.thelab.core.data.remote.dto.weather.OneCallWeatherResponse
import com.riders.thelab.core.data.remote.dto.wikimedia.WikimediaResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import okhttp3.ResponseBody
import retrofit2.Call
import timber.log.Timber
import java.io.File
import java.util.Locale
import javax.inject.Inject

class ApiImpl @Inject constructor(
    artistsAPIService: ArtistsAPIService,
    googleAPIService: GoogleAPIService,
    youtubeApiService: YoutubeApiService,
    weatherApiService: WeatherApiService,
    weatherBulkApiService: WeatherBulkApiService,
    theLabBackApiService: TheLabBackApiService,
    spotifyAccountApiService: SpotifyAccountAPIService,
    spotifyApiService: SpotifyAPIService,
    tmdbApiService: TMDBApiService,
    flightApiService: FlightApiService,
    wikimediaService: WikimediaApiService
) : IApi {

    private var mArtistsAPIService: ArtistsAPIService = artistsAPIService
    private var mGoogleAPIService: GoogleAPIService = googleAPIService
    private var mYoutubeApiService: YoutubeApiService = youtubeApiService
    private var mWeatherApiService: WeatherApiService = weatherApiService
    private var mWeatherBulkApiService: WeatherBulkApiService = weatherBulkApiService
    private var mTheLabBackApiService: TheLabBackApiService = theLabBackApiService
    private var mSpotifyAccountApiService: SpotifyAccountAPIService = spotifyAccountApiService
    private var mSpotifyApiService: SpotifyAPIService = spotifyApiService
    private var mTmdbApiService: TMDBApiService = tmdbApiService
    private var mFlightApiService: FlightApiService = flightApiService
    private var mWikimediaApiService: WikimediaApiService = wikimediaService

    private var downloadManager: DownloadManager? = null

    override suspend fun getStorageReference(activity: Activity): StorageReference? = try {
        Timber.e("getStorageReference()")
        val storage = arrayOfNulls<FirebaseStorage>(1)
        var storageRef: StorageReference? = null

        // Initialize Firebase Auth
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

        val task = mAuth.signInAnonymously().await()

        task.user?.let {
            Timber.d("signInAnonymously:success : $it")
        }

        if (null != mAuth.currentUser) {
            // Sign in success, update UI with the signed-in user's information
            Timber.d("signInAnonymously:success")

            if (BuildConfig.DEBUG) {
                Timber.d("auth user: ${mAuth.currentUser}")
            }

            val bucketName = "gs://the-lab-3920e.appspot.com"
            storage[0] = FirebaseStorage.getInstance(bucketName)
            // Create a storage reference from our app
            storageRef = storage[0]!!.reference
        }
        storageRef

    } catch (exception: Exception) {

        // If sign in fails, display a message to the user.
        Timber.w("signInAnonymously:failure %s", exception.toString())
        activity.runOnUiThread {
            Toast.makeText(
                activity,
                "Authentication failed.",
                Toast.LENGTH_SHORT
            ).show()
        }

        null
    }


    override suspend fun getArtists(url: String): List<Artist> = mArtistsAPIService.getArtists(url)

    override suspend fun getVideos(): List<VideoDto> = mYoutubeApiService.fetchYoutubeVideos()

    override suspend fun getWeatherOneCallAPI(location: Location): OneCallWeatherResponse =
        mWeatherApiService
            .getCurrentWeatherWithNewOneCallAPI(
                location.latitude,
                location.longitude
            )

    override fun getBulkWeatherCitiesFile(): Call<ResponseBody> =
        mWeatherBulkApiService.getCitiesZipFile()

    override suspend fun getBulkDownload(context: Context): Flow<Download> =
        mWeatherBulkApiService.getCitiesGZipFile()
            .downloadCitiesFile(
                context.filesDir!!,
                "my_file"
            )


    private fun ResponseBody.downloadCitiesFile(
        directory: File,
        filename: String
    ): Flow<Download> = flow {

        emit(Download.Started(true))
        Timber.d("downloadToFileWithProgress()")
        emit(Download.Progress(0))

        // flag to delete file if download errors or is cancelled
        var deleteFile = true
        val file = File(directory, "${filename}.${contentType()?.subtype}")

        try {

            byteStream().use { inputStream ->
                file.outputStream().use { outputStream ->
                    val totalBytes = contentLength()
                    val data = ByteArray(8_192)
                    var progressBytes = 0L

                    while (true) {
                        val bytes = inputStream.read(data)

                        if (bytes == -1) {
                            break
                        }

                        outputStream.channel
                        outputStream.write(data, 0, bytes)
                        progressBytes += bytes

                        val progress = ((progressBytes * 100) / totalBytes).toInt()
                        Timber.d("downloadToFileWithProgress() | progress: $progress")
                        emit(Download.Progress(percent = ((progressBytes * 100) / totalBytes).toInt()))
                    }

                    when {
                        progressBytes < totalBytes -> {
                            emit(Download.Done(true))
                            throw Exception("missing bytes")
                        }

                        progressBytes > totalBytes -> {
                            emit(Download.Done(true))
                            throw Exception("too many bytes")
                        }

                        else ->
                            deleteFile = false
                    }
                }
            }

            emit(Download.Finished(file))
        } catch (exception: Exception) {
            exception.printStackTrace()
            emit(Download.Error(true))
        } finally {
            // check if download was successful
            emit(Download.Done(true))

            if (deleteFile) {
                file.delete()
            }
        }
    }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()


    override suspend fun getApi(): ApiResponse = mTheLabBackApiService.getApi()

    override suspend fun login(user: UserDto): ApiResponse = mTheLabBackApiService.login(user)

    override suspend fun saveUser(user: UserDto): ApiResponse = mTheLabBackApiService.saveUser(user)
    override suspend fun getToken(requestToken: SpotifyRequestToken): SpotifyToken =
        mSpotifyAccountApiService.getToken(request = requestToken)

    override suspend fun getToken(
        clientId: String,
        clientSecret: String
    ): SpotifyToken =
        mSpotifyAccountApiService.getToken(clientId = clientId, clientSecret = clientSecret)

    override suspend fun getTrackInfo(bearerToken: String, trackId: String): SpotifyResponse =
        mSpotifyApiService.getTrackInfo(bearerToken, trackId)

    override fun getDownloadManager(context: Context): DownloadManager {
        if (null == downloadManager) {
            Timber.e("Download Manager object is null")
            this.downloadManager =
                context.getSystemService(DownloadManager::class.java) as DownloadManager
        }

        return downloadManager as DownloadManager
    }


    @SuppressLint("NewApi")
    override fun downloadFile(context: Context, url: String): Long {
        Timber.d("downloadFile() | url: $url")

        if (null == downloadManager) {
            Timber.e("Download Manager object is null")
            this.downloadManager = getDownloadManager(context)
        }

        val fileName = url.substring(url.lastIndexOf('/') + 1).run {
            this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1)
        }

        val request = DownloadManager.Request(url.toUri())
            .apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                // Title of the Download Notification
                setTitle(fileName)
                // Description of the Download Notification
                setDescription("Downloading")

                if (LabCompatibilityManager.isNougat()) {
                    // Set if charging is required to begin the download
                    setRequiresCharging(false)
                }

                // Uri of the destination file
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }

        return downloadManager!!.enqueue(request)
    }

    override fun cancelDownload(downloadId: Long): Int = downloadManager!!.remove(downloadId)

    override fun cancelDownloads(downloadIds: List<Long>): Int {
        var totalDownloadStooped = 0
        downloadIds.forEach { totalDownloadStooped += downloadManager!!.remove(it) }
        return totalDownloadStooped
    }

    override suspend fun getTrendingMovies(): TMDBMovieResponse =
        mTmdbApiService.getTrendingMovies()

    override suspend fun getPopularMovies(): TMDBMovieResponse = mTmdbApiService.getPopularMovies()

    override suspend fun getUpcomingMovies(): TMDBMovieResponse =
        mTmdbApiService.getUpcomingMovies()

    override suspend fun getTrendingTvShows(): TMDBTvShowsResponse =
        mTmdbApiService.getTrendingTvShows()

    override suspend fun getPopularTvShows(): TMDBTvShowsResponse =
        mTmdbApiService.getPopularTvShows()

    override suspend fun getMovieVideos(movieID: Int): TMDBVideoResponse? =
        mTmdbApiService.getMovieVideos(movieID)

    override suspend fun getTvShowVideos(thShowID: Int): TMDBVideoResponse? =
        mTmdbApiService.getTvShowVideos(thShowID)

    override suspend fun getMovieCredits(movieID: Int): TMDBCreditsResponse? =
        mTmdbApiService.getMovieCredits(movieID)

    override suspend fun getAirports(maxPages: Int, cursor: String?): AirportsResponse =
        mFlightApiService.getAirports(maxPages, cursor)

    override suspend fun searchAirportById(query: String): AirportsSearchResponse =
        mFlightApiService.searchAirportById(query)

    override suspend fun omniSearchAirport(query: String): AirportsSearchResponse =
        mFlightApiService.omniSearchAirport(searchTerm = query, query = query)

    override suspend fun getAirportById(airportID: String): Airport =
        mFlightApiService.getAirportById(airportID)

    override suspend fun getAirportFlightsById(
        airportID: String,
        maxPages: Int,
        cursor: String?,
        startDate: String?,
        endDate: String?,
        type: String?
    ): AirportFlightsResponse = mFlightApiService.getAirportFlightsById(
        airportID,
        maxPages,
        cursor,
        startDate,
        endDate,
        type
    )

    override suspend fun searchFlightByRoute(
        departureAirportCode: NotBlankString,
        arrivalAirportCode: NotBlankString,
        type: FlightType,
        connection: NotBlankString?,
        startDate: NotBlankString?,
        endDate: NotBlankString?,
        maxPages: Int,
        cursor: String?
    ): SearchByRouteResponse = mFlightApiService.searchFlightByRoute(
        departureAirportCode = departureAirportCode,
        arrivalAirportCode = arrivalAirportCode,
        type = FlightType.AIRLINE.type.toNotBlankString().getOrThrow(),
        connection = connection,
        startDate = startDate,
        endDate = endDate,
        maxPages = maxPages,
        cursor = cursor
    )

    /**
     * @return a list of airports located within a given distance from the given location.
     */
    override suspend fun getAirportNearBy(
        latitude: NotBlankString,
        longitude: NotBlankString,
        radius: Int,
        maxPages: Int,
        cursor: String?
    ): AirportsResponse =
        mFlightApiService.getAirportNearBy(latitude, longitude, radius, maxPages, cursor)

    override suspend fun getOperators(maxPages: Int, cursor: String?): OperatorResponse =
        mFlightApiService.getOperators(maxPages, cursor)

    override suspend fun getOperatorById(operatorID: String): Operator =
        mFlightApiService.getOperatorById(operatorID)

    override suspend fun getFlightOnMap(
        flightID: NotBlankString,
        height: Int,
        width: Int,
        layerOn: Array<NotBlankString>?,
        layerOff: Array<NotBlankString>?,
        showDataBlock: Boolean,
        airportExpandView: Boolean,
        showAirports: Boolean,
        boundingBox: Boolean
    ): ByteArray = mFlightApiService.getFlightOnMap(
        flightID,
        height,
        width,
        layerOn,
        layerOff,
        showDataBlock,
        airportExpandView,
        showAirports,
        boundingBox
    )

    override suspend fun searchFlight(
        query: NotBlankString,
        maxPages: Int,
        cursor: String?
    ): SearchFlightResponse = mFlightApiService.searchFlight(query, maxPages, cursor)

    override suspend fun searchFlightByID(
        query: NotBlankString,
        type: NotBlankString?,
        startDate: NotBlankString?,
        endDate: NotBlankString?,
        maxPages: Int,
        cursor: String?
    ): SearchFlightResponse = mFlightApiService.searchFlightByID(
        query = query,
        type = type,
        startDate = startDate,
        endDate = endDate,
        maxPages = maxPages,
        cursor = cursor
    )

    override suspend fun getWikimediaResponse(query: NotBlankString): WikimediaResponse =
        mWikimediaApiService.getWikimediaResponse(query)
}