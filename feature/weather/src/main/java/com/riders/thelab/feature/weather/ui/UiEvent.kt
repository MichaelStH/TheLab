package com.riders.thelab.feature.weather.ui

sealed interface UiEvent {
    data object OnMyLocationClicked : UiEvent
    data object OnRetryRequest : UiEvent
    data class OnUpdateSearchMenuExpanded(val expanded: Boolean) : UiEvent
    data class OnUpdateSearchCityQuery(val newQuery: String) : UiEvent
    data class OnUpdateMoreWeatherDataVisible(val isVisible: Boolean) : UiEvent
    data class OnFetchWeatherForCity(val latitude: Double, val longitude: Double) : UiEvent
}