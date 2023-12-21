package com.riders.thelab.core.data

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.firebase.storage.StorageReference
import com.riders.thelab.core.common.utils.LabParser
import com.riders.thelab.core.data.local.DbImpl
import com.riders.thelab.core.data.local.model.Contact
import com.riders.thelab.core.data.local.model.Download
import com.riders.thelab.core.data.local.model.SpotifyRequestToken
import com.riders.thelab.core.data.local.model.User
import com.riders.thelab.core.data.local.model.Video
import com.riders.thelab.core.data.local.model.app.App
import com.riders.thelab.core.data.local.model.app.PackageApp
import com.riders.thelab.core.data.local.model.weather.CityModel
import com.riders.thelab.core.data.local.model.weather.WeatherData
import com.riders.thelab.core.data.preferences.PreferencesImpl
import com.riders.thelab.core.data.remote.ApiImpl
import com.riders.thelab.core.data.remote.dto.ApiResponse
import com.riders.thelab.core.data.remote.dto.UserDto
import com.riders.thelab.core.data.remote.dto.artist.Artist
import com.riders.thelab.core.data.remote.dto.spotify.SpotifyResponse
import com.riders.thelab.core.data.remote.dto.spotify.SpotifyToken
import com.riders.thelab.core.data.remote.dto.weather.City
import com.riders.thelab.core.data.remote.dto.weather.OneCallWeatherResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Call
import timber.log.Timber
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    dbImpl: DbImpl,
    apiImpl: ApiImpl,
    preferencesImpl: PreferencesImpl
) : IRepository {

    companion object {
        private val TARGET_PACKAGES = arrayOf(
            "com.riders",
            "com.reepling",
            "com.praeter"
        )
    }

    private var mDbImpl: DbImpl = dbImpl
    private var mApiImpl: ApiImpl = apiImpl
    private var mPreferencesImpl: PreferencesImpl = preferencesImpl

    override fun getAppListFromAssets(context: Context): List<App> =
        LabParser.parseJsonFile<List<App>>(
            context = context,
            filename = "app_list.json"
        )!!

    override fun getPackageList(context: Context): List<App> {

        val installedAppList: List<ApplicationInfo> = ArrayList()

        val appList: MutableList<App> = ArrayList()

        if (isPackageExists(context, TARGET_PACKAGES)) {
            for (appInfo in installedAppList) {
                Timber.e("package found : %s", appInfo.packageName)
                try {

                    val icon: Drawable =
                        context.packageManager.getApplicationIcon(appInfo.packageName)
                    val pInfo: PackageInfo =
                        context.packageManager.getPackageInfo(appInfo.packageName, 0)
                    val version = pInfo.versionName
                    val packageName = appInfo.packageName
                    appList.add(
                        PackageApp(
                            context.packageManager.getApplicationLabel(appInfo).toString(),
                            icon,
                            version,
                            packageName
                        )
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
        } else {
            Timber.e("package %s not found.", TARGET_PACKAGES.contentToString())
            //installPackage(directory, targetApkFile);
        }

        return appList
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun isPackageExists(
        context: Context,
        targetPackages: Array<String>
    ): Boolean {
        var isPackageFound = false

        // First Method
        val packages: List<ApplicationInfo>
        val packageManager: PackageManager = context.packageManager
        packages = packageManager.getInstalledApplications(0)

        for (packageInfo in packages) {
            for (packageItem in targetPackages) {
                if (packageInfo.packageName.contains(packageItem)) {

                    // Store found app package name
                    val appToAdd = packageInfo.packageName

                    // Check if it does equal to The Lab package name
                    // because we don't don't want to display it
                    // TODO: Refactor
                    /*if (appToAdd != TheLabApplication.getInstance().getLabPackageName())
                        installedAppList.add(packageInfo)*/
                    isPackageFound = true
                }
            }
        }
        return isPackageFound

        // Second method
        /*try {
            PackageInfo info = packageManager
                    .getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return false
        }
        return true;
        */
    }


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

    override fun insertUser(user: User): Long = mDbImpl.insertUser(user)

    override fun insertAllUsers(users: List<User>) = mDbImpl.insertAllUsers(users)

    override fun getUsers(): Flow<List<User>> = mDbImpl.getUsers()

    override fun getUsersSync(): List<User> = mDbImpl.getUsersSync()

    override fun getUserByID(userId: Int): User = mDbImpl.getUserByID(userId)

    override fun getUserByName(lastname: String): User = mDbImpl.getUserByName(lastname)
    override fun getUserByUsername(username: String): User = mDbImpl.getUserByUsername(username)

    override fun getUserByEmail(email: String): User = mDbImpl.getUserByEmail(email)

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

    override suspend fun getStorageReference(activity: Activity): StorageReference? {
        return mApiImpl.getStorageReference(activity)
    }

    override suspend fun getArtists(url: String): List<Artist> {
        return mApiImpl.getArtists(url)
    }

    override suspend fun getVideos(): List<Video> {
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

    override suspend fun getApi(): ApiResponse = mApiImpl.getApi()

    override suspend fun login(user: UserDto) = mApiImpl.login(user)

    override suspend fun saveUser(user: UserDto) = mApiImpl.saveUser(user)
    override suspend fun getToken(requestToken: SpotifyRequestToken): SpotifyToken =
        mApiImpl.getToken(requestToken = requestToken)

    override suspend fun getToken(clientId: String, clientSecret: String): SpotifyToken =
        mApiImpl.getToken(clientId = clientId, clientSecret = clientSecret)

    override suspend fun getTrackInfo(bearerToken: String, trackId: String): SpotifyResponse =
        mApiImpl.getTrackInfo(bearerToken, trackId)

    override fun isNightMode(): Flow<Boolean> = mPreferencesImpl.isNightMode()

    override suspend fun toggleNightMode() = mPreferencesImpl.toggleNightMode()

    override fun isVibration(): Flow<Boolean> = mPreferencesImpl.isVibration()

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
}