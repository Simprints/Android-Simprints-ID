package com.simprints.id.activities.collectFingerprints.fingers

import android.app.Activity
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsContract
import com.simprints.id.activities.collectFingerprints.FingerPageAdapter
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.Finger
import com.simprints.id.tools.extensions.isFingerNotCollectable
import com.simprints.libcommon.FingerConfig
import com.simprints.libcommon.ScanConfig
import com.simprints.libsimprints.FingerIdentifier
import javax.inject.Inject


class CollectFingerprintsFingerDisplayHelper(private val view: CollectFingerprintsContract.View,
                                             private val presenter: CollectFingerprintsContract.Presenter) {

    @Inject lateinit var preferencesManager: PreferencesManager

    private val allFingers = ArrayList<Finger>(Finger.NB_OF_FINGERS)

    init {
        ((view as Activity).application as Application).component.inject(this)
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

    private fun isFingerRequired(identifier: FingerIdentifier) = preferencesManager.fingerStatus[identifier] == true

    private fun refreshWhichFingerIsLast() {
        presenter.activeFingers.forEach { it.isLastFinger = false }
        presenter.activeFingers.last().isLastFinger = true
    }

    private fun initPageAdapter() {
        view.pageAdapter = FingerPageAdapter((view as AppCompatActivity).supportFragmentManager, presenter.activeFingers)
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
    fun doNudgeIfNecessary() {
        if (preferencesManager.nudgeMode) {
            if (presenter.currentActiveFingerNo < presenter.activeFingers.size) {
                presenter.isNudging = true
                Handler().postDelayed({
                    view.viewPager.setScrollDuration(SLOW_SWIPE_SPEED)
                    view.viewPager.currentItem = presenter.currentActiveFingerNo + 1
                    view.viewPager.setScrollDuration(FAST_SWIPE_SPEED)
                    presenter.isNudging = false
                }, AUTO_SWIPE_DELAY)
            }
        }
    }

    fun showSplashAndNudgeAndAddNewFinger() {
        showTryDifferentFingerSplash()
        Handler().postDelayed({
            handleAutoAddFinger()
            doNudgeIfNecessary()
        }, TRY_DIFFERENT_FINGER_SPLASH_DELAY)
    }

    fun showSplashAndNudgeIfNecessary() {
        showTryDifferentFingerSplash()
        doNudgeIfNecessary()
    }


    private fun showTryDifferentFingerSplash() {
        view.showSplashScreen()
    }

    companion object {
        private const val AUTO_SWIPE_DELAY: Long = 500
        private const val FAST_SWIPE_SPEED = 100
        private const val SLOW_SWIPE_SPEED = 1000

        const val TRY_DIFFERENT_FINGER_SPLASH_DELAY: Long = 2000

        private val defaultScanConfig = ScanConfig().apply {
            set(FingerIdentifier.LEFT_THUMB, FingerConfig.REQUIRED, 0, 0)
            set(FingerIdentifier.LEFT_INDEX_FINGER, FingerConfig.REQUIRED, 1, 1)
            set(FingerIdentifier.LEFT_3RD_FINGER, FingerConfig.OPTIONAL, 4, 2)
            set(FingerIdentifier.LEFT_4TH_FINGER, FingerConfig.OPTIONAL, 6, 3)
            set(FingerIdentifier.LEFT_5TH_FINGER, FingerConfig.OPTIONAL, 8, 4)
            set(FingerIdentifier.RIGHT_THUMB, FingerConfig.OPTIONAL, 2, 5)
            set(FingerIdentifier.RIGHT_INDEX_FINGER, FingerConfig.OPTIONAL, 3, 6)
            set(FingerIdentifier.RIGHT_3RD_FINGER, FingerConfig.OPTIONAL, 5, 7)
            set(FingerIdentifier.RIGHT_4TH_FINGER, FingerConfig.OPTIONAL, 7, 8)
            set(FingerIdentifier.RIGHT_5TH_FINGER, FingerConfig.OPTIONAL, 9, 9)
        }
    }
}
