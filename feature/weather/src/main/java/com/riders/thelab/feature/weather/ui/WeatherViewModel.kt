package com.riders.thelab.feature.weather.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.text.Editable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.riders.thelab.core.common.network.LabNetworkManager
import com.riders.thelab.core.common.network.NetworkState
import com.riders.thelab.core.common.utils.LabAddressesUtils
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import com.riders.thelab.core.common.utils.toLocation
import com.riders.thelab.core.data.IRepository
import com.riders.thelab.core.data.local.model.compose.weather.WeatherCityUIState
import com.riders.thelab.core.data.local.model.compose.weather.WeatherUIState
import com.riders.thelab.core.data.local.model.weather.CityModel
import com.riders.thelab.core.data.local.model.weather.WeatherData
import com.riders.thelab.core.data.remote.dto.weather.CurrentWeather
import com.riders.thelab.core.data.remote.dto.weather.OneCallWeatherResponse
import com.riders.thelab.core.ui.data.SnackBarType
import com.riders.thelab.core.ui.utils.UIManager
import com.riders.thelab.feature.weather.core.worker.WeatherDownloadWorker
import com.riders.thelab.feature.weather.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: IRepository
) : ViewModel() {

    //////////////////////////////////////////
    // Compose states
    //////////////////////////////////////////
    private var _weatherUiState: MutableStateFlow<WeatherUIState> =
        MutableStateFlow(WeatherUIState.Loading)
    val weatherUiState: StateFlow<WeatherUIState> = _weatherUiState
    private var _weatherCityUiState: MutableStateFlow<WeatherCityUIState> =
        MutableStateFlow(WeatherCityUIState.None)
    val weatherCityUiState: StateFlow<WeatherCityUIState> = _weatherCityUiState
    private var _searchText: MutableStateFlow<String> = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    // Network
    private lateinit var mNetworkState: StateFlow<NetworkState>
    private var hasInternetConnection: Boolean by mutableStateOf(false)
        private set

    var expanded by mutableStateOf(false)
        private set
    var isWeatherMoreDataVisible by mutableStateOf(false)
        private set
    var iconState by mutableStateOf(false)
        private set

    // Suggestions for search
    var suggestions: SnapshotStateList<CityModel> = mutableStateListOf()
        private set
    var weatherAddress: Address? by mutableStateOf(null)
        private set
    var cityMaxTemp by mutableStateOf("")
        private set
    var cityMinTemp by mutableStateOf("")
        private set

    fun updateUIState(state: WeatherUIState) {
        _weatherUiState.value = state
    }

    private fun updateWeatherCityUIState(cityState: WeatherCityUIState) {
        _weatherCityUiState.value = cityState
    }

    private fun updateHasInternetConnection(hasInternet: Boolean) {
        this.hasInternetConnection = hasInternet
    }


    fun updateSearchText(newSearchText: String) {
        this._searchText.value = newSearchText

        if (2 <= newSearchText.length) {
            if (null != mSearchJob && mSearchJob?.isActive == true) {
                mSearchJob?.cancel()
            }

            if (!this.expanded) {
                this.expanded = true
            }

            getCitiesFromDb(this.searchText.value)
        } else {
            this.expanded = false
        }
    }

    fun updateExpanded(expanded: Boolean) {
        this.expanded = expanded
    }

    private fun updateSuggestions(suggestions: List<CityModel>) {
        this.suggestions.clear()
        this.suggestions.addAll(suggestions)
    }

    private fun updateCityMaxTemp(newTemperature: Double) {
        this.cityMaxTemp = "${newTemperature.toInt()}"
    }

    private fun updateCityMinTemp(newTemperature: Double) {
        this.cityMinTemp = "${newTemperature.toInt()}"
    }

    private fun updateWeatherAddress(newAddress: Address) {
        this.weatherAddress = newAddress
    }

    fun updateMoreDataVisibility() {
        this.isWeatherMoreDataVisible = !isWeatherMoreDataVisible
    }

    fun updateIconState(iconState: Boolean) {
        this.iconState = iconState
    }


    //////////////////////////////////////////
    // Coroutines
    //////////////////////////////////////////
    private var mSearchJob: Job? = null

    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            Timber.e(throwable.message)
        }
    private val searchCityCoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            Timber.e("searchCityCoroutineExceptionHandler | message: ${throwable.message}")
            handleError(throwable)
        }


    ///////////////////////////
    //
    // OVERRIDE
    //
    ///////////////////////////
    override fun onCleared() {
        super.onCleared()
        Timber.e("onCleared()")
    }

    //////////////////////////////////////////
    //Live Data
    //////////////////////////////////////////
    private val workerStatus: MutableLiveData<WorkInfo.State> = MutableLiveData()
    private val isWeatherData: MutableLiveData<Boolean> = MutableLiveData()

    fun getWorkerStatus(): LiveData<WorkInfo.State> = workerStatus


    ///////////////////////////
    //
    // Class methods
    //
    ///////////////////////////
    fun observeNetworkState(networkManager: LabNetworkManager) {
        Timber.d("observeNetworkState()")
        mNetworkState = networkManager.networkState


        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            networkManager.getNetworkState().collect { networkState ->
                when (networkState) {
                    is NetworkState.Available -> {
                        Timber.d("network state is Available. All set.")
                        updateHasInternetConnection(true)
                    }

                    is NetworkState.Losing -> {
                        Timber.w("network state is Losing. Internet connection about to be lost")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Lost -> {
                        Timber.e("network state is Lost. Should not allow network calls initialization")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Unavailable -> {
                        Timber.e("network state is Unavailable. Should not allow network calls initialization")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Undefined -> {
                        Timber.i("network state is Undefined. Do nothing")
                    }
                }
            }
        }
    }

    private fun getCitiesFromDb(query: String) {
        Timber.d("getCitiesFromDb() | query: $query")

        mSearchJob =
            viewModelScope.launch(Dispatchers.IO + SupervisorJob() + searchCityCoroutineExceptionHandler) {
                delay(150L)

                if (query.isBlank()) {
                    repository.searchCity(query).all {
                        Timber.d("getCitiesFromDb() | query.isBlank() | Search: $it")
                        true
                    }
                } else {
                    val sanitizedQuery =
                        sanitizeSearchQuery(Editable.Factory.getInstance().newEditable(query))

                    // Replace % with * here
                    val results = repository.searchCity(sanitizedQuery).let {
                        Timber.d("getCitiesFromDb() | query.isNotBlank() | list: $it")
                        it
                    }

                    handleResults(results)
                }
            }
        viewModelScope.launch { mSearchJob?.join() }
    }

    private fun handleResults(cityModel: List<CityModel>) {
        Timber.d("handleResults() | cityModel size: ${cityModel.size}, update suggestions")
        updateSuggestions(cityModel)
    }

    private fun handleError(t: Throwable) {
        Timber.e("handleError() | Problem while Fetching City (cause: ${t.message})")
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                UIManager.showToast(context, "Problem while fetching city")
            }
        }
    }

    private fun sanitizeSearchQuery(query: Editable?): String {
        if (query == null) {
            return ""
        }

        val sanitizedQuery = query.replace(Regex.fromLiteral("\""), "\"\"")
        Timber.d("sanitizeSearchQuery() | sanitized query: $sanitizedQuery")

//        val newQuery = "*\'$query\'*"
        val newQuery = "'%$query%'"
        Timber.d("sanitizeSearchQuery() | new query: $newQuery")

        return newQuery
    }

    fun retry() {
        Timber.d("Retrying...")
        updateUIState(WeatherUIState.Loading)
    }

    @SuppressLint("NewApi")
    fun getCityNameWithCoordinates(
        activity: WeatherActivity,
        latitude: Double,
        longitude: Double
    ) {
        Timber.d("GetCityNameWithCoordinates()")

        val geocoder = Geocoder(activity, Locale.getDefault())

        if (!LabCompatibilityManager.isTiramisu()) {
            LabAddressesUtils.getDeviceAddressLegacy(
                geocoder,
                (latitude to longitude).toLocation()
            )?.let { updateWeatherAddress(it) }
        } else {
            LabAddressesUtils.getDeviceAddressAndroid13(
                geocoder,
                (latitude to longitude).toLocation()
            ) { address ->
                address?.let { updateWeatherAddress(it) }
            }
        }
    }

    fun fetchCities(activity: WeatherActivity) {
        Timber.d("fetchCities()")

        if (mNetworkState.value !is NetworkState.Available) {
            updateUIState(WeatherUIState.Error())
            return
        } else {
            updateUIState(WeatherUIState.Loading)

            viewModelScope.launch(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
                try {
                    // First step
                    // Call repository to check if there is data in database
                    val weatherData = repository.getWeatherData()

                    if (null == weatherData || !weatherData.isWeatherData) {

                        // In this case record's return is null
                        // then we have to call our Worker to perform
                        // the web service call to retrieve data from api
                        Timber.e("fetchCities() | List is empty. No Record found in database")

                        // Only for debug purposes
                        // Use worker to make long job operation in background
                        Timber.e("fetchCities() | Use worker to make long job operation in background...")

                        withContext(Dispatchers.IO) {
                            startWork(activity)
                        }
                    } else {
                        // In this case data already exists in database
                        // Load data then let the the user perform his request
                        Timber.d("Record found in database. Continue...")
                        withContext(Dispatchers.Main) {
                            isWeatherData.value = true
                            updateUIState(WeatherUIState.SuccessWeatherData(true))
                        }
                    }
                } catch (throwable: Exception) {
                    Timber.e("Error while fetching records in database")

//                    if (throwable is EmptyResultSetException) {
//                        Timber.e(throwable)
//                        Timber.e("weatherData is empty. No Record found in database")
//                        isWeatherData.value = false
//                    } else {
                    Timber.e(throwable)
//                    }
                }
            }
        }
    }


    fun fetchWeather(location: Location) {
        Timber.d("fetchWeather()")
        viewModelScope.launch(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
            try {
                val weatherResponse = repository.getWeatherOneCallAPI(location)

                withContext(Dispatchers.Main) {
                    weatherResponse?.let {
                        updateWeatherCityUIState(WeatherCityUIState.Success(it))
                    }
                }
            } catch (throwable: Exception) {
                Timber.e(throwable)
                withContext(Dispatchers.Main) {
                    updateUIState(WeatherUIState.Error())
                }
            }
        }
    }

    fun getMaxMinTemperature(hourlyWeather: List<CurrentWeather>) {
        Timber.d("getMaxMinTemperature() | hourlyWeather: $hourlyWeather")
        var minStoredTemperature: Double = hourlyWeather[0].temperature
        var maxStoredTemperature: Double = hourlyWeather[0].temperature

        hourlyWeather.forEach { temp ->
            if (minStoredTemperature >= temp.temperature) {
                minStoredTemperature = temp.temperature
            }
        }

        hourlyWeather.forEach { temp ->
            if (temp.temperature >= maxStoredTemperature) {
                maxStoredTemperature = temp.temperature
            }
        }
        updateCityMaxTemp(maxStoredTemperature)
        updateCityMinTemp(minStoredTemperature)
    }


    /////////////////////////////////////
    //
    // WORKER
    //
    /////////////////////////////////////
    /**
     * Launch Worker that will manage download and extraction of the cities zip file from bulk openweather server
     */
    @SuppressLint("RestrictedApi")
    fun startWork(activity: WeatherActivity) {
        Timber.d("startWork()")

        val workerConstraints: Constraints = Constraints.Builder()
            .apply {
                setRequiredNetworkType(NetworkType.CONNECTED)
                setRequiresBatteryNotLow(true)
                setRequiresCharging(false)
                setRequiresStorageNotLow(true)
            }
            .build()

        val weatherCitiesWorkRequest: WorkRequest =
            OneTimeWorkRequest.Builder(WeatherDownloadWorker::class.java)
                .setConstraints(workerConstraints)
                .setInputData(
                    Data.Builder()
                        .putString(
                            URL_REQUEST,
                            Constants.BASE_ENDPOINT_WEATHER_BULK_DOWNLOAD + Constants.WEATHER_BULK_DOWNLOAD_URL
                        )
                        .build()
                )
                .addTag(WeatherDownloadWorker::class.java.simpleName)
                .build()

        val id = weatherCitiesWorkRequest.id

        WorkManager
            .getInstance(activity)
            .enqueue(weatherCitiesWorkRequest)

        activity.runOnUiThread {
            listenToTheWorker(activity, id)
        }
    }


    private fun listenToTheWorker(activity: WeatherActivity, workerId: UUID) {
        Timber.d("listenToTheWorker : $workerId")

        WorkManager
            .getInstance(activity)
            .getWorkInfoByIdLiveData(workerId)
            .observe(activity) { workInfo: WorkInfo? ->
                workInfo?.let {
                    when (it.state) {
                        WorkInfo.State.ENQUEUED -> Timber.d("Worker ENQUEUED")
                        WorkInfo.State.RUNNING -> {
                            Timber.d("Worker RUNNING")
                            workerStatus.value = WorkInfo.State.RUNNING
                            updateUIState(WeatherUIState.Loading)
                        }

                        WorkInfo.State.SUCCEEDED -> {

                            // Save data in database
                            viewModelScope.launch(Dispatchers.IO) {
                                repository.insertWeatherData(WeatherData(true))
                            }

                            updateUIState(WeatherUIState.Success(OneCallWeatherResponse()))
                            workerStatus.value = WorkInfo.State.SUCCEEDED
                        }

                        WorkInfo.State.FAILED -> {
                            Timber.e("Worker FAILED")
                            workerStatus.value = WorkInfo.State.FAILED

                            activity.runOnUiThread {
                                UIManager.showActionInSnackBar(
                                    activity,
                                    "Worker FAILED",
                                    SnackBarType.ALERT,
                                    "",
                                    null
                                )
                            }

                            updateUIState(WeatherUIState.Error())
                        }

                        WorkInfo.State.BLOCKED -> Timber.e("Worker BLOCKED")
                        WorkInfo.State.CANCELLED -> Timber.e("Worker CANCELLED")
                        else -> {
                            Timber.e("Else branch")
                            updateUIState(WeatherUIState.Error())
                        }
                    }
                } ?: run {
                    Timber.e("listenToTheWorker() | WorkInfo is null")
                    updateUIState(WeatherUIState.Error())
                }
            }
    }

    fun clearBackgroundResources(activity: WeatherActivity) {
        cancelWorker(activity)
    }

    private fun cancelWorker(activity: WeatherActivity) {
        Timber.e("cancelWorker()")
        Timber.i("Worker is about to be cancelled")
        WorkManager
            .getInstance(activity)
            .cancelAllWork()
    }


    companion object {
        private val TAG = WeatherViewModel::class.java.simpleName

        const val MESSAGE_STATUS = "message_status"
        const val URL_REQUEST = "url_request"
        private const val WORK_RESULT = "work_result"
    }
}