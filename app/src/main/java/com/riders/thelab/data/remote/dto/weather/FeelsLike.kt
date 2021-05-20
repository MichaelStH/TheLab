package com.riders.thelab.data.remote.dto.weather

import com.squareup.moshi.Json

data class FeelsLike(
    @Json(name = "day")
    private var day: Double = 0.0,
    @Json(name = "night")
    val night: Double = 0.0,
    @Json(name = "eve")
    val evening: Double = 0.0,
    @Json(name = "morn")
    val morning: Double = 0.0
)