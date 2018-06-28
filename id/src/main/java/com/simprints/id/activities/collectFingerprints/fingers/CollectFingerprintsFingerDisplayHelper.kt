package com.simprints.id.activities.collectFingerprints.fingers

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsContract
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsPresenter
import com.simprints.id.activities.collectFingerprints.FingerPageAdapter
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.Finger
import com.simprints.id.domain.FingerRes
import com.simprints.id.tools.extensions.isFingerNotCollectable
import com.simprints.id.tools.extensions.isFingerRequired
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.libcommon.FingerConfig
import com.simprints.libcommon.ScanConfig
import com.simprints.libsimprints.FingerIdentifier
import javax.inject.Inject


class CollectFingerprintsFingerDisplayHelper(private val context: Context,
                                             private val view: CollectFingerprintsContract.View,
                                             private val presenter: CollectFingerprintsContract.Presenter) {

    @Inject lateinit var preferencesManager: PreferencesManager

    private val allFingers = ArrayList<Finger>(Finger.NB_OF_FINGERS)

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

    init {
        ((view as Activity).application as Application).component.inject(this)
        clearAndPopulateFingerArraysWithDefaultFingers()
        initPageAdapter()
        initViewPager()
    }

    fun clearAndPopulateFingerArraysWithDefaultFingers() {
        loadFingerStatusAndRefreshWithDefaultFingers()
        clearFingerArrays()
        populateFingerArrays()
    }

    private fun loadFingerStatusAndRefreshWithDefaultFingers() {
        val fingerStatus = preferencesManager.fingerStatus as MutableMap
        fingerStatus[FingerIdentifier.LEFT_THUMB] = true
        fingerStatus[FingerIdentifier.LEFT_INDEX_FINGER] = true
        preferencesManager.fingerStatus = fingerStatus
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
        val isFingerActive = isFingerRequired(id) || wasFingerAddedByUser(id)
        val fingerPriority = defaultScanConfig.getPriority(id)
        val fingerOrder = defaultScanConfig.getOrder(id)
        return Finger(id, isFingerActive, fingerPriority, fingerOrder)
    }

    private fun isFingerRequired(identifier: FingerIdentifier) = defaultScanConfig.isFingerRequired(identifier)

    private fun wasFingerAddedByUser(identifier: FingerIdentifier) =
        preferencesManager.fingerStatusPersist && preferencesManager.fingerStatus[identifier] == true

    private fun refreshWhichFingerIsLast() {
        allFingers.forEach { it.isLastFinger = false }
        allFingers.last().isLastFinger = true
    }

    private fun initPageAdapter() {
        view.pageAdapter = FingerPageAdapter((view as AppCompatActivity).supportFragmentManager, presenter.activeFingers)
    }

    private fun initViewPager() {
        view.initViewPager(
            onPageSelected = { presenter.viewPagerOnPageSelected(it) },
            onTouch = { presenter.isScanning() })
    }

    fun launchAddFingerDialog() {
        val fingerOptions = createAddFingerOptionsList()

        val dialog = AddFingerDialog(context, fingerOptions,
            preferencesManager.fingerStatusPersist,
            onAddFingerPositiveButton()
        ).create()

        (view as Activity).runOnUiThreadIfStillRunning { dialog.show() }
    }

    private fun onAddFingerPositiveButton() = { shouldPersistFingerState: Boolean, fingersDialogOptions: ArrayList<FingerDialogOption> ->
        val currentFinger = presenter.currentFinger()
        val fingerStates = updateActiveFingersAndGetFingerStates(fingersDialogOptions)
        saveFingerStates(shouldPersistFingerState, fingerStates)
        refreshIndexOfCurrentFinger(currentFinger)
        refreshWhichFingerIsLast()
        handleFingersChanged()
    }

    private fun updateActiveFingersAndGetFingerStates(fingersDialogOptions: ArrayList<FingerDialogOption>): MutableMap<FingerIdentifier, Boolean> {
        val fingerStates = preferencesManager.fingerStatus as MutableMap
        updateAllFingersActiveState(fingersDialogOptions)
        updateFingerStatesAndAddNewActiveFingers(fingerStates)
        updateFingerStatesAndRemoveOldActiveFingers(fingerStates)
        presenter.activeFingers.sort()
        return fingerStates
    }

    private fun createAddFingerOptionsList(): ArrayList<FingerDialogOption> =
        arrayListOf<FingerDialogOption>().apply {
            allFingers.forEach {
                val fingerName = context.getString(FingerRes.get(it).nameId)
                FingerDialogOption(fingerName, it.id, defaultScanConfig.isFingerRequired(it.id), it.isActive)
                    .also {
                        add(it)
                    }
            }
        }

    private fun updateAllFingersActiveState(fingersDialogOptions: ArrayList<FingerDialogOption>) {
        fingersDialogOptions.forEach { option ->
            allFingers.find { it.id == option.fingerId }?.isActive = option.active
        }
    }

    private fun updateFingerStatesAndAddNewActiveFingers(fingerStates: MutableMap<FingerIdentifier, Boolean>) {
        allFingers
            .filter { it.isActive && !presenter.activeFingers.contains(it) }
            .forEach {
                presenter.activeFingers.add(it)
                fingerStates[it.id] = true
            }
    }

    private fun updateFingerStatesAndRemoveOldActiveFingers(fingerStates: MutableMap<FingerIdentifier, Boolean>) {
        allFingers
            .filter { !it.isActive && presenter.activeFingers.contains(it) }
            .forEach {
                presenter.activeFingers.remove(it)
                fingerStates[it.id] = false
            }
    }

    private fun refreshIndexOfCurrentFinger(currentFinger: Finger) =
        if (presenter.activeFingers.contains(currentFinger)) {
            presenter.currentActiveFingerNo = presenter.activeFingers.indexOf(currentFinger)
        } else {
            resetFingerIndexToBeginning()
        }

    fun resetFingerIndexToBeginning() {
        view.viewPager.currentItem = 0
        presenter.currentActiveFingerNo = 0
    }

    private fun saveFingerStates(persistFingerState: Boolean, persistentFingerStatus: MutableMap<FingerIdentifier, Boolean>) {
        preferencesManager.fingerStatusPersist = persistFingerState
        preferencesManager.fingerStatus = persistentFingerStatus
    }

    fun handleAutoAddFinger() {
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
            Handler().postDelayed({
                if (presenter.currentActiveFingerNo < presenter.activeFingers.size) {
                    view.viewPager.setScrollDuration(SLOW_SWIPE_SPEED)
                    view.viewPager.currentItem = presenter.currentActiveFingerNo + 1
                    view.viewPager.setScrollDuration(FAST_SWIPE_SPEED)
                }
            }, AUTO_SWIPE_DELAY)
        }
    }

    companion object {
        private const val AUTO_SWIPE_DELAY: Long = 500
        private const val FAST_SWIPE_SPEED = 100
        private const val SLOW_SWIPE_SPEED = 1000
    }
}
