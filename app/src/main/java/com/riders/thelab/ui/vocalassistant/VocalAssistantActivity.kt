package com.riders.thelab.ui.vocalassistant

import android.os.Bundle
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity

class VocalAssistantActivity: BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun backPressed() {
        finish()
    }
}