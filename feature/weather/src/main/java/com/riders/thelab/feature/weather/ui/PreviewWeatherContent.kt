package com.riders.thelab.feature.weather.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.riders.thelab.core.data.local.model.compose.weather.WeatherDataState
import com.riders.thelab.core.data.local.model.compose.weather.WeatherUIState
import com.riders.thelab.core.data.local.model.weather.CityModel
import com.riders.thelab.core.ui.R
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.Lottie
import com.riders.thelab.core.ui.compose.component.toolbar.TheLabTopAppBar
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.utils.UIManager
import timber.log.Timber
import java.util.UUID


///////////////////////////////////////////////////
//
// COMPOSABLE
//
///////////////////////////////////////////////////
@DevicePreviews
@Composable
fun WeatherLoading(modifier: Modifier = Modifier) {
    TheLabTheme {
        Box(
            modifier = Modifier
                .size(72.dp)
                .then(modifier),
            contentAlignment = Alignment.Center
        ) {
            Lottie(
                modifier = Modifier.fillMaxSize(),
                url = "https://assets2.lottiefiles.com/packages/lf20_kk62um5v.json"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSuccess(
    weatherUiState: WeatherUIState,
    searchMenuExpanded: Boolean,
    searchCityQuery: String,
    suggestions: List<CityModel>,
    isWeatherMoreDataVisible: Boolean,
    uiEvent: (UiEvent) -> Unit
) {
    TheLabTheme {
        Column(modifier = Modifier.fillMaxSize()) {

            // Weather city search field
            WeatherCitySearchField(
                suggestions = suggestions,
                searchCityQuery = searchCityQuery,
                onSearchTextChange = { uiEvent.invoke(UiEvent.OnUpdateSearchCityQuery(it)) },
                searchMenuExpanded = searchMenuExpanded,
                onUpdateSearchMenuExpanded = {
                    uiEvent.invoke(UiEvent.OnUpdateSearchMenuExpanded(it))
                },
                onFetchWeatherRequest = { latitude, longitude ->
                    uiEvent.invoke(UiEvent.OnFetchWeatherForCity(latitude, longitude))
                },
                onDismissSearch = {}
            )

            // Weather city data to display
            WeatherMainCityContent(
                weatherUIState = weatherUiState,
                isWeatherMoreDataVisible = isWeatherMoreDataVisible,
                uiEvent = uiEvent
            )
        }
    }
}

@Composable
fun WeatherError(modifier: Modifier, onRetryButtonClicked: () -> Unit) {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Lottie(
                    modifier = Modifier.fillMaxSize(.5f),
                    rawResId = R.raw.error_rolling_dark_theme
                )

                Text("Error while getting weather for your location")

                Button(onClick = onRetryButtonClicked) {
                    Text(stringResource(id = R.string.action_retry))
                }
            }
        }
    }
}

@Composable
fun WeatherContent(
    weatherDataState: WeatherDataState,
    weatherUiState: WeatherUIState,
    iconState: Boolean,
    searchMenuExpanded: Boolean,
    searchCityQuery: String,
    suggestions: List<CityModel>,
    isWeatherMoreDataVisible: Boolean,
    uiEvent: (UiEvent) -> Unit
) {
    val context = LocalContext.current

    TheLabTheme {
        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = {
                TheLabTopAppBar(
                    title = stringResource(id = R.string.activity_title_weather),
                    iconState = iconState,
                    actionBlock = {
                        if (!iconState) {
                            Timber.e("Unable to perform action due to location feature unavailable")

                            UIManager.showToast(
                                context,
                                "Please make sure that the location setting is enabled"
                            )
                        } else {
                            uiEvent.invoke(UiEvent.OnMyLocationClicked)
                        }
                    }
                )
            }) { contentPadding ->
            AnimatedContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                targetState = weatherDataState
            ) { targetState: WeatherDataState ->
                when (targetState) {
                    is WeatherDataState.None,
                    is WeatherDataState.Loading -> {
                        // Loading State
                        WeatherLoading()
                    }

                    is WeatherDataState.Error -> {
                        // Error State
                        WeatherError(
                            modifier = Modifier.fillMaxSize(),
                            onRetryButtonClicked = { uiEvent.invoke(UiEvent.OnRetryRequest) }
                        )
                    }

                    is WeatherDataState.SuccessWeatherData -> {
                        // Success State
                        WeatherSuccess(
                            weatherUiState = weatherUiState,
                            searchMenuExpanded = searchMenuExpanded,
                            searchCityQuery = searchCityQuery,
                            suggestions = suggestions,
                            isWeatherMoreDataVisible = isWeatherMoreDataVisible,
                            uiEvent = uiEvent
                        )
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////////
//
// PREVIEWS
//
///////////////////////////////////////////////////
@DevicePreviews
@Composable
private fun PreviewWeatherError() {
    TheLabTheme {
        WeatherError(modifier = Modifier, onRetryButtonClicked = {})
    }
}

@DevicePreviews
@Composable
private fun PreviewWeatherContent(@PreviewParameter(PreviewProviderWeatherDataState::class) dataState: WeatherDataState) {
    val weatherUIState: WeatherUIState = PreviewProviderWeatherUIState().values.toList()[0]

    TheLabTheme {
        WeatherContent(
            weatherDataState = dataState,
            weatherUiState = weatherUIState,
            iconState = true,
            searchMenuExpanded = true,
            searchCityQuery = "Pa",
            suggestions = listOf(
                CityModel(
                    id = 1,
                    uuid = UUID.randomUUID().toString(),
                    name = "Johanesburg",
                    state = "",
                    country = "South Africa",
                    longitude = 48.3535,
                    latitude = 3.58978
                )
            ),
            isWeatherMoreDataVisible = true,
        ) {}
    }
}

@DevicePreviews
@Composable
private fun PreviewWeatherContentJohannesburg(@PreviewParameter(PreviewProviderWeatherDataState::class) dataState: WeatherDataState) {
    val weatherUIState: WeatherUIState = PreviewProviderWeatherUIState().values.toList()[0]

    TheLabTheme {
        WeatherContent(
            weatherDataState = dataState,
            weatherUiState = weatherUIState,
            iconState = true,
            searchMenuExpanded = true,
            searchCityQuery = "Johannesbu",
            suggestions = listOf(
                CityModel(
                    id = 1,
                    uuid = UUID.randomUUID().toString(),
                    name = "Johanesburg",
                    state = "",
                    country = "South Africa",
                    longitude = 48.3535,
                    latitude = 3.58978
                )
            ),
            isWeatherMoreDataVisible = true,
        ) {}
    }
}