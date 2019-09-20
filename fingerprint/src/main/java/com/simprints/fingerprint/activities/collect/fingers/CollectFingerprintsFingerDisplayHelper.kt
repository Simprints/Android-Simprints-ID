package com.simprints.fingerprint.activities.collect.fingers

import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.simprints.fingerprint.activities.collect.CollectFingerprintsContract
import com.simprints.fingerprint.activities.collect.FingerPageAdapter
import com.simprints.fingerprint.activities.collect.models.DefaultScanConfig
import com.simprints.fingerprint.activities.collect.models.Finger
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.tools.extensions.isFingerNotCollectable

class CollectFingerprintsFingerDisplayHelper(private val view: CollectFingerprintsContract.View,
                                             private val presenter: CollectFingerprintsContract.Presenter,
                                             var fingerStatus: Map<FingerIdentifier, Boolean>,
                                             private val androidResourcesHelper: FingerprintAndroidResourcesHelper) {


    private val allFingers = ArrayList<Finger>(Finger.NB_OF_FINGERS)

    init {
        clearAndPopulateFingerArrays()
        initPageAdapter()
        initViewPager()
    }

    fun clearAndPopulateFingerArrays() {
        clearFingerArrays()
        populateFingerArrays()
    }

    private fun clearFingerArrays() {
        allFingers.clear()
        presenter.activeFingers.clear()
    }

    private fun populateFingerArrays() {
        FingerIdentifier.values().take(Finger.NB_OF_FINGERS).forEach { id ->
            val finger = createFinger(id)
            allFingers.add(finger)
            if (finger.isActive) presenter.activeFingers.add(finger)
        }
        allFingers.sort()
        presenter.activeFingers.sort()
        refreshWhichFingerIsLast()
    }

    private fun createFinger(id: FingerIdentifier): Finger {
        val isFingerActive = isFingerRequired(id)
        val fingerPriority = defaultScanConfig.getPriority(id)
        val fingerOrder = defaultScanConfig.getOrder(id)
        return Finger(id, isFingerActive, fingerPriority, fingerOrder)
    }

    private fun isFingerRequired(identifier: FingerIdentifier) = fingerStatus[identifier] == true

    private fun refreshWhichFingerIsLast() {
        presenter.activeFingers.forEach { it.isLastFinger = false }
        presenter.activeFingers.last().isLastFinger = true
    }

    private fun initPageAdapter() {
        view.pageAdapter = FingerPageAdapter((view as AppCompatActivity).supportFragmentManager, presenter.activeFingers, androidResourcesHelper)
    }

    private fun initViewPager() {
        view.initViewPager(
            onPageSelected = { presenter.viewPagerOnPageSelected(it) },
            onTouch = { presenter.isScanning() })
    }

    fun resetFingerIndexToBeginning() {
        view.viewPager.currentItem = 0
        presenter.currentActiveFingerNo = 0
    }

    private fun handleAutoAddFinger() {
        val fingersSortedByPriority = getFingersSortedByPriority()
        addNextFingerInPriorityList(fingersSortedByPriority)
        presenter.activeFingers.sort()
        refreshWhichFingerIsLast()
        handleFingersChanged()
    }

    private fun getFingersSortedByPriority(): List<Finger> {
        val fingersSortedByPriority = arrayOfNulls<Finger>(Finger.NB_OF_FINGERS)
        allFingers.forEach { finger ->
            fingersSortedByPriority[finger.priority] = finger
        }
        return fingersSortedByPriority.filterNotNull()
    }

    private fun addNextFingerInPriorityList(fingersSortedByPriority: List<Finger>) {
        for (finger in fingersSortedByPriority) {
            if (!defaultScanConfig.isFingerNotCollectable(finger.id) && !presenter.activeFingers.contains(finger)) {
                presenter.activeFingers.add(finger)
                finger.isActive = true
                break
            }
        }
    }

    fun handleFingersChanged() {
        presenter.initIndicators()
        view.pageAdapter.notifyDataSetChanged()
        view.viewPager.currentItem = presenter.currentActiveFingerNo
        presenter.refreshDisplay()
    }

    // Swipes ViewPager automatically when the current finger is complete
    fun doNudge() {
        if (presenter.currentActiveFingerNo < presenter.activeFingers.size) {
            presenter.isBusyWithFingerTransitionAnimation = true
            Handler().postDelayed({
                view.viewPager.setScrollDuration(SLOW_SWIPE_SPEED)
                view.viewPager.currentItem = presenter.currentActiveFingerNo + 1
                view.viewPager.setScrollDuration(FAST_SWIPE_SPEED)
                presenter.isBusyWithFingerTransitionAnimation = false
            }, AUTO_SWIPE_DELAY)
        }
    }

    fun showSplashAndNudgeAndAddNewFinger() {
        showTryDifferentFingerSplash()
        Handler().postDelayed({
            handleAutoAddFinger()
            doNudge()
        }, TRY_DIFFERENT_FINGER_SPLASH_DELAY)
    }

    fun showSplashAndNudgeIfNecessary() {
        showTryDifferentFingerSplash()
        doNudge()
    }


    private fun showTryDifferentFingerSplash() {
        view.showSplashScreen()
    }

    companion object {
        private const val AUTO_SWIPE_DELAY: Long = 500
        private const val FAST_SWIPE_SPEED = 100
        private const val SLOW_SWIPE_SPEED = 1000

        const val TRY_DIFFERENT_FINGER_SPLASH_DELAY: Long = 2000

        private val defaultScanConfig = DefaultScanConfig()
    }
}
