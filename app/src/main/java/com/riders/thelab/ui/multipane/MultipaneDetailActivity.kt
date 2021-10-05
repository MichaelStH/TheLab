package com.riders.thelab.ui.multipane

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.riders.thelab.data.local.model.Movie
import com.riders.thelab.databinding.ActivityMultiPaneDetailBinding
import timber.log.Timber

class MultipaneDetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_MOVIE = "MOVIE"
    }

    private lateinit var viewBinding: ActivityMultiPaneDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMultiPaneDetailBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        getBundle()
    }

    private fun getBundle() {
        val bundle = intent.extras
        if (null == bundle) {
            Timber.e("Bundle is null exit activity.")
            finish()
        }

        bundle?.let {
            val movie: Movie? = it.getParcelable(EXTRA_MOVIE)
            if (movie != null) {
                supportActionBar?.title = movie.title
                setViews(movie)
            }
        }
    }

    private fun setViews(movie: Movie) {
        Glide.with(this)
            .load(movie.urlThumbnail)
            .into(viewBinding.multiPaneMovieItemImageDetail)
        viewBinding.multiPaneMovieItemTitleDetail.text = movie.title
        viewBinding.multiPaneMovieItemGenreDetail.text = movie.genre
        viewBinding.multiPaneMovieItemYearDetail.text = movie.year
    }
}