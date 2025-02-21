package com.riders.thelab.core.data.local.model.compose.weather

import androidx.compose.runtime.Stable

@Stable
sealed class WeatherDataState {
    @Stable
    data class SuccessWeatherData(val isWeatherData: Boolean) : WeatherDataState()

    @Stable
    data class Error(val errorResponse: Throwable? = null) : WeatherDataState()

    @Stable
    data object Loading : WeatherDataState()

    @Stable
    data object None : WeatherDataState()
}