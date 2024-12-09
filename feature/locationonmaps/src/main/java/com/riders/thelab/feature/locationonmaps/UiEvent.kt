package com.riders.thelab.feature.locationonmaps

import com.google.android.libraries.places.api.model.Place

sealed interface UiEvent {
    data class OnSearchPlaceVisible(val isVisible: Boolean) : UiEvent
    data class OnPlaceSelected(val place: Place) : UiEvent
    data object OnVocalSearch : UiEvent
}