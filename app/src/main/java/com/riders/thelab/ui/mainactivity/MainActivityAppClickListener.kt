package com.riders.thelab.ui.mainactivity

import android.view.View
import com.riders.thelab.data.local.model.App

interface MainActivityAppClickListener {
    fun onAppItemCLickListener(view: View, item: App, position: Int)
}