package com.simprints.id.activities.collectFingerprints

import android.app.Activity
import android.content.Context
import com.simprints.id.Application
import com.simprints.id.activities.collectFingerprints.fingers.CollectFingerprintsFingerDisplayHelper
import com.simprints.id.activities.collectFingerprints.scanning.CollectFingerprintsScanningHelper
import com.simprints.id.activities.collectFingerprints.sync.CollectFingerprintsSyncHelper
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Finger
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.services.sync.SyncService
import com.simprints.id.tools.TimeHelper
import javax.inject.Inject


class CollectFingerprintsPresenter(private val context: Context,
                                   private val view: CollectFingerprintsContract.View)
    : CollectFingerprintsContract.Presenter {

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var timeHelper: TimeHelper

    private lateinit var syncHelper: CollectFingerprintsSyncHelper
    private lateinit var scanningHelper: CollectFingerprintsScanningHelper
    private lateinit var fingerDisplayHelper: CollectFingerprintsFingerDisplayHelper

    // Array with only the active Fingers, used to populate the ViewPager
    override val activeFingers = ArrayList<Finger>()
    override var currentActiveFingerNo: Int = 0

    init {
        ((view as Activity).application as Application).component.inject(this)
    }

    override fun start() {
        preferencesManager.msSinceBootOnMainStart = timeHelper.msSinceBoot()

        initSyncHelper(context, view)
        initFingerDisplayHelper(context, view)
        initScanningHelper(context, view)
        initScanButtonListeners()
        refreshDisplay()
    }

    private fun initSyncHelper(context: Context, view: CollectFingerprintsContract.View) {
        SyncService.getClient(context)
        syncHelper = CollectFingerprintsSyncHelper(context, view)
    }

    private fun initFingerDisplayHelper(context: Context, view: CollectFingerprintsContract.View) {
        fingerDisplayHelper = CollectFingerprintsFingerDisplayHelper(context, view, this)
    }

    private fun initScanningHelper(context: Context, view: CollectFingerprintsContract.View) {
        scanningHelper = CollectFingerprintsScanningHelper(context, view, this)
    }

    private fun initScanButtonListeners() {
        view.setScanButtonListeners(
            onClick = { scanningHelper.toggleContinuousCapture() },
            onLongClick = { resetFingerState() })
    }

    private fun resetFingerState(): Boolean {
        if (!isScanning()) {
            currentFinger().isNotCollected
            currentFinger().template = null
            refreshDisplay()
        }
        return true
    }

    override fun currentFinger(): Finger = activeFingers[currentActiveFingerNo]

    override fun isScanning(): Boolean = currentFinger().isCollecting

    override fun viewPagerOnPageSelected(position: Int) {
        currentActiveFingerNo = position
        refreshDisplay()
        scanningHelper.resetScannerUi()
    }

    override fun handleBackPressedWhileScanning() {
        scanningHelper.toggleContinuousCapture()
    }

    override fun handleAutoAddFingerPressed() {
        if (isScanning()) {
            scanningHelper.toggleContinuousCapture()
        }
        fingerDisplayHelper.autoAdd()
    }

    override fun handleAddFingerPressed() {
        if (isScanning()) {
            scanningHelper.toggleContinuousCapture()
        }
        fingerDisplayHelper.addFinger()
    }

    override fun refreshDisplay() {
        val (nbCollected, promptContinue) = view.refreshIndicators()
        view.refreshScanButtonAndTimeoutBar()
        view.refreshFingerFragment()
        view.refreshContinueButton(nbCollected, promptContinue)
    }

    override fun handleSyncPressed() {
        syncHelper.sync()
    }

    override fun handleTryAgain() {
        scanningHelper.reconnect()
    }

    override fun handleOnStart() {
        scanningHelper.startListeners()
        syncHelper.startListeners()
    }

    override fun handleOnStop() {
        scanningHelper.stopListeners()
        syncHelper.stopListeners()
    }

    override fun handleOnBackPressedToLeave() {
        scanningHelper.stopReconnecting()
    }

    override fun handleUnexpectedError(error: SimprintsError) {
        analyticsManager.logError(error)
        view.doLaunchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
    }
}
