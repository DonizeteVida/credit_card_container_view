package com.doni.group

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.get

enum class AnimatorFactory(
    val propertyName: String, val value: Float
) {
    FLIP_RIGHT("rotationY", 180f),
    FLIP_LEFT("rotationY", 0f),
    HIDE("alpha", 0f),
    SHOW("alpha", 1f)
}

abstract class AnimatedViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    protected val animationDuration: Float

    init {
        with(context) {
            val attributes = theme.obtainStyledAttributes(
                attrs,
                R.styleable.AnimatedViewGroup,
                defStyleAttr,
                defStyleRes
            )

            cameraDistance = attributes.getFloat(
                R.styleable.AnimatedViewGroup_cameraDistance,
                6000F
            ) * resources.displayMetrics.density

            animationDuration =
                attributes.getFloat(R.styleable.AnimatedViewGroup_animationDuration, 3000F)

        }
    }

    protected fun generateAnimator(
        target: View,
        factory: AnimatorFactory,
        defaultDuration: Long = 0L,
        defaultStartDelay: Long = 0L
    ): ObjectAnimator =
        ObjectAnimator.ofFloat(target, factory.propertyName, factory.value).apply {
            duration = defaultDuration
            startDelay = defaultStartDelay
        }
}

class CreditCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : AnimatedViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    lateinit var child0: View
    lateinit var child1: View

    init {
        setOnClickListener {
            flip()
        }
    }

    override fun onAttachedToWindow() {
        child0 = get(0)
        child1 = get(1).apply {
            alpha = 0f
            scaleX = -1f
        }
        super.onAttachedToWindow()
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

    private fun showFront(doOnEnd: () -> Unit) = show(
        hideTarget = child1,
        showTarget = child0,
        doOnEnd = doOnEnd,
        right = false
    )

    private fun showBack(doOnEnd: () -> Unit) = show(
        hideTarget = child0,
        showTarget = child1,
        doOnEnd = doOnEnd,
    )

    private fun show(
        doOnStart: () -> Unit = {},
        doOnEnd: () -> Unit = {},
        hideTarget: View,
        showTarget: View,
        right: Boolean = true
    ) {
        val delay = (animationDuration * 0.222f).toLong()

        val factory = if (right) {
            AnimatorFactory.FLIP_RIGHT
        } else {
            AnimatorFactory.FLIP_LEFT
        }

        val flipCardAnimator = generateAnimator(
            target = this,
            factory = factory,
            defaultDuration = animationDuration.toLong()
        ).apply {
            interpolator = BounceInterpolator()
        }

        val hideAnimator = generateAnimator(
            target = hideTarget,
            factory = AnimatorFactory.HIDE,
            defaultDuration = 0,
            defaultStartDelay = delay
        )

        val showAnimator = generateAnimator(
            target = showTarget,
            factory = AnimatorFactory.SHOW,
            defaultDuration = 0,
            defaultStartDelay = delay
        )

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