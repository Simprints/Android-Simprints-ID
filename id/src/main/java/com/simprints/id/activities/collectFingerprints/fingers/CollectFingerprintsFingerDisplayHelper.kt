package com.simprints.id.activities.collectFingerprints.fingers

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsContract
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

    //Array with all fingers, built based on defaultScanConfig
    private var fingers = ArrayList<Finger>(Finger.NB_OF_FINGERS)

    private val defaultScanConfig = ScanConfig().apply {
        set(FingerIdentifier.LEFT_THUMB, FingerConfig.REQUIRED, 0, 0)
        set(FingerIdentifier.LEFT_INDEX_FINGER, FingerConfig.REQUIRED, 1, 1)
        set(FingerIdentifier.LEFT_3RD_FINGER, FingerConfig.OPTIONAL, 4, 2)
        set(FingerIdentifier.LEFT_4TH_FINGER, FingerConfig.OPTIONAL, 5, 3)
        set(FingerIdentifier.LEFT_5TH_FINGER, FingerConfig.OPTIONAL, 6, 4)
        set(FingerIdentifier.RIGHT_THUMB, FingerConfig.OPTIONAL, 2, 5)
        set(FingerIdentifier.RIGHT_INDEX_FINGER, FingerConfig.OPTIONAL, 3, 6)
        set(FingerIdentifier.RIGHT_3RD_FINGER, FingerConfig.OPTIONAL, 7, 7)
        set(FingerIdentifier.RIGHT_4TH_FINGER, FingerConfig.OPTIONAL, 8, 8)
        set(FingerIdentifier.RIGHT_5TH_FINGER, FingerConfig.OPTIONAL, 9, 9)
    }

    init {
        ((view as Activity).application as Application).component.inject(this)

        setFingerStatus()
        initPageAdapter()
        initViewPager()
        initActiveFingers()
    }

    private fun initPageAdapter() {
        view.pageAdapter = FingerPageAdapter((view as AppCompatActivity).supportFragmentManager, presenter.activeFingers)
    }

    // Reads the fingerStatus Map (from sharedPrefs) and "active" LEFT_THUMB and LEFT_INDEX_FINGER as
    // default finger.
    private fun setFingerStatus() {
        // We set the two defaults in the config for the first reset.
        val fingerStatus = preferencesManager.fingerStatus as MutableMap
        fingerStatus[FingerIdentifier.LEFT_THUMB] = true
        fingerStatus[FingerIdentifier.LEFT_INDEX_FINGER] = true
        preferencesManager.fingerStatus = fingerStatus
    }

    // Builds the array of "fingers" and "activeFingers" based on the info from:
    // FingerIdentifier values - all possible fingers
    // defaultScanConfig - to find out if a finger is required or not, collectable or not, etc..
    // fingerStatusPersist - to find out if a finger is active or not (added by user with "Add finger dialog" or defaults ones)
    private fun initActiveFingers() {
        FingerIdentifier.values().take(Finger.NB_OF_FINGERS).forEachIndexed { _, identifier ->

            val wasFingerAddedByUser = { preferencesManager.fingerStatusPersist && preferencesManager.fingerStatus[identifier] == true }
            val isFingerRequired = { defaultScanConfig.isFingerRequired(identifier) }
            val isFingerActive = isFingerRequired() || wasFingerAddedByUser()

            val finger = Finger(identifier, isFingerActive, defaultScanConfig.getPriority(identifier), defaultScanConfig.getOrder(identifier))
            fingers.add(finger)
            if (isFingerActive) {
                presenter.activeFingers.add(finger)
            }
        }

        presenter.activeFingers.sort()
        fingers.sort()

        updateLastFinger()
    }

    private fun initViewPager() {
        view.initViewPager(
            onPageSelected = { presenter.viewPagerOnPageSelected(it) },
            onTouch = { presenter.isScanning() })
    }

    fun addFinger() {
        val fingerOptions = arrayListOf<FingerDialogOption>().apply {
            fingers.forEach {
                val fingerName = context.getString(FingerRes.get(it).nameId)
                FingerDialogOption(fingerName, it.id, defaultScanConfig.isFingerRequired(it.id), it.isActive).also {
                    this.add(it)
                }
            }
        }

        val dialog = AddFingerDialog(context, fingerOptions, preferencesManager.fingerStatusPersist) { persistFingerState, fingersDialogOptions ->
            val currentActiveFinger = presenter.activeFingers[presenter.currentActiveFingerNo]

            val persistentFingerStatus = preferencesManager.fingerStatus as MutableMap

            //updateFingersActiveState
            fingersDialogOptions.forEach { option ->
                fingers.find { it.id == option.fingerId }?.isActive = option.active
            }
            preferencesManager.fingerStatusPersist = persistFingerState

            //remove old active fingers from MainView
            fingers
                .filter { it.isActive && !presenter.activeFingers.contains(it) }
                .forEach {
                    presenter.activeFingers.add(it)
                    persistentFingerStatus[it.id] = true
                }

            //add new active fingers from MainView
            fingers
                .filter { !it.isActive && presenter.activeFingers.contains(it) }
                .forEach {
                    presenter.activeFingers.remove(it)
                    persistentFingerStatus[it.id] = false
                }

            preferencesManager.fingerStatus = persistentFingerStatus
            presenter.activeFingers.sort()

            presenter.currentActiveFingerNo = if (currentActiveFinger.isActive) {
                presenter.activeFingers.indexOf(currentActiveFinger)
            } else {
                0
            }

            updateLastFinger()

            handleFingersChanged()
        }.create()

        (view as Activity).runOnUiThreadIfStillRunning {
            dialog.show()
        }
    }

    private fun updateLastFinger() {
        fingers.forEach { it.isLastFinger = false }
        fingers.last().isLastFinger = true
    }

    fun autoAdd() {
        presenter.activeFingers[presenter.activeFingers.size - 1].isLastFinger = false

        // Construct a list of fingers sorted by priority
        val fingersSortedByPriority = arrayOfNulls<Finger>(Finger.NB_OF_FINGERS)
        for (finger in fingers) {
            fingersSortedByPriority[finger.priority] = finger
        }

        // Auto-add the next finger sorted by the "priority" field
        for (finger in fingersSortedByPriority.filterNotNull()) {
            if (!defaultScanConfig.isFingerNotCollectable(finger.id) && !presenter.activeFingers.contains(finger)) {
                presenter.activeFingers.add(finger)
                finger.isActive = true
                break
            }
        }
        presenter.activeFingers.sort()

        presenter.activeFingers[presenter.activeFingers.size - 1].isLastFinger = true

        handleFingersChanged()
    }

    private fun handleFingersChanged() {
        view.initIndicators()
        view.pageAdapter.notifyDataSetChanged()
        view.setCurrentViewPagerItem(presenter.currentActiveFingerNo)
        presenter.refreshDisplay()
    }
}
