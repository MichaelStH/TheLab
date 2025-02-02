package com.riders.thelab.ui.spring

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.BounceInterpolator
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.DynamicAnimation.ViewProperty
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.riders.thelab.R
import com.riders.thelab.core.ui.compose.base.BaseAppCompatActivity
import com.riders.thelab.databinding.ActivitySpringBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SpringActivity : BaseAppCompatActivity(), View.OnClickListener {

    private var _viewBinding: ActivitySpringBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _viewBinding!!

    private var isTopAnimationToggle: Boolean = false
    private var isBottomAnimationToggle: Boolean = false

    private var xAnimation: SpringAnimation? = null
    private var yAnimation: SpringAnimation? = null

    private var dX = 0f
    private var dY = 0f

    private val globalLayoutListener = OnGlobalLayoutListener {
        xAnimation =
            createSpringAnimation(binding.imageView, DynamicAnimation.X, binding.imageView.x)
        yAnimation =
            createSpringAnimation(binding.imageView, DynamicAnimation.Y, binding.imageView.y)
    }


    @SuppressLint("ClickableViewAccessibility")
    private val touchListener = OnTouchListener { v: View, event: MotionEvent ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // capture the difference between view's top left corner and touch point
                dX = v.x - event.rawX
                dY = v.y - event.rawY
                // cancel animations
                xAnimation!!.cancel()
                yAnimation!!.cancel()
            }

            MotionEvent.ACTION_MOVE -> {
                //  a different approach would be to change the view's LayoutParams.
                binding.imageView.animate()
                    .x(event.rawX + dX)
                    .y(event.rawY + dY)
                    .setDuration(0)
                    .start()
                val newX = event.rawX + dX
                val newY = event.rawY + dY
                v
                    .animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start()
            }

            MotionEvent.ACTION_UP -> {
                xAnimation!!.start()
                yAnimation!!.start()
            }
        }
        true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate()")
        _viewBinding = ActivitySpringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()

        binding.imageView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        chainedSpringAnimation()
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
        Timber.d("backPressed()")

        if (isTopAnimationToggle) {
            revertTopAnimation()
            setListeners()
            return
        }

        if (isBottomAnimationToggle) {
            revertBottomAnimation()
            setListeners()
            return
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.e("onDestroy()")
        _viewBinding = null
    }

    private fun setListeners() {
        Timber.d("setListeners()")
        binding.clTop.setOnClickListener(this)
        binding.clBottom.setOnClickListener(this)
    }

    private fun createSpringAnimation(
        view: View?,
        property: ViewProperty?,
        finalPosition: Float,
        stiffness: Float = SpringForce.STIFFNESS_MEDIUM,
        dampingRatio: Float = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
    ): SpringAnimation {
        val animation = SpringAnimation(view, property)
        val springForce = SpringForce(finalPosition)
        springForce.stiffness = stiffness
        springForce.dampingRatio = dampingRatio
        animation.spring = springForce
        return animation
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun chainedSpringAnimation() {
        Timber.d("chainedSpringAnimation()")
        val xAnimation2 =
            createSpringAnimation(binding.imageView2, DynamicAnimation.X, binding.imageView2.x)
        val yAnimation2 =
            createSpringAnimation(binding.imageView2, DynamicAnimation.Y, binding.imageView2.y)
        val xAnimation3 =
            createSpringAnimation(binding.imageView3, DynamicAnimation.X, binding.imageView3.x)
        val yAnimation3 =
            createSpringAnimation(binding.imageView3, DynamicAnimation.Y, binding.imageView3.y)
        val xAnimation4 =
            createSpringAnimation(binding.imageView4, DynamicAnimation.X, binding.imageView4.x)
        val yAnimation4 =
            createSpringAnimation(binding.imageView4, DynamicAnimation.Y, binding.imageView4.y)
        val xAnimation5 =
            createSpringAnimation(binding.imageView5, DynamicAnimation.X, binding.imageView5.x)
        val yAnimation5 =
            createSpringAnimation(binding.imageView5, DynamicAnimation.Y, binding.imageView5.y)

        val imageView2Params = binding.imageView2.layoutParams as MarginLayoutParams
        val imageView3Params = binding.imageView3.layoutParams as MarginLayoutParams
        val imageView4Params = binding.imageView4.layoutParams as MarginLayoutParams
        val imageView5Params = binding.imageView5.layoutParams as MarginLayoutParams
        xAnimation2.addUpdateListener { _: DynamicAnimation<*>?, v: Float, _: Float ->
            xAnimation3.animateToFinalPosition(
                v + (binding.imageView2.width - binding.imageView3.width) / 2
            )
        }
        yAnimation2.addUpdateListener { _: DynamicAnimation<*>?, v: Float, _: Float ->
            yAnimation3.animateToFinalPosition(
                v + binding.imageView2.height + imageView3Params.topMargin
            )
        }
        xAnimation3.addUpdateListener { _: DynamicAnimation<*>?, v: Float, _: Float ->
            xAnimation4.animateToFinalPosition(
                v + (binding.imageView3.width - binding.imageView4.width) / 2
            )
        }
        yAnimation3.addUpdateListener { _: DynamicAnimation<*>?, v: Float, _: Float ->
            yAnimation4.animateToFinalPosition(
                v + binding.imageView3.height + imageView4Params.topMargin
            )
        }
        xAnimation4.addUpdateListener { _: DynamicAnimation<*>?, v: Float, _: Float ->
            xAnimation5.animateToFinalPosition(
                v + (binding.imageView4.width - binding.imageView5.width) / 2
            )
        }
        yAnimation4.addUpdateListener { _: DynamicAnimation<*>?, v: Float, _: Float ->
            yAnimation5.animateToFinalPosition(
                v + binding.imageView4.height + imageView5Params.topMargin
            )
        }

        binding.imageView.setOnTouchListener { view: View, motionEvent: MotionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - motionEvent.rawX
                    dY = view.y - motionEvent.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val newX = motionEvent.rawX + dX
                    val newY = motionEvent.rawY + dY
                    view
                        .animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start()
                    xAnimation2.animateToFinalPosition(
                        newX + (binding.imageView.width - binding.imageView2.width) / 2
                    )
                    yAnimation2.animateToFinalPosition(
                        newY + binding.imageView2.height + imageView2Params.topMargin
                    )
                }
            }
            true
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.cl_top -> {
                Timber.d("Click on top then Dismiss bottom")
                toggleTopAnimation()
            }

            R.id.cl_bottom -> {
                Timber.d("Click on bottom then Dismiss top")
                toggleBottomAnimation()
            }
        }
    }

    private fun removeListeners() {
        binding.clTop.setOnClickListener(null)
        binding.clBottom.setOnClickListener(null)
    }


    /**
     * This function moves the top card view to the center of the screen while fading out the bottom card view
     * and display the multiple views in full screen
     */
    private fun toggleTopAnimation() {
        Timber.d("toggleTopAnimation()")
        removeListeners()
        isTopAnimationToggle = !isTopAnimationToggle
        startChainedAnimations(R.id.start, R.id.bottom_dismiss_end, R.id.cl_views_container_end)
        launchAnimations()
    }


    /**
     * This function moves the bottom card view to the center of the screen while fading out the top card view
     * and display the dot container in full screen
     */
    private fun toggleBottomAnimation() {
        Timber.d("toggleBottomAnimation()")
        removeListeners()
        isBottomAnimationToggle = !isBottomAnimationToggle
        startChainedAnimations(R.id.start, R.id.top_dismiss_end, R.id.cl_dot_container_end)
    }


    /**
     * This function should collapse the multiple spring views into the card view
     * and then display the two buttons
     */
    private fun revertTopAnimation() {
        Timber.d("revertTopAnimation()")
        startChainedAnimations(R.id.cl_views_container_end, R.id.bottom_dismiss_end, R.id.start)
        isTopAnimationToggle = !isTopAnimationToggle
    }

    /**
     * This function should collapse the dot container view into the card view
     * and then display the two buttons
     */
    private fun revertBottomAnimation() {
        Timber.d("revertBottomAnimation()")
        startChainedAnimations(R.id.cl_dot_container_end, R.id.top_dismiss_end, R.id.start)
        isBottomAnimationToggle = !isBottomAnimationToggle
    }


    /**
     * Used to run animations avoid code redundancy
     */
    private fun startChainedAnimations(
        idTransitionStart: Int,
        idTransitionEnd: Int,
        idOnEndListenerTransitionEnd: Int
    ) {
        // Use idTransitionEnd as idOnEndListenerTransitionStart because the end point of the first animation
        // will be the start point of the end listener animation
        val idOnEndListenerTransitionStart: Int = idTransitionEnd

        binding.springMotionLayout.setTransition(idTransitionStart, idTransitionEnd)
        binding.springMotionLayout.transitionToEnd {
            Runnable {
                Timber.d("Expand bottom")
                binding.springMotionLayout.setTransition(
                    idOnEndListenerTransitionStart,
                    idOnEndListenerTransitionEnd
                )
                binding.springMotionLayout.transitionToEnd()
            }.run()
        }
    }


    private fun launchAnimations() {
        CoroutineScope(Dispatchers.Main).launch {
            launch {
                delay(450)
                setBounceAnimation(binding.tvTopLeft, "translationY")
            }
            launch {
                delay(750)
                getSpringAnimation(
                    binding.tvTopRight,
                    SpringAnimation.TRANSLATION_Y,
                    100f
                ).start()
            }

            launch {
                delay(500)
                // Translation on bottom left
                setBounceAnimation(binding.tvBottomLeft, "translationX")
            }
            launch {
                delay(750)
                // Scale on bottom right
                getSpringAnimation(binding.tvBottomRight, SpringAnimation.SCALE_X, 0.8f).start()
                getSpringAnimation(binding.tvBottomRight, SpringAnimation.SCALE_Y, 0.8f).start()
            }
        }
    }

    private fun setBounceAnimation(targetView: View, propertyName: String) {
        val bounceInterpolator = BounceInterpolator()
        val anim: ObjectAnimator =
            ObjectAnimator.ofFloat(targetView, propertyName, 0f, -50f, 50f, 0f)
        anim.interpolator = bounceInterpolator
        anim
            .setDuration(450)
            .start()
    }


    private fun getSpringAnimation(
        view: View,
        springAnimationType: FloatPropertyCompat<View>,
        finalPosition: Float
    ): SpringAnimation {
        val animation = SpringAnimation(view, springAnimationType)
        // create a spring with desired parameters
        val spring = SpringForce()
        spring.finalPosition = finalPosition
        spring.stiffness = SpringForce.STIFFNESS_MEDIUM // optional
        spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY // optional
        // set your animation's spring
        animation.spring = spring
        return animation
    }

}