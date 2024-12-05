package com.riders.thelab.core.data.local.model.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.core.content.res.ResourcesCompat
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.RawValue
import kotlin.reflect.KClass

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

    var bitmap: Bitmap? = null

    companion object {
        @SuppressLint("DiscouragedApi")
        @JvmStatic
        fun getDrawableByName(context: Context, imageResName: String): Drawable {
            val mResources = context.resources

            return ResourcesCompat.getDrawable(
                mResources,
                mResources.getIdentifier(imageResName, "drawable", context.packageName),
                context.theme
            )!!
        }
    }

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
}