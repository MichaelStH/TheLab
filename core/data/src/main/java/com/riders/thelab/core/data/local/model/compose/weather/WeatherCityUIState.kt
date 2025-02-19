package com.riders.thelab.core.data.local.model.compose.weather

import androidx.compose.runtime.Stable
import com.riders.thelab.core.data.local.model.weather.WeatherModel

@Stable
sealed class WeatherCityUIState {
    @Stable
    data class Success(val weather: WeatherModel) : WeatherCityUIState()

    @Stable
    data class Error(val errorResponse: Throwable? = null) : WeatherCityUIState()

    @Stable
    data object None : WeatherCityUIState()
}