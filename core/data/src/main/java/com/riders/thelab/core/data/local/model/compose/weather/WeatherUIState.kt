package com.riders.thelab.core.data.local.model.compose.weather

import androidx.compose.runtime.Stable
import com.riders.thelab.core.data.local.model.weather.WeatherModel

@Stable
sealed class WeatherUIState {
    @Stable
    data class Success(val weather: WeatherModel) : WeatherUIState()

    @Stable
    data class Error(val errorResponse: Throwable? = null) : WeatherUIState()

    @Stable
    data object None : WeatherUIState()
}