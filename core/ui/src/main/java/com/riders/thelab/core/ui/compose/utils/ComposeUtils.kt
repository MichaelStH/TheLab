package com.riders.thelab.core.ui.compose.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.riders.thelab.core.ui.compose.base.BaseAppCompatActivity
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

fun Context.findActivity(): Activity? = when (this) {
    is BaseAppCompatActivity -> this
    is BaseComponentActivity -> this
    is AppCompatActivity -> this
    is FragmentActivity -> this
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun executeOnBackPressed(context: Context) {
    Timber.d("executeOnBackPressed()")

    runCatching {
        Timber.d("runCatching | Attempt to execute backPressed on ComponentActivity()")
        (context.findActivity() as BaseComponentActivity).backPressed()
    }
        .onFailure { baseComponentException ->
            baseComponentException.printStackTrace()
            Timber.e("runCatching | onFailure | error caught with message: ${baseComponentException.message} (class: ${baseComponentException.javaClass.canonicalName})")

            runCatching {
                Timber.d("runCatching | Attempt to execute fallback backPressed on AppCompatActivity()")
                (context.findActivity() as BaseAppCompatActivity).backPressed()
            }
                .onFailure { baseAppCompatException ->
                    baseAppCompatException.printStackTrace()
                    Timber.e("runCatching | onFailure | error caught with message: ${baseAppCompatException.message} (class: ${baseAppCompatException.javaClass.canonicalName})")

                    runCatching {
                        Timber.d("runCatching | Attempt to execute fallback backPressed on FragmentActivity()")
                        (context.findActivity() as FragmentActivity).onBackPressed()
                    }
                        .onFailure { fragmentActivityException ->
                            fragmentActivityException.printStackTrace()
                            Timber.e("runCatching | onFailure | error caught with message: ${fragmentActivityException.message} (class: ${fragmentActivityException.javaClass.canonicalName})")
                        }
                }
        }
}

@Composable
@ReadOnlyComposable
private fun resourcesAsComposable(): Resources = LocalContext.current.resources

@Composable
fun isKeyboardVisible(): Boolean = WindowInsets.ime.getBottom(LocalDensity.current) > 0


/*
use example :
val isKeyboardOpen by keyboardAsState() // true or false
 */
@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}

@Composable
fun keyboardAsStateView(): State<Boolean> {
    val keyboardState = remember { mutableStateOf(false) }
    val view = LocalView.current
    LaunchedEffect(view) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            keyboardState.value = insets.isVisible(WindowInsetsCompat.Type.ime())
            insets
        }
    }
    return keyboardState
}

@OptIn(ExperimentalMaterial3Api::class)
fun showTooltip(scope: CoroutineScope, tooltipState: TooltipState) = scope.launch {
    if (!tooltipState.isVisible) {
        tooltipState.show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun hideTooltip(scope: CoroutineScope, tooltipState: TooltipState) = scope.launch {
    if (tooltipState.isVisible) {
        tooltipState.dismiss()
    }
}