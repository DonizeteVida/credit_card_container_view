package com.doni.credit_card_view

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnLayout
import androidx.core.view.get

class CreditCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    init {
        cameraDistance = resources.displayMetrics.density * 6000

        setOnClickListener {
            flip()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        get(1).apply {
            scaleX = -1f
            alpha = 0f
        }
    }

    private var isFlipped = false
    private var isAnimationRunning = false

    private fun flip() {
        if (isAnimationRunning) {
            return
        }
        when (isFlipped) {
            true -> showFront {
                isFlipped = false
            }
            false -> showBack {
                isFlipped = true
            }
        }
    }

    private fun loadAnimator(anim: Int) = AnimatorInflater.loadAnimator(context, anim)

    private fun showFront(doOnTerminate: () -> Unit) = show(
        hideTarget = get(1),
        showTarget = get(0),
        doOnEnd = {
            doOnTerminate()
        },
        right = false
    )

    private fun showBack(doOnTerminate: () -> Unit) = show(
        hideTarget = get(0),
        showTarget = get(1),
        doOnEnd = {
            doOnTerminate()
        },
    )

    private fun show(
        doOnStart: () -> Unit = {},
        doOnEnd: () -> Unit = {},
        hideTarget: View,
        showTarget: View,
        right: Boolean = true
    ) {
        val res = if (right) {
            R.animator.flip_card_animator_right
        } else {
            R.animator.flip_card_animator_left
        }

        val flipCardAnimator = loadAnimator(res).apply {
            setTarget(this@CreditCardView)
            duration = 1500
        }

        val hideAnimator = loadAnimator(R.animator.hide_animator).apply {
            setTarget(hideTarget)
            startDelay = 750
        }
        val showAnimator = loadAnimator(R.animator.show_animator).apply {
            setTarget(showTarget)
            startDelay = 750
        }

        flipCardAnimator.apply {
            doOnStart {
                isAnimationRunning = true
                doOnStart()
            }

            doOnEnd {
                isAnimationRunning = false
                doOnEnd()
            }
            start()
        }
        hideAnimator.start()
        showAnimator.start()
    }
}