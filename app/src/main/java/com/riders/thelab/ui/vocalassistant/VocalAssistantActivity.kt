package com.riders.thelab.ui.vocalassistant

import android.os.Bundle
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import com.riders.thelab.core.ui.utils.UIManager

class VocalAssistantActivity : BaseComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UIManager.showToast(this, "Trigger phrase detected!")
    }

    override fun backPressed() {
        finish()
    }
}