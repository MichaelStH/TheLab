package com.riders.thelab.feature.flightaware.ui.search

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.riders.thelab.core.data.local.model.compose.SearchFlightsUiState
import com.riders.thelab.core.data.local.model.flight.SearchFlightModel
import com.riders.thelab.feature.flightaware.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotools.types.experimental.ExperimentalKotoolsTypesApi
import kotools.types.text.NotBlankString
import timber.log.Timber

class SearchFlightViewModel : ViewModel() {

    //////////////////////////////////////////
    // Compose states
    //////////////////////////////////////////
    private var _searchFlightUiState: MutableStateFlow<SearchFlightsUiState> =
        MutableStateFlow(SearchFlightsUiState.Loading)
    var searchFlightUiState: StateFlow<SearchFlightsUiState> = _searchFlightUiState

    private fun updateUiState(newState: SearchFlightsUiState) {
        this._searchFlightUiState.value = newState
    }


    /////////////////////////////////////
    //
    // CLASS METHODS
    //
    /////////////////////////////////////
    @OptIn(ExperimentalKotoolsTypesApi::class)
    fun getBundle(intent: Intent) {
        Timber.d("getBundle()")

        runCatching {
            // Try to get bundle values
            intent.extras?.let { bundle ->

                val searchType = bundle.getString(Constants.EXTRA_SEARCH_TYPE).toString()
                val flightString: String? = bundle.getString(Constants.EXTRA_FLIGHT)

                flightString?.let {
                    val extraItem: List<SearchFlightModel>? =
                        Json.decodeFromString(flightString) as List<SearchFlightModel>?

                    when (searchType) {
                        Constants.EXTRA_SEARCH_TYPE_FLIGHT_NUMBER -> {
                            extraItem?.let { flights: List<SearchFlightModel> ->
                                // Log
                                Timber.d("SearchFlightActivity.EXTRA_SEARCH_TYPE_FLIGHT_NUMBER | item length: ${flights.size}")
                                updateUiState(SearchFlightsUiState.Success(flights))
                            }
                                ?: run { Timber.e("SearchFlightActivity.EXTRA_SEARCH_TYPE_FLIGHT_NUMBER | Extra item object is null") }
                        }

                        Constants.EXTRA_SEARCH_TYPE_FLIGHT_ROUTE -> {
                            extraItem?.let { flights: List<SearchFlightModel> ->
                                // Log
                                Timber.d("SearchFlightActivity.EXTRA_SEARCH_TYPE_FLIGHT_ROUTE | item length: ${flights.size}")
                                updateUiState(SearchFlightsUiState.Success(flights))
                            }
                                ?: run {
                                    Timber.e("SearchFlightActivity.EXTRA_SEARCH_TYPE_FLIGHT_ROUTE | Extra item object is null")
                                    updateUiState(SearchFlightsUiState.Error(NotBlankString.create("Error occurred while getting value")))
                                }
                        }

                        else -> {
                            Timber.e("Unknown search type: $searchType")
                            updateUiState(SearchFlightsUiState.Error(NotBlankString.create("Error occurred while getting value")))
                        }
                    }
                } ?: run {
                    Timber.e("getBundle() | flightString is null")
                    updateUiState(SearchFlightsUiState.Error(NotBlankString.create("Error occurred while getting value")))
                }

            } ?: run {
                Timber.e("Intent extras are null")
                updateUiState(SearchFlightsUiState.Error(NotBlankString.create("Error occurred while getting value")))
            }
        }
            .onFailure {
                it.printStackTrace()
                Timber.e("getBundle() | onFailure | error caught with message: ${it.message} (class: ${it.javaClass.canonicalName})")
                updateUiState(
                    SearchFlightsUiState.Error(
                        message = NotBlankString.create("Error occurred while getting value"),
                        throwable = it
                    )
                )
            }
            .onSuccess {
                Timber.d("getBundle() | onSuccess")
            }
    }
}