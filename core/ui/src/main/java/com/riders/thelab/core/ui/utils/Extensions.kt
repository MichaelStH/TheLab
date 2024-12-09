package com.riders.thelab.core.ui.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import coil.compose.AsyncImagePainter

val Float.toPx get() = this * Resources.getSystem().displayMetrics.density


fun Context.getDrawableByName(imageResName: String): Drawable? = ResourcesCompat.getDrawable(
    this.resources,
    this.resources.getIdentifier(imageResName, "drawable", this.packageName),
    this.theme
)


/////////////////////////////////////////////////////
// Glide Image Loader
/////////////////////////////////////////////////////

// painter.loadImage() -> Drawable
suspend fun AsyncImagePainter.loadImage(): Drawable =
    imageLoader
        .execute(request)
        .drawable!!