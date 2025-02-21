package com.riders.thelab.feature.weather.ui

import android.location.Address
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.riders.thelab.core.common.utils.DateTimeUtils
import com.riders.thelab.core.data.local.model.compose.weather.WeatherUIState
import com.riders.thelab.core.data.local.model.weather.WeatherModel
import com.riders.thelab.core.ui.R
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.theme.Typography
import com.riders.thelab.core.ui.data.WindDirection
import com.riders.thelab.feature.weather.utils.WeatherUtils
import kotlin.math.roundToInt


///////////////////////////////////////////////////
//
// COMPOSABLE
//
///////////////////////////////////////////////////
@Composable
fun WeatherMoreData(weather: WeatherModel) {
    val gridState = rememberLazyGridState()

    val realFeels =
        "${weather.temperature?.realFeels?.roundToInt()} ${stringResource(R.string.degree_placeholder)}"
    val cloudiness = "${weather.clouds} ${stringResource(R.string.percent_placeholder)}"
    val humidity = "${weather.humidity} ${stringResource(R.string.percent_placeholder)}"
    val pressure = "${weather.pressure} ${stringResource(R.string.pressure_unit_placeholder)}"
    // Wind
    val wind = "${weather.windSpeed.toString()} ${stringResource(R.string.meter_unit_placeholder)}"

    val windDirection: WindDirection =
        WindDirection.getWindDirectionToTextualDescription(weather.windDegree)

    // Build chart with hourly weather data
    // buildChart(hourlyWeather)

    val sunrise: String = DateTimeUtils.formatMillisToTimeHoursMinutes(
        weather.timezone!!,
        weather.sunrise
    )

    TheLabTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Text(text = "Real Feels")
                Text(text = realFeels, fontWeight = FontWeight.ExtraBold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterVertically
                    )
                ) {
                    Image(
                        modifier = Modifier.size(36.dp),
                        painter = painterResource(id = R.drawable.ic_sunrise),
                        contentDescription = "sunrise icon",
                        colorFilter = ColorFilter.tint(
                            color = if (!isSystemInDarkTheme()) Color.Black else Color.White,
                            blendMode = BlendMode.SrcIn
                        )
                    )
                    Text(
                        text = sunrise
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterVertically
                    )
                ) {
                    Image(
                        modifier = Modifier.size(36.dp),
                        painter = painterResource(id = R.drawable.ic_sunset),
                        contentDescription = "sunset icon",
                        colorFilter = ColorFilter.tint(
                            color = if (!isSystemInDarkTheme()) Color.Black else Color.White,
                            blendMode = BlendMode.SrcIn
                        )
                    )

                    Text(
                        text = DateTimeUtils.formatMillisToTimeHoursMinutes(
                            weather.timezone!!,
                            weather.sunset
                        )
                    )
                }
            }

            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth(),
                state = gridState,
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cloud),
                            contentDescription = "cloud icon"
                        )
                        Text(text = "Cloudiness: $cloudiness")
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pressure),
                            contentDescription = "pressure icon"
                        )
                        Text(text = "Pressure: $pressure")
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_wind),
                            contentDescription = "wind icon"
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Wind: $wind")
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(id = windDirection.icon),
                                contentDescription = "wind direction icon"
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_humidity),
                            contentDescription = "humidity icon"
                        )
                        Text(text = "humidity: $humidity")
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherMainCityContent(
    weatherUIState: WeatherUIState,
    isWeatherMoreDataVisible: Boolean,
    uiEvent: (UiEvent) -> Unit
) {
    TheLabTheme {
        AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            targetState = weatherUIState
        ) { targetState: WeatherUIState ->
            when (targetState) {
                is WeatherUIState.None -> Box(modifier = Modifier)
                is WeatherUIState.Success -> {

                    val weather = targetState.weather
                    val address: Address? = targetState.weather.address
                    val cityName = targetState.weather.address?.locality
                    val country = address?.countryName

                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest
                            .Builder(LocalContext.current)
                            .data( weather.weatherIconUrl.toString())
                            .apply {
                                crossfade(true)
                                allowHardware(false)
                                //transformations(RoundedCornersTransformation(32.dp.value))
                            }
                            .build(),
                        placeholder = painterResource(R.drawable.logo_colors),
                    )

                    // Temperatures
                    val temperature =
                        "${weather.temperature?.temperature?.roundToInt()} ${
                            stringResource(R.string.degree_placeholder)
                        }"

                    // weather.hourlyWeather?.let { onGetMaxMinTemperature(it) }

                    TheLabTheme {
                        AnimatedVisibility(visible = true) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Weather icon
                                            Image(
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                painter = painter,
                                                contentDescription = "weather icon wth coil",
                                                contentScale = ContentScale.Fit,
                                            )

                                            // Colum with city name country and weather state
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(text = "$cityName, $country")
                                                Text(
                                                    text = weather.mainWeather.toString(),
                                                    style = Typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }

                                        // Temperature row container
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier) {
                                                // current temperature
                                                Text(
                                                    text = temperature,
                                                    style = Typography.titleLarge,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                // Min | Max Temperatures
                                                Row(
                                                    modifier = Modifier,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = weather.temperature?.max?.toInt().toString(),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "|"
                                                    )
                                                    Text(
                                                        text = weather.temperature?.min?.toInt().toString()
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    uiEvent.invoke(
                                                        UiEvent.OnUpdateMoreWeatherDataVisible(
                                                            !isWeatherMoreDataVisible
                                                        )
                                                    )
                                                }) {
                                                AnimatedContent(
                                                    targetState = isWeatherMoreDataVisible,
                                                    label = "weather_visibility_animation"
                                                ) { targetState ->
                                                    Row(
                                                        modifier = Modifier,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            16.dp,
                                                            Alignment.CenterHorizontally
                                                        ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(text = if (!targetState) "Show More" else "Close Panel")
                                                        Icon(
                                                            imageVector = if (!targetState) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                                                            contentDescription = "more icon"
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        AnimatedVisibility(visible = isWeatherMoreDataVisible) {
                                            WeatherMoreData(weather)
                                        }

                                        AnimatedVisibility(visible = !weather.dailyWeather.isNullOrEmpty()) {
                                            // Forecast
                                            WeatherDailyForecast(
                                                dailyWeatherList = weather.dailyWeather!!
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.weather_data_provided_by),
                                        fontSize = 12.sp
                                    )
                                    Image(
                                        modifier = Modifier.height(28.dp),
                                        painter = painterResource(id = R.drawable.openweathermap_logo_white),
                                        contentDescription = "open weather icon"
                                    )
                                }
                            }
                        }
                    }
                }

                is WeatherUIState.Error -> Box(modifier = Modifier)
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
fun PreviewWeatherMoreData(@PreviewParameter(PreviewProviderWeather::class) weather: WeatherModel) {
    TheLabTheme {
        WeatherMoreData(weather)
    }
}

@DevicePreviews
@Composable
private fun PreviewWeatherMainCityContent(
    @PreviewParameter(PreviewProviderWeatherUIState::class) weatherUiState: WeatherUIState
) {
    TheLabTheme {
        WeatherMainCityContent(
            weatherUIState = weatherUiState,
            isWeatherMoreDataVisible = false
        ) {}
    }
}