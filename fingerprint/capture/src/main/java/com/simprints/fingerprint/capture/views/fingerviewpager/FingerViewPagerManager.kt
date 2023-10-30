package com.simprints.fingerprint.capture.views.fingerviewpager

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.simprints.fingerprint.capture.resources.indicatorDrawableId
import com.simprints.fingerprint.capture.state.FingerState
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class FingerViewPagerManager(
    private val activeFingers: MutableList<IFingerIdentifier>,
    private val parentFragment: Fragment,
    private val viewPager: ViewPager2,
    private val indicatorLayout: LinearLayout,
    private val onFingerSelected: (Int) -> Unit,
    private val isAbleToSelectNewFinger: () -> Boolean) {

    private lateinit var pageAdapter: FingerPageAdapter

    init {
        initIndicators()
        initViewPager()
    }

    private fun initIndicators() {
        indicatorLayout.removeAllViewsInLayout()
        activeFingers.forEachIndexed { index, _ ->
            val indicator = ImageView(parentFragment.requireContext())
            indicator.adjustViewBounds = true
            indicator.setOnClickListener { if (isAbleToSelectNewFinger()) onFingerSelected(index) }
            indicatorLayout.addView(indicator, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        pageAdapter = FingerPageAdapter(parentFragment, activeFingers)
        viewPager.adapter = pageAdapter
        viewPager.offscreenPageLimit = 1
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                onFingerSelected(position)
            }
        })
    }

    fun setCurrentPageAndFingerStates(fingerStates: List<FingerState>, currentFingerIndex: Int) {
        refreshActiveFingersIfChanged(fingerStates)
        updateIndicatorImages(fingerStates, currentFingerIndex)
        viewPager.currentItem = currentFingerIndex
        viewPager.isUserInputEnabled = !fingerStates[currentFingerIndex].currentCapture().isCommunicating()
    }

    private fun refreshActiveFingersIfChanged(fingerStates: List<FingerState>) {
        val oldFingerIds = this.activeFingers
        val newFingerIds = fingerStates.map { it.id }
        if (oldFingerIds != newFingerIds) {
            activeFingers.clear()
            activeFingers.addAll(newFingerIds)
            initIndicators()
            pageAdapter.notifyDataSetChanged()
        }
    }

    private fun updateIndicatorImages(fingerStates: List<FingerState>, currentFingerIndex: Int) {
        fingerStates.forEachIndexed { index, fingerState ->
            val selected = currentFingerIndex == index
            indicatorLayout.children.iterator().withIndex().forEach { (i, view) ->
                if (i == index && view is ImageView) {
                    view.setImageResource(fingerState.indicatorDrawableId(selected))
                }
            }
        }
    }
}
