package com.riders.thelab.ui.youtubelike

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.riders.thelab.R
import com.riders.thelab.data.local.model.Video

class YoutubeLikeListAdapter(
    private val context: Context,
    private val youtubeList: List<Video>,
    private val listener: YoutubeListClickListener
) : RecyclerView.Adapter<YoutubeLikeViewHolder>() {


    // Allows to remember the last item shown on screen
    private var lastPosition = -1

    override fun getItemCount(): Int {
        return youtubeList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YoutubeLikeViewHolder {
        return YoutubeLikeViewHolder(
            context,
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.row_youtube_like_item, parent, false)
        )
    }


    override fun onBindViewHolder(holder: YoutubeLikeViewHolder, position: Int) {
        val itemYoutubeVideo = youtubeList[position]

        holder.bind(itemYoutubeVideo)

        holder.viewBinding.cardViewItem.setOnClickListener { v ->
            listener.onYoutubeItemClicked(
                holder.getImageView(),
                holder.getNameTextView(),
                holder.getDescriptionView(),
                itemYoutubeVideo,
                holder.adapterPosition
            )
        }

        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            holder.viewBinding.cardViewItem.startAnimation(animation)
            lastPosition = position
        }
    }
}