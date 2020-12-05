package com.riders.thelab.ui.youtubelike;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.riders.thelab.R;
import com.riders.thelab.data.local.model.Video;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressLint("NonConstantResourceId")
public class YoutubeLikeViewHolder extends RecyclerView.ViewHolder {

    // TAG
    private final Context context;

    // Views
    @BindView(R.id.card_view_item)
    public CardView itemCardView;

    @BindView(R.id.loader_item)
    ProgressBar itemLoader;
    @BindView(R.id.image_item)
    ImageView imageThumb;
    @BindView(R.id.name_item)
    TextView nameTextView;
    @BindView(R.id.description_item)
    TextView descriptionTextView;


    public YoutubeLikeViewHolder(Context context, View itemView) {
        super(itemView);

        this.context = context;
        ButterKnife.bind(this, itemView);
    }

    public ImageView getImageView() {
        return this.imageThumb;
    }

    public void setImage(String imageURL) {
        Glide.with(context)
                .load(imageURL)
                .into(imageThumb);
    }

    public void setName(String name) {
        nameTextView.setText(name);

    }

    public void setDescription(String description) {
        descriptionTextView.setText(description);
    }

    public void bind(Video itemYoutubeVideo) {

        if (itemLoader != null) {
            itemLoader.setVisibility(View.VISIBLE);
        }

        //Call Picasso to display the correct image in each row view item
        /*Picasso.with(context)
                .load(itemYoutubeVideo.getImageThumb())
                .into(imageThumb, new ImageLoadedCallback(itemLoader) {
                    @Override
                    public void onSuccess() {
                        if (itemLoader != null) {
                            itemLoader.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError() {

                        imageThumb.setImageResource(R.mipmap.ic_launcher);

                        Log.e(TAG, "bandeau pictures - OOOOOOOHHH CA VA PAAAAAS LAAAAA !!!");

                        if (itemLoader != null) {
                            itemLoader.setVisibility(View.GONE);
                        }
                    }
                });*/
    }

}
