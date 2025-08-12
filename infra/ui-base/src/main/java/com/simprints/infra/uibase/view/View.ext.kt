package com.simprints.infra.uibase.view

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

fun View.setPulseAnimation(isEnabled: Boolean) {
    (tag as? ObjectAnimator?)?.run {
        cancel()
        tag = null
    }
    if (!isEnabled) return
    val progressBarPulseAnimator = ObjectAnimator.ofFloat(
        this,
        View.ALPHA,
        PULSE_ANIMATION_ALPHA_FULL, PULSE_ANIMATION_ALPHA_INTERMEDIATE, PULSE_ANIMATION_ALPHA_MIN,
    ).apply {
        duration = PULSE_ANIMATION_DURATION_MILLIS
        repeatCount = ObjectAnimator.INFINITE
        repeatMode = ObjectAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
        start()
    }
    tag = progressBarPulseAnimator
}

private const val PULSE_ANIMATION_ALPHA_FULL = 1.0f
private const val PULSE_ANIMATION_ALPHA_INTERMEDIATE = 0.9f
private const val PULSE_ANIMATION_ALPHA_MIN = 0.6f
private const val PULSE_ANIMATION_DURATION_MILLIS = 2000L
