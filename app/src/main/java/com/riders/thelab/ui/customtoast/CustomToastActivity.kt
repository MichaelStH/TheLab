package com.riders.thelab.ui.customtoast

import android.animation.Animator
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.riders.thelab.R
import com.riders.thelab.core.ui.compose.base.BaseAppCompatActivity
import com.riders.thelab.core.ui.views.toast.ToastTypeEnum
import com.riders.thelab.databinding.ActivityCustomToastBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Random

@AndroidEntryPoint
class CustomToastActivity : BaseAppCompatActivity() {

    private var _viewBinding: ActivityCustomToastBinding? = null
    private val binding: ActivityCustomToastBinding get() = _viewBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val w = window
        w.statusBarColor = Color.TRANSPARENT

        _viewBinding = ActivityCustomToastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCustom.setOnClickListener {
            binding.progressIndicator.animate()
                .setDuration(2000)
                .alpha(1.0f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        binding.progressIndicator.visibility = View.VISIBLE
                        binding.progressIndicator.alpha = 0f
                        binding.progressIndicator.setIndicatorColor(
                            ContextCompat.getColor(this@CustomToastActivity, R.color.success),
                            ContextCompat.getColor(this@CustomToastActivity, R.color.warning),
                            ContextCompat.getColor(this@CustomToastActivity, R.color.error)
                        )
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(2000)
                            binding.progressIndicator.animate()
                                .setDuration(1500)
                                .alpha(0.0f)
                                .setListener(object : Animator.AnimatorListener {
                                    override fun onAnimationStart(animation: Animator) {}
                                    override fun onAnimationEnd(animation: Animator) {
                                        binding.progressIndicator.visibility = View.GONE
                                    }

                                    override fun onAnimationCancel(animation: Animator) {}
                                    override fun onAnimationRepeat(animation: Animator) {}
                                })
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
                .start()
            displayCustomToastUsingClass()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                backPressed()
            }
        }
        return true
    }

    override fun backPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(false)
        _viewBinding = null
    }

    private fun displayCustomToastUsingClass() {
        val random = Random().nextInt(ToastTypeEnum.entries.size)

        Timber.d("random : %s", random)

        /*TheLabToast.Builder(this)
            .setText("Testing a new text")
            .setType(ToastTypeEnum.values()[random])
            .show()*/
    }
}