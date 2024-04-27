package com.riders.thelab.feature.flightaware.viewmodel

import android.app.Activity
import android.content.Context
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import com.riders.thelab.core.common.utils.LabLocationManager
import com.riders.thelab.core.data.BuildConfig
import com.riders.thelab.core.data.IRepository
import com.riders.thelab.core.data.local.model.flight.AirportModel
import com.riders.thelab.core.data.local.model.flight.AirportSearchModel
import com.riders.thelab.core.data.local.model.flight.toModel
import com.riders.thelab.core.data.remote.dto.flight.AirportSearch
import com.riders.thelab.core.data.remote.dto.flight.Flight
import com.riders.thelab.feature.flightaware.ui.main.FlightMainActivity
import com.riders.thelab.feature.flightaware.ui.main.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotools.types.experimental.ExperimentalKotoolsTypesApi
import kotools.types.text.NotBlankString
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
open class FlightSearchViewModel @Inject constructor(
    private val repository: IRepository
) : BaseFlightViewModel() {
    //////////////////////////////////////////
    // Variables
    //////////////////////////////////////////
    private var mLabLocationManager: LabLocationManager? = null
    var isOptionSelectedByUser: Boolean = false

    //////////////////////////////////////////
    // Composable states
    //////////////////////////////////////////

    var flightNumber: String by mutableStateOf("")
        private set

    var departureAirportQuery: String by mutableStateOf("")
        private set

    var departureAirportStateFlow: StateFlow<List<AirportSearchModel>> =
        snapshotFlow { departureAirportQuery }
            .debounce(750)
            .distinctUntilChanged()
            .mapLatest { latestDepartureAirportInput ->
                if (!hasInternetConnection) {
                    Timber.e("Internet connection not available. Make sure that you are connected to the internet to proceed to the search")
                    return@mapLatest emptyList()
                }
                if (latestDepartureAirportInput.isEmpty()) {
                    Timber.e("Input is empty. Cannot execute the query.")
                    return@mapLatest emptyList()
                }
                if (isOptionSelectedByUser) {
                    Timber.w("No need to perform action. user selection")
                    isOptionSelectedByUser = false
                    return@mapLatest emptyList()
                }

                Timber.d("departureAirportStateFlow | mapLatest | it: $latestDepartureAirportInput")
                val airportSearchList = getAirportByOmniSearch(latestDepartureAirportInput)
                Timber.d("result: $airportSearchList")

                if (airportSearchList.isNullOrEmpty()) {
                    return@mapLatest emptyList()
                }

                getAirportSearchModelList(airportSearchList)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    var arrivalAirportQuery: String by mutableStateOf("")
        private set
    var arrivalAirportStateFlow: StateFlow<List<AirportSearchModel>> =
        snapshotFlow { arrivalAirportQuery }
            .debounce(750)
            .distinctUntilChanged()
            .mapLatest { latestArrivalAirportInput ->
                if (!hasInternetConnection) {
                    Timber.e("Internet connection not available. Make sure that you are connected to the internet to proceed to the search")
                    return@mapLatest emptyList()
                }
                if (latestArrivalAirportInput.isEmpty()) {
                    Timber.e("Input is empty. Cannot execute the query.")
                    return@mapLatest emptyList()
                }
                if (isOptionSelectedByUser) {
                    Timber.w("No need to perform action. user selection")
                    isOptionSelectedByUser = false
                    return@mapLatest emptyList()
                }

                Timber.d("arrivalAirportStateFlow | mapLatest | it: $latestArrivalAirportInput")
                val airportSearchList = getAirportByOmniSearch(latestArrivalAirportInput)
                Timber.d("result: $airportSearchList")

                if (airportSearchList.isNullOrEmpty()) {
                    return@mapLatest emptyList()
                }

                getAirportSearchModelList(airportSearchList)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    var airportsNearBy: SnapshotStateList<AirportModel> = mutableStateListOf()
        private set

    var isAirportsNearByLoading: Boolean by mutableStateOf(false)
        private set

    fun updateFlightNumber(newFlightNumber: String) {
        this.flightNumber = newFlightNumber
    }

    fun updateDepartureAirportQuery(newAirportQuery: String) {
        Timber.d("updateDepartureAirportQuery() | departure query: $newAirportQuery")
        this.departureAirportQuery = newAirportQuery
    }

    fun updateArrivalAirportQuery(newAirportQuery: String) {
        Timber.d("updateArrivalAirportQuery() | arrival query: $newAirportQuery")
        this.arrivalAirportQuery = newAirportQuery
    }

    private fun updateAirportsNearBy(newAirportsNearBy: List<AirportModel>) {
        this.airportsNearBy.clear()
        this.airportsNearBy.addAll(newAirportsNearBy)
    }

    private fun updateIsAirportNearByLoading(isLoading: Boolean) {
        this.isAirportsNearByLoading = isLoading
    }


    /////////////////////////////////////
    // Coroutine
    /////////////////////////////////////
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            Timber.e("CoroutineExceptionHandler | Error caught with message: ${throwable.message}")

            if (isAirportsNearByLoading) {
                updateIsAirportNearByLoading(false)
            }
        }


    ///////////////////////////////
    //
    // CLASS METHODS
    //
    ///////////////////////////////
    fun initLocationManager(activity: Activity) {
        if (null == mLabLocationManager) {
            mLabLocationManager = runCatching {
                LabLocationManager.getInstance(activity = activity)
            }
                .onFailure {
                    it.printStackTrace()
                    Timber.e("initLocationManager() | onFailure | error caught with message: ${it.message} (class: ${it.javaClass.simpleName})")
                }
                .onSuccess {
                    Timber.d("initLocationManager() | onSuccess | is success: $it")
                }
                .getOrNull()
        }
    }

    @OptIn(ExperimentalKotoolsTypesApi::class)
    open fun onEvent(uiEvent: UiEvent, activity: Activity? = null) {
        if (!hasInternetConnection) {
            Timber.e("Internet connection not available. Make sure that you are connected to the internet to proceed to the search")
            return
        }

        when (uiEvent) {
            is UiEvent.OnUpdateDepartureQuery -> updateDepartureAirportQuery(uiEvent.departureAirportQuery)
            is UiEvent.OnUpdateArrivalQuery -> updateArrivalAirportQuery(uiEvent.arrivalAirportQuery)

            is UiEvent.OnSearchFlightByID -> searchFlightByFlightNumber(
                NotBlankString.create(uiEvent.id),
                uiEvent.context
            )

            /*is UiEvent.OnSearchFlightByRoute -> searchFlightByRoute(
                uiEvent.departureAirportIcaoCode,
                uiEvent.arrivalAirportIcaoCode
            )*/

            is UiEvent.OnFetchAirportNearBy -> {
                if (null == activity) {
                    Timber.e("onEvent() | Activity value is null")
                    return
                }
                searchAirportNearBy()
            }

            else -> Timber.e("onEvent() | else branch | uiEvent: $uiEvent")
        }
    }

    @OptIn(ExperimentalKotoolsTypesApi::class)
    fun searchFlightByFlightNumber(flightNumber: NotBlankString, context: Context) {
        Timber.d("searchFlightByFlightNumber()")

        if (flightNumber.toString().isEmpty()) {
            Timber.e("FLight number is null. Cannot perform REST call")
            return
        }

        val query = NotBlankString.create(if (BuildConfig.DEBUG) "AAL306" else flightNumber)

        viewModelScope.launch(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
            val flight : Flight = repository.searchFlightByID(query).flights[0]

            runCatching {
                (context as FlightMainActivity).launchFlightDetail(flight.toModel())
            }
                .onFailure {
                    it.printStackTrace()
                    Timber.e("searchFlightByFlightNumber() | onFailure | error caught with message: ${it.message} (class: ${it.javaClass.simpleName})")
                }
        }
    }

     fun searchFlightByRoute(
        departureAirportCode: NotBlankString,
        arrivalAirportCode: NotBlankString
    ) {
        Timber.d("searchFlightByRoute() | departureAirportCode: ${departureAirportCode.toString()}, arrivalAirportCode: ${arrivalAirportCode.toString()}")

        if (departureAirportCode.toString().isEmpty()) {
            Timber.e("Departure Airport query is null. Cannot perform REST call")
            return
        }
        if (arrivalAirportCode.toString().isEmpty()) {
            Timber.e("Arrival Airport query is null. Cannot perform REST call")
            return
        }

        viewModelScope.launch(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
            repository.searchFlightByRoute(departureAirportCode, arrivalAirportCode)
        }
    }

    private fun searchFlight(query: NotBlankString) {
        Timber.d("searchFlight()")
        viewModelScope.launch(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
            repository.searchFlight(query = query)
        }
    }

    private fun getAirportByOmniSearch(airportID: String): List<AirportSearch>? =
        runBlocking(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
            Timber.d("getAirportByOmniSearch() | airport ID: $airportID")
            repository.omniSearchAirport(airportID).airportsOmniData
        }


    fun getAirportById(airportID: String): AirportModel =
        runBlocking(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
            Timber.d("getAirportById() | airport ID: $airportID")
            repository.getAirportById(airportID).toModel()
        }


    private fun getAirportSearchModelList(airportSearchList: List<AirportSearch>): List<AirportSearchModel> =
        runCatching {
            buildSet {
                airportSearchList.forEach {
                    add(it.toModel())
                }
            }.toList()
        }
            .onFailure {
                it.printStackTrace()
                Timber.e("getAirportSearchModelList() | onFailure | error caught with message: ${it.message} (class: ${it.javaClass.simpleName})")
            }
            .getOrElse {
                emptyList()
            }


    @OptIn(ExperimentalKotoolsTypesApi::class)
    fun searchAirportNearBy() {
        Timber.d("getAirportNearBy()")

        updateIsAirportNearByLoading(true)

        val location: Location? =
            runCatching {
                mLabLocationManager?.getCurrentLocation() ?: run {
                    Timber.e("location manager object is null")
                    null
                }
            }
                .onFailure {
                    it.printStackTrace()
                    Timber.e("getAirportNearBy() | onFailure | error caught with message: ${it.message} (class: ${it.javaClass.simpleName})")
                }
                .onSuccess {
                    Timber.d("getAirportNearBy() | onSuccess | is success: $it")
                }
                .getOrNull()

        if (null == location) {
            Timber.d("getAirportNearBy() | location is null")
            updateIsAirportNearByLoading(false)
            return
        }


        val latitude = NotBlankString.create(location.latitude)
        val longitude = NotBlankString.create(location.longitude)


        runBlocking(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
            val response =
                repository.getAirportNearBy(latitude = latitude, longitude = longitude, radius = 50)
            if (response.airports.isEmpty()) {
                Timber.d("getAirportNearBy() | airports list is empty")
                return@runBlocking
            }

            updateAirportsNearBy(response.airports.map { it.toModel() })

            updateIsAirportNearByLoading(false)
        }
    }

}