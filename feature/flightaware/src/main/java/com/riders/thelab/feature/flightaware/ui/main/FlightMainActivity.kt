package com.riders.thelab.feature.flightaware.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.riders.thelab.core.common.network.LabNetworkManager
import com.riders.thelab.core.data.local.model.Permission
import com.riders.thelab.core.data.local.model.flight.SearchFlightModel
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.feature.flightaware.ui.airport.AirportSearchActivity
import com.riders.thelab.feature.flightaware.ui.airport.AirportSearchDetailActivity
import com.riders.thelab.feature.flightaware.ui.search.SearchFlightActivity
import com.riders.thelab.feature.flightaware.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

// TODO : Package organization
// TODO : flight (search id and route ; list and details)
// TODO : airports (search and details)

@AndroidEntryPoint
class FlightMainActivity : BaseComponentActivity() {

    private val mViewModel: FlightViewModel by viewModels<FlightViewModel>()

    private var mLabNetworkManager: LabNetworkManager? = null

    private var continueWithBlock: Pair<Boolean, () -> Unit> = false to {}

    override var permissionLauncher: ActivityResultLauncher<Array<String>>? =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { areGranted: Map<String, Boolean> ->
            if (!areGranted.values.all { it }) {
                Timber.e("$areGranted permissions is NOT granted")
            } else {
                Timber.d("$areGranted permissions is granted")

                if (continueWithBlock.first) {
                    continueWithBlock.second()

                    continueWithBlock = false to {}
                }
            }
        }


    ///////////////////////////////
    //
    // OVERRIDE
    //
    ///////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mLabNetworkManager = LabNetworkManager
            .getInstance(this, lifecycle)
            .also {
                mViewModel.observeNetworkState(it)
                // mFlightSearchViewModel.observeNetworkState(it)
            }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                setContent {
                    val departureAirportsFlow by mViewModel.departureAirportStateFlow.collectAsStateWithLifecycle()
                    val arrivalAirportsFlow by mViewModel.arrivalAirportStateFlow.collectAsStateWithLifecycle()

                    TheLabTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            FlightMainContent(
                                hasConnection = mViewModel.hasInternetConnection,
                                searchPageIndex = mViewModel.searchPageIndex,
                                airportsNearBy = mViewModel.airportsNearBy,
                                isLoading = mViewModel.isAirportsNearByLoading,
                                departureExpanded = mViewModel.departureDropdownExpanded,
                                departureSuggestions = departureAirportsFlow,
                                arrivalExpanded = mViewModel.arrivalDropdownExpanded,
                                arrivalSuggestions = arrivalAirportsFlow,
                                uiEvent = { event ->
                                    Timber.i("uiEvent | ${event.javaClass.name}")

                                    // Call onEvent for FlightSearchViewModel as well
                                    when (event) {
                                        is UiEvent.OnFetchAirportNearBy -> {
                                            if (!hasLocationPermission()) {
                                                permissionLauncher?.launch(
                                                    Permission.Location
                                                        .permissions
                                                        .toList()
                                                        .toTypedArray()
                                                )

                                                mViewModel.initLocationManager(this@FlightMainActivity)

                                                continueWithBlock = true to {
                                                    // Call onEvent for ViewModel
                                                    mViewModel.onEvent(
                                                        uiEvent = event,
                                                        activity = this@FlightMainActivity
                                                    )
                                                }
                                            } else {
                                                mViewModel.initLocationManager(this@FlightMainActivity)
                                                // Call onEvent for ViewModel
                                                mViewModel.onEvent(
                                                    uiEvent = event,
                                                    activity = this@FlightMainActivity
                                                )
                                            }
                                        }

                                        else -> {
                                            // Call onEvent for ViewModel
                                            mViewModel.onEvent(uiEvent = event)
                                        }
                                    }
                                },
                            )
                        }
                    }

                    LaunchedEffect(departureAirportsFlow) {
                        Timber.d("LaunchedEffect | departure Airports Flow value: $departureAirportsFlow | coroutineContext: ${this.coroutineContext}")
                        mViewModel.onEvent(UiEvent.OnDepartureExpanded(departureAirportsFlow.isNotEmpty()))
                    }
                    LaunchedEffect(arrivalAirportsFlow) {
                        Timber.d("LaunchedEffect | arrival Airports Flow value: $arrivalAirportsFlow | coroutineContext: ${this.coroutineContext}")
                        mViewModel.onEvent(UiEvent.OnArrivalExpanded(arrivalAirportsFlow.isNotEmpty()))
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.e("onPause()")
    }

    override fun onResume() {
        Timber.d("onResume()")
        mViewModel.isResumed = true
        super.onResume()
    }

    override fun backPressed() {
        Timber.e("backPressed()")

        if (mViewModel.departureDropdownExpanded) {
            mViewModel.updateDepartureExpanded(false)
            return
        }

        finish()
    }


    ///////////////////////////////
    //
    // CLASS METHODS
    //
    ///////////////////////////////
    private fun hasLocationPermission(): Boolean = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    fun launchAirportSearchActivity() = Intent(this, AirportSearchActivity::class.java)
        .run { startActivity(this) }

    fun launchAirportDetail(airportID: String) =
        Intent(this, AirportSearchDetailActivity::class.java)
            .apply { this.putExtra(AirportSearchDetailActivity.EXTRA_AIRPORT_ID, airportID) }
            .run { startActivity(this) }

    fun launchSearchFlights(flights: List<SearchFlightModel>, searchType: String) =
        Intent(this, SearchFlightActivity::class.java)
            .apply {
                Timber.d("launchSearchFlights() | with flights length: ${flights.size}")

                this.putExtra(
                    Constants.EXTRA_SEARCH_TYPE,
                    when (searchType) {
                        SEARCH_TYPE_FLIGHT_NUMBER -> Constants.EXTRA_SEARCH_TYPE_FLIGHT_NUMBER
                        SEARCH_TYPE_FLIGHT_ROUTE -> Constants.EXTRA_SEARCH_TYPE_FLIGHT_ROUTE
                        else -> "UNKNOWN"
                    }
                )
                this.putExtra(Constants.EXTRA_FLIGHT, Json.encodeToString(flights))
            }
            .run { startActivity(this) }


    companion object {
        const val SEARCH_TYPE_FLIGHT_NUMBER: String = "SEARCH_TYPE_FLIGHT_NUMBER"
        const val SEARCH_TYPE_FLIGHT_ROUTE: String = "SEARCH_TYPE_FLIGHT_ROUTE"

    }
}