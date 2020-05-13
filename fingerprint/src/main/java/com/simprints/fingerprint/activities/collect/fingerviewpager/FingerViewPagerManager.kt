package com.simprints.fingerprint.activities.collect.fingerviewpager

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.simprints.fingerprint.activities.collect.resources.indicatorDrawableId
import com.simprints.fingerprint.activities.collect.state.FingerCollectionState
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

class FingerViewPagerManager(
    var activeFingers: List<FingerIdentifier>,
    private val activity: FragmentActivity,
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
            val indicator = ImageView(activity)
            indicator.adjustViewBounds = true
            indicator.setOnClickListener { if (isAbleToSelectNewFinger()) onFingerSelected(index) }
            indicatorLayout.addView(indicator, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        pageAdapter = FingerPageAdapter(activity, this)
        viewPager.adapter = pageAdapter
        viewPager.offscreenPageLimit = 1
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                onFingerSelected(position)
            }
        })
        viewPager.setOnTouchListener { _, _ -> !isAbleToSelectNewFinger() }
    }

    fun setCurrentPageAndFingerStates(fingerStates: List<FingerCollectionState>, currentFingerIndex: Int) {
        refreshActiveFingersIfChanged(fingerStates)
        updateIndicatorImages(fingerStates, currentFingerIndex)
        viewPager.currentItem = currentFingerIndex
    }

    private fun refreshActiveFingersIfChanged(fingerStates: List<FingerCollectionState>) {
        val oldFingerIds = this.activeFingers
        val newFingerIds = fingerStates.map { it.id }
        if (oldFingerIds != newFingerIds) {
            activeFingers = newFingerIds
            initIndicators()
            pageAdapter.notifyDataSetChanged()
        }
    }

    private fun updateIndicatorImages(fingerStates: List<FingerCollectionState>, currentFingerIndex: Int) {
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
