package com.riders.thelab.feature.locationonmaps

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.libraries.places.api.Places
import com.riders.thelab.core.ui.compose.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LocationOnMapsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : BaseViewModel() {
    // Define a Place ID.
    private var placeId by mutableStateOf("")

    var isSearchPlaceVisible: Boolean by mutableStateOf(false)

    private fun updateIsSearchPlaceVisible(isVisible: Boolean) {
        this.isSearchPlaceVisible = isVisible
    }


    fun onEvent(event: UiEvent) {
        Timber.d("onEvent() | $event")

        when (event) {
            is UiEvent.OnSearchPlaceVisible -> updateIsSearchPlaceVisible(event.isVisible)
            is UiEvent.OnPlaceSelected -> {
                updateIsSearchPlaceVisible(true)
            }

            is UiEvent.OnVocalSearch -> {}
        }
    }

    fun onGoogleMapEvent(event: GoogleMapUiEvent) {
        Timber.d("onGoogleMapEvent() | $event")

        when (event) {
            is GoogleMapUiEvent.OnMapLoaded -> {}
            is GoogleMapUiEvent.OnMapTouched -> {}
            is GoogleMapUiEvent.OnMyLocationButtonClick -> {}
            is GoogleMapUiEvent.OnMarkerClicked -> {}
            is GoogleMapUiEvent.OnMapClick -> updateIsSearchPlaceVisible(true)
            is GoogleMapUiEvent.OnMapLongClick -> {}
            is GoogleMapUiEvent.OnMyLocationClick -> {}
            is GoogleMapUiEvent.OnPOIClick -> {}
        }
    }

    /*private fun fetchPlace() {
        Timber.d("fetchPlace() | placeId: $placeId")

        val placesClient = Places.createClient(context)
        // Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.newInstance(placeId, Constants.CURRENT_PLACE_FIELDS)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Timber.d("fetchPlace() | Place found: ${place.name}")
            }
            .addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Timber.e("fetchPlace() | Error caught, Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                    TODO("Handle error with given status code")
                }
            }
    }*/

    fun initPlaces() {
        if (!Places.isInitialized()) {
            Timber.d("initPlaces()")
            // Initialize the Places SDK.
            Places.initialize(
                context,
                "AIzaSyDuywb98tO1xXHb9lflC2iTLs_67jgihgQ",
                Locale.FRANCE
            )
        }
    }
}