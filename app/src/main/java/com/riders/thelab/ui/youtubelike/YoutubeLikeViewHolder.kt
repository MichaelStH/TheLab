package com.riders.thelab.ui.youtubelike

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.riders.thelab.data.local.model.Video
import com.riders.thelab.databinding.RowYoutubeLikeItemBinding
import timber.log.Timber

class YoutubeLikeViewHolder constructor(
    private val context: Context,
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    var viewBinding: RowYoutubeLikeItemBinding = RowYoutubeLikeItemBinding.bind(itemView)

    fun getImageView(): ShapeableImageView {
        return viewBinding.imageItem
    }

    fun getNameTextView(): MaterialTextView {
        return viewBinding.nameItem
    }

    fun getDescriptionView(): MaterialTextView {
        return viewBinding.descriptionItem
    }

    fun bind(itemYoutubeVideo: Video) {
        viewBinding.nameItem.text = itemYoutubeVideo.name
        viewBinding.descriptionItem.text = itemYoutubeVideo.description

        Glide.with(context)
            .load(itemYoutubeVideo.imageThumb)
            .addListener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.e(e)
                    viewBinding.loaderItem.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    viewBinding.loaderItem.visibility = View.GONE
                    return false
                }
            })
            .into(viewBinding.imageItem)

    }
}