package com.riders.thelab.core.data.local.model.app

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.RawValue

@Stable
@Immutable
data class LocalApp(
    val localId: Byte,
    val localTitle: String,
    val localDescription: String,
    var localDrawableIcon: @RawValue Drawable?,
    val localActivity: Class<out Activity?>?,
    val localDate: String,
) : App(
    appName = null,
    appVersion = null,
    appPackageName = null,
    id = localId,
    appTitle = localTitle,
    appDescription = localDescription,
    appDrawableIcon = localDrawableIcon,
    appActivity = localActivity,
    appDate = localDate
) {
    @IgnoredOnParcel
    var title: String? = null

    @IgnoredOnParcel
    var description: String? = null

    @IgnoredOnParcel
    var activity: Class<out Activity?>? = null

    @IgnoredOnParcel
    var icon: String? = null

    @IgnoredOnParcel
    var date: String? = null

    var bitmap: Bitmap? = null
}