package com.riders.thelab.ui.mainactivity

import androidx.compose.runtime.Stable
import com.riders.thelab.core.data.local.model.app.App
import com.riders.thelab.core.data.local.model.compose.IslandState

@Stable
sealed interface UiEvent {
    data object OnMicrophoneClicked : UiEvent
    data class OnUpdateMicrophoneEnabled(val enabled: Boolean) : UiEvent
    data object OnSearchClicked : UiEvent
    data class OnUpdateSearchQuery(val newQuery: String) : UiEvent
    data object OnSettingsClicked : UiEvent
    data class OnUpdateDynamicIslandState(val newState: IslandState) : UiEvent
    data class OnUpdateDynamicIslandVisible(val isVisible: Boolean) : UiEvent
    data class OnAppItemClicked(val app: App) : UiEvent
}