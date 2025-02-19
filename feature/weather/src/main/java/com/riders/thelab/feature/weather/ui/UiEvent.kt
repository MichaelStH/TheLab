package com.riders.thelab.feature.weather.ui

sealed interface UiEvent {
    data class OnUpdateLocationSearchQuery(val newQuery: String) : UiEvent
    data object onMyLocationClicked : UiEvent
}