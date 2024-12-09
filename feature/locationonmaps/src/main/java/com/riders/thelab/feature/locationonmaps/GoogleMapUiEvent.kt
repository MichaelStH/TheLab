package com.riders.thelab.feature.locationonmaps

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest

sealed interface GoogleMapUiEvent {
    data object OnMapLoaded : GoogleMapUiEvent
    data object OnMapTouched : GoogleMapUiEvent
    data class OnMapClick(val latLng: LatLng) : GoogleMapUiEvent
    data class OnMapLongClick(val latLng: LatLng) : GoogleMapUiEvent
    data class OnMyLocationClick(val location: Location) : GoogleMapUiEvent
    data class OnPOIClick(val poi: PointOfInterest) : GoogleMapUiEvent
    data object OnMyLocationButtonClick : GoogleMapUiEvent
    data object OnMarkerClicked : GoogleMapUiEvent
}