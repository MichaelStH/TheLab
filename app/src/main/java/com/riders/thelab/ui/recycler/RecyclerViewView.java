package com.riders.thelab.ui.recycler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ProgressBar;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.riders.thelab.R;
import com.riders.thelab.core.utils.LabCompatibilityManager;
import com.riders.thelab.data.remote.dto.Artist;
import com.riders.thelab.ui.base.BaseViewImpl;

import org.parceler.Parcels;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class RecyclerViewView extends BaseViewImpl<RecyclerViewPresenter>
        implements RecyclerViewContract.View, RecyclerClickListener {

    private RecyclerViewActivity context;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private RecyclerViewAdapter adapter;

    @Inject
    RecyclerViewView(RecyclerViewActivity context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        getPresenter().attachView(this);

        ButterKnife.bind(this, context.findViewById(android.R.id.content));

        context.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context.getSupportActionBar().setTitle(context.getString(R.string.activity_title_recycler_view));

        getPresenter().getFirebaseJSONURL();
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        getPresenter().detachView();
    }

    @Override
    public void showLoader() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader() {
        progressBar.setVisibility(View.GONE);
    }

    private String bucketUrl;

    @Override
    public void onJSONURLFetched(String url) {
        bucketUrl = url;
        getPresenter().getFirebaseFiles();
    }

    @Override
    public void onJSONURLError() {

    }

    private List<String> artistThumbnails;

    @Override
    public void onArtistsThumbnailsSuccessful(List<String> artistThumbnails) {
        this.artistThumbnails = artistThumbnails;
        getPresenter().fetchArtists(bucketUrl);
    }

    @Override
    public void onArtistsThumbnailsError() {

    }

    @Override
    public void onFetchArtistsSuccessful(List<Artist> listOfArtists) {

        Completable.complete()
                .delay(3, TimeUnit.SECONDS)
                .doOnComplete(() -> {
                    context.runOnUiThread(() -> {
                        hideLoader();
                        adapter =
                                new RecyclerViewAdapter(
                                        context,
                                        listOfArtists,
                                        artistThumbnails,
                                        this);

                        LinearLayoutManager layoutManager;
                        if (!LabCompatibilityManager.isTablet(context)) {
                            layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

                        } else {
                            layoutManager = new GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false);
                        }

                        mRecyclerView.setLayoutManager(layoutManager);

                        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                        mRecyclerView.setAdapter(adapter);
                    });
                })
                .doOnError(Timber::e)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }

    @Override
    public void onFetchArtistsError() {

    }

    @Override
    public void onRecyclerClick(Artist item) {
        Timber.d("onRecyclerClick()");
        Timber.d(item.toString());

    }

    @Override
    public void onDetailClick(Artist item, ShapeableImageView sharedImageView, int position) {
        Timber.d("onDetailClick(item, sharedImageView, position)");

        Intent intent = new Intent(context, RecyclerViewDetailActivity.class);

        intent.putExtra(RecyclerViewDetailActivity.EXTRA_RECYCLER_ITEM, Parcels.wrap(item));

        // Check if we're running on Android 5.0 or higher
        if (LabCompatibilityManager.isLollipop()) {
            Timber.d("Apply activity transition");

            intent.putExtra(
                    RecyclerViewDetailActivity.EXTRA_TRANSITION_ICON_NAME,
                    ViewCompat.getTransitionName(sharedImageView));

            // Apply activity transition
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            context,
                            sharedImageView,
                            "icon");
            context.startActivity(intent, options.toBundle());
        } else {
            Timber.d("Swap without transition");
            // Swap without transition
            context.startActivity(intent);
        }
    }

    @Override
    public void onDeleteClick(Artist item, int position) {
        Timber.d("onDeleteClick() item %s at position : %s", item.getArtistName(), position);

        // get the removed item name to display it in snack bar
        String name = item.getArtistName();

        // backup of removed item for undo purpose
        final Artist deletedItem = item;
        final int deletedIndex = position;

        // remove the item from recycler view
        adapter.removeItem(position);

        // showing snack bar with Undo option
        Snackbar snackbar =
                Snackbar.make(
                        coordinatorLayout,
                        name + " removed from cart!",
                        BaseTransientBottomBar.LENGTH_LONG);
        snackbar.setAction(
                "UNDO",
                view -> {
                    // undo is selected, restore the deleted item
                    adapter.restoreItem(deletedItem, deletedIndex);
                });
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }
}
