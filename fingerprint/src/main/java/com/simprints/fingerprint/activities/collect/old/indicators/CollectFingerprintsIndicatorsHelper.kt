package com.simprints.fingerprint.activities.collect.old.indicators

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.simprints.fingerprint.activities.collect.old.CollectFingerprintsContract
import com.simprints.fingerprint.activities.collect.old.models.FingerStatus


class CollectFingerprintsIndicatorsHelper(private val context: Context,
                                          private val view: CollectFingerprintsContract.View,
                                          private val presenter: CollectFingerprintsContract.Presenter) {

    private val indicators = ArrayList<ImageView>()

    init {
        initIndicators()
    }

    // It adds an imageView for each bullet point (indicator) underneath the finger image.
    // "Indicator" indicates the scan state (good scan/bad scan/ etc...) for a specific finger.
    fun initIndicators() {
        view.indicatorLayout.removeAllViewsInLayout()
        indicators.clear()
        presenter.activeFingers.indices.forEach { fingerPosition ->
            val indicator = ImageView(context)
            indicator.adjustViewBounds = true
            indicator.setOnClickListener { handleIndicatorClick(fingerPosition) }
            indicators.add(indicator)
            view.indicatorLayout.addView(indicator, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        }
    }

    fun refreshIndicators() {
        presenter.activeFingers.indices.forEach { i ->
            val selected = presenter.currentActiveFingerNo == i
            val finger = presenter.activeFingers[i]
            indicators[i].setImageResource(finger.status.getDrawableId(selected))
        }
    }

    private fun handleIndicatorClick(fingerPosition: Int) {
        if (progressBarIsBusy()) {
            view.viewPager.currentItem = fingerPosition
        }
    }

    private fun progressBarIsBusy() =
        presenter.currentFinger().status != FingerStatus.COLLECTING &&
            presenter.currentFinger().status != FingerStatus.TRANSFERRING_IMAGE
}
