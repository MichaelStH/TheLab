package com.riders.thelab.ui.locationonmaps

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable

@HiltViewModel
class LocationOnMapsViewModel : ViewModel() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
}