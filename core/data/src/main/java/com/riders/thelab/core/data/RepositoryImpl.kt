package com.riders.thelab.core.data

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.firebase.storage.StorageReference
import com.riders.thelab.core.data.local.DbImpl
import com.riders.thelab.core.data.local.model.Contact
import com.riders.thelab.core.data.local.model.Download
import com.riders.thelab.core.data.local.model.SpotifyRequestToken
import com.riders.thelab.core.data.local.model.User
import com.riders.thelab.core.data.local.model.weather.CityModel
import com.riders.thelab.core.data.local.model.weather.WeatherData
import com.riders.thelab.core.data.preferences.PreferencesImpl
import com.riders.thelab.core.data.remote.ApiImpl
import com.riders.thelab.core.data.remote.dto.ApiResponse
import com.riders.thelab.core.data.remote.dto.UserDto
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
import com.riders.thelab.core.data.remote.dto.weather.City
import com.riders.thelab.core.data.remote.dto.weather.OneCallWeatherResponse
import com.riders.thelab.core.data.remote.dto.wikimedia.WikimediaResponse
import com.riders.thelab.core.data.remote.dto.youtube.VideoDto
import kotlinx.coroutines.flow.Flow
import kotools.types.text.NotBlankString
import okhttp3.ResponseBody
import retrofit2.Call
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    dbImpl: DbImpl,
    apiImpl: ApiImpl,
    preferencesImpl: PreferencesImpl
) : IRepository {

    private var mDbImpl: DbImpl = dbImpl
    private var mApiImpl: ApiImpl = apiImpl
    private var mPreferencesImpl: PreferencesImpl = preferencesImpl


    private val mLocationData: MediatorLiveData<Boolean>
        get() = MediatorLiveData()

    override fun getLocationStatusData(): LiveData<Boolean> {
        return mLocationData
    }

    override fun addLocationStatusDataSource(data: LiveData<Boolean>) {
        mLocationData.addSource(data, mLocationData::setValue)
    }

    override fun removeLocationStatusDataSource(data: LiveData<Boolean>) {
        mLocationData.removeSource(data)
    }

    /////////////////////////
    //
    // DB
    //
    /////////////////////////
    override fun insertUser(user: User): Long = mDbImpl.insertUser(user)

    override fun insertAllUsers(users: List<User>) = mDbImpl.insertAllUsers(users)

    override fun getUsers(): Flow<List<User>> = mDbImpl.getUsers()

    override fun getUsersSync(): List<User> = mDbImpl.getUsersSync()

    override fun getUserByID(userId: Int): User = mDbImpl.getUserByID(userId)

    override fun getUserByName(lastname: String): User = mDbImpl.getUserByName(lastname)
    override fun getUserByUsername(username: String): User = mDbImpl.getUserByUsername(username)

    override fun getUserByEmail(email: String): User = mDbImpl.getUserByEmail(email)

    override fun getUserWithGoogle(email: String): User? = mDbImpl.getUserWithGoogle(email)

    override fun setUserLogged(userId: Int) = mDbImpl.setUserLogged(userId)
    override fun logUser(usernameOrMail: String, encodedPassword: String): User? =
        mDbImpl.logUser(usernameOrMail, encodedPassword)

    override fun logoutUser(userId: Int) = mDbImpl.logoutUser(userId)
    override fun deleteUser(userId: Int) = mDbImpl.deleteUser(userId)


    override fun insertContact(contact: Contact) {
        mDbImpl.insertContact(contact)
    }

    override suspend fun insertContactRX(contact: Contact): Long {
        return mDbImpl.insertContactRX(contact)
    }

    override fun insertAllContacts(contactDetails: List<Contact>) {
        mDbImpl.insertAllContacts(contactDetails)
    }

    override fun getContacts(): List<Contact> {
        return mDbImpl.getContacts()
    }

    override suspend fun getAllContacts(): List<Contact> {
        return mDbImpl.getAllContacts()
    }

    override fun clearData() {
        mDbImpl.clearData()
    }

    override suspend fun insertWeatherData(isWeatherData: WeatherData): Long {
        return mDbImpl.insertWeatherData(isWeatherData)
    }

    override suspend fun saveCity(city: CityModel): Long {
        return mDbImpl.saveCity(city)
    }

    override suspend fun saveCities(dtoCities: List<City>): List<Long> {
        return mDbImpl.saveCities(dtoCities)
    }

    override suspend fun searchCity(cityQuery: String): List<CityModel> =
        mDbImpl.searchCity(cityQuery)

    override suspend fun getWeatherData(): WeatherData? {
        return mDbImpl.getWeatherData()
    }

    override suspend fun getCities(): List<CityModel> {
        return mDbImpl.getCities()
    }

    override fun getCitiesCursor(query: String): Cursor {
        return mDbImpl.getCitiesCursor(query)
    }

    override fun deleteAll() {
        mDbImpl.deleteAll()
    }

    /////////////////////////
    //
    // API
    //
    /////////////////////////
    override suspend fun getStorageReference(activity: Activity): StorageReference? {
        return mApiImpl.getStorageReference(activity)
    }

    override suspend fun getArtists(url: String): List<Artist> {
        return mApiImpl.getArtists(url)
    }

    override suspend fun getVideos(): List<VideoDto> {
        return mApiImpl.getVideos()
    }

    override suspend fun getWeatherOneCallAPI(location: Location): OneCallWeatherResponse {
        return mApiImpl.getWeatherOneCallAPI(location)
    }

    override fun getBulkWeatherCitiesFile(): Call<ResponseBody> {
        return mApiImpl.getBulkWeatherCitiesFile()
    }

    override suspend fun getBulkDownload(context: Context): Flow<Download> {
        return mApiImpl.getBulkDownload(context)
    }

    override fun getDownloadManager(context: Context): DownloadManager =
        mApiImpl.getDownloadManager(context)

    override fun downloadFile(context: Context, url: String): Long =
        mApiImpl.downloadFile(context, url)

    override fun cancelDownload(downloadId: Long): Int = mApiImpl.cancelDownload(downloadId)
    override fun cancelDownloads(downloadIds: List<Long>): Int =
        mApiImpl.cancelDownloads(downloadIds)

    override suspend fun getTrendingMovies(): TMDBMovieResponse = mApiImpl.getTrendingMovies()

    override suspend fun getPopularMovies(): TMDBMovieResponse = mApiImpl.getPopularMovies()

    override suspend fun getUpcomingMovies(): TMDBMovieResponse = mApiImpl.getUpcomingMovies()

    override suspend fun getTrendingTvShows(): TMDBTvShowsResponse = mApiImpl.getTrendingTvShows()

    override suspend fun getPopularTvShows(): TMDBTvShowsResponse = mApiImpl.getPopularTvShows()

    override suspend fun getMovieVideos(movieID: Int): TMDBVideoResponse? =
        mApiImpl.getMovieVideos(movieID)

    override suspend fun getTvShowVideos(thShowID: Int): TMDBVideoResponse? =
        mApiImpl.getTvShowVideos(thShowID)

    override suspend fun getMovieCredits(movieID: Int): TMDBCreditsResponse? =
        mApiImpl.getMovieCredits(movieID)

    override suspend fun getApi(): ApiResponse = mApiImpl.getApi()

    override suspend fun login(user: UserDto) = mApiImpl.login(user)

    override suspend fun saveUser(user: UserDto) = mApiImpl.saveUser(user)
    override suspend fun getToken(requestToken: SpotifyRequestToken): SpotifyToken =
        mApiImpl.getToken(requestToken = requestToken)

    override suspend fun getToken(clientId: String, clientSecret: String): SpotifyToken =
        mApiImpl.getToken(clientId = clientId, clientSecret = clientSecret)

    override suspend fun getTrackInfo(bearerToken: String, trackId: String): SpotifyResponse =
        mApiImpl.getTrackInfo(bearerToken, trackId)

    override suspend fun getAirports(maxPages: Int, cursor: String?): AirportsResponse =
        mApiImpl.getAirports(maxPages, cursor)

    override suspend fun searchAirportById(query: String): AirportsSearchResponse =
        mApiImpl.searchAirportById(query)

    override suspend fun omniSearchAirport(query: String): AirportsSearchResponse =
        mApiImpl.omniSearchAirport(query)

    override suspend fun getAirportById(airportID: String): Airport =
        mApiImpl.getAirportById(airportID)

    override suspend fun getAirportFlightsById(
        airportID: String,
        maxPages: Int,
        cursor: String?,
        startDate: String?,
        endDate: String?,
        type: String?
    ): AirportFlightsResponse =
        mApiImpl.getAirportFlightsById(airportID, maxPages, cursor, startDate, endDate, type)

    override suspend fun searchFlightByRoute(
        departureAirportCode: NotBlankString,
        arrivalAirportCode: NotBlankString,
        type: FlightType,
        connection: NotBlankString?,
        startDate: NotBlankString?,
        endDate: NotBlankString?,
        maxPages: Int,
        cursor: String?
    ): SearchByRouteResponse = mApiImpl.searchFlightByRoute(
        departureAirportCode,
        arrivalAirportCode,
        type,
        connection,
        startDate,
        endDate,
        maxPages,
        cursor
    )

    override suspend fun getAirportNearBy(
        latitude: NotBlankString,
        longitude: NotBlankString,
        radius: Int,
        maxPages: Int,
        cursor: String?
    ): AirportsResponse = mApiImpl.getAirportNearBy(latitude, longitude, radius, maxPages, cursor)

    override suspend fun getOperators(maxPages: Int, cursor: String?): OperatorResponse =
        mApiImpl.getOperators(maxPages, cursor)

    override suspend fun getOperatorById(operatorID: String): Operator =
        mApiImpl.getOperatorById(operatorID)

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
    ): ByteArray = mApiImpl.getFlightOnMap(
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
    ): SearchFlightResponse = mApiImpl.searchFlight(query, maxPages, cursor)

    override suspend fun searchFlightByID(
        query: NotBlankString,
        type: NotBlankString?,
        startDate: NotBlankString?,
        endDate: NotBlankString?,
        maxPages: Int,
        cursor: String?
    ): SearchFlightResponse =
        mApiImpl.searchFlightByID(query, type, startDate, endDate, maxPages, cursor)

    override suspend fun getWikimediaResponse(query: NotBlankString): WikimediaResponse =
        mApiImpl.getWikimediaResponse(query)


    /////////////////////////
    //
    // PREFERENCES
    //
    /////////////////////////
    override fun isFirstTimeLaunched(): Flow<Boolean> = mPreferencesImpl.isFirstTimeLaunched()

    override suspend fun saveFirstTimeLaunched(firstTimeLaunched: Boolean) =
        mPreferencesImpl.saveFirstTimeLaunched(firstTimeLaunched)

    override fun isNightMode(): Flow<Boolean> = mPreferencesImpl.isNightMode()
    override suspend fun saveNightMode(isNightMode: Boolean) =
        mPreferencesImpl.saveNightMode(isNightMode)

    override suspend fun toggleNightMode() = mPreferencesImpl.toggleNightMode()

    override fun isVibration(): Flow<Boolean> = mPreferencesImpl.isVibration()
    override suspend fun saveVibration(isVibration: Boolean) =
        mPreferencesImpl.saveVibration(isVibration)

    override suspend fun toggleVibration() = mPreferencesImpl.toggleVibration()

    override fun getEmailPref(): Flow<String> = mPreferencesImpl.getEmailPref()

    override suspend fun saveEmailPref(email: String) = mPreferencesImpl.saveEmailPref(email)

    override fun getPasswordPref(): Flow<String> = mPreferencesImpl.getPasswordPref()

    override suspend fun savePasswordPref(password: String) =
        mPreferencesImpl.savePasswordPref(password)

    override fun isRememberCredentialsPref(): Flow<Boolean> =
        mPreferencesImpl.isRememberCredentialsPref()

    override suspend fun saveRememberCredentialsPref(isChecked: Boolean) =
        mPreferencesImpl.saveRememberCredentialsPref(isChecked)

    override fun getUserToken(): Flow<String> = mPreferencesImpl.getUserToken()

    override suspend fun saveTokenPref(token: String) = mPreferencesImpl.saveTokenPref(token)

    override fun isActivitiesSplashScreenEnabled(): Flow<Boolean> =
        mPreferencesImpl.isActivitiesSplashScreenEnabled()

    override suspend fun saveActivitiesSplashScreenEnabled(isActivitiesSplashScreenEnabled: Boolean) =
        mPreferencesImpl.saveActivitiesSplashScreenEnabled(isActivitiesSplashScreenEnabled)

    override suspend fun toggleActivitiesSplashScreenEnabled() =
        mPreferencesImpl.toggleActivitiesSplashScreenEnabled()
}