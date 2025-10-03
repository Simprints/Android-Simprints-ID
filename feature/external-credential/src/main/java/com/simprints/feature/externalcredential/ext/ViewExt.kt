package com.simprints.feature.externalcredential.ext

import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

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
