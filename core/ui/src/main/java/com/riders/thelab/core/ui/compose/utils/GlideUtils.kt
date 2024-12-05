package com.riders.thelab.core.ui.compose.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.glide.GlideRequestType

@Composable
fun getGlideImage(
    modifier: Modifier = Modifier,
    url: String,
    description: String? = null,
    glideRequestType: GlideRequestType = GlideRequestType.BITMAP
) = GlideImage(
    modifier = modifier,
    imageModel = { url },
    glideRequestType = glideRequestType,
    loading = {

    },
    failure = {

    },
    success = { _, painter ->
        return@GlideImage Box(modifier) {
            Image(
                painter = painter,
                contentDescription = description
            )
        }

        /*it
            .thumbnail(
                requestManager
                    .asDrawable()
                    .load(item.uri)
                    .signature(signature)
                    .override(THUMBNAIL_DIMENSION)
            )
            .signature(signature)*/
    }
)