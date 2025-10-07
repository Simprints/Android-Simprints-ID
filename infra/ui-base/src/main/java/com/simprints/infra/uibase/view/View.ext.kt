package com.simprints.infra.uibase.view

import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

fun View.setPulseAnimation(isEnabled: Boolean) {
    (tag as? ObjectAnimator?)?.run {
        cancel()
        tag = null
    }
    if (!isEnabled) return
    val progressBarPulseAnimator = ObjectAnimator
        .ofFloat(
            this,
            View.ALPHA,
            PULSE_ANIMATION_ALPHA_FULL,
            PULSE_ANIMATION_ALPHA_INTERMEDIATE,
            PULSE_ANIMATION_ALPHA_MIN,
        ).apply {
            duration = PULSE_ANIMATION_DURATION_MILLIS
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    tag = progressBarPulseAnimator
}
@ExcludedFromGeneratedTestCoverageReports("View animation")
fun View.animateOut(
    duration: Long,
    scaleX: Boolean,
    fragment: Fragment,
): ViewPropertyAnimator {
    return animate()
        .alpha(0f)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction {
            if (fragment.isAdded) {
                this.isVisible = false
            }
        }.also {
            if (scaleX) {
                it.scaleX(0f)
            }
            it.start()
        }
}

@ExcludedFromGeneratedTestCoverageReports("View animation")
fun View.animateIn(
    duration: Long,
    onComplete: (() -> Unit)?,
    fragment: Fragment,
): ViewPropertyAnimator {
    return animate()
        .alpha(1f)
        .setInterpolator(AccelerateInterpolator())
        .setDuration(duration)
        .withEndAction {
            if (fragment.isAdded) {
                this.isVisible = true
                onComplete?.invoke()
            }
        }
        .also { it.start() }
}

private const val PULSE_ANIMATION_ALPHA_FULL = 1.0f
private const val PULSE_ANIMATION_ALPHA_INTERMEDIATE = 0.9f
private const val PULSE_ANIMATION_ALPHA_MIN = 0.6f
private const val PULSE_ANIMATION_DURATION_MILLIS = 2000L
