package com.simprints.id.activities.collectFingerprints.indicators

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsContract


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
        for (i in presenter.activeFingers.indices) {
            val indicator = ImageView(context)
            indicator.adjustViewBounds = true
            indicator.setOnClickListener { view.viewPager.currentItem = i }
            indicators.add(indicator)
            view.indicatorLayout.addView(indicator, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }

    fun refreshIndicators() {
        presenter.activeFingers.indices.forEach { fingerIndex ->
            val selected = presenter.currentActiveFingerNo == fingerIndex
            val finger = presenter.activeFingers[fingerIndex]
            indicators[fingerIndex].setImageResource(finger.status.getDrawableId(selected))
        }
    }
}
