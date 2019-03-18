package com.simprints.fingerprint.activities.collect

import android.app.ProgressDialog
import android.content.Intent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.simprints.fingerprint.activities.BasePresenter
import com.simprints.fingerprint.activities.BaseView
import com.simprints.fingerprint.activities.collect.models.Finger
import com.simprints.fingerprint.data.domain.alert.Alert
import com.simprints.id.exceptions.SimprintsException

interface CollectFingerprintsContract {

    interface View : BaseView<Presenter> {

        // Common
        var viewPager: ViewPagerCustom

        // Refresh Display
        fun refreshFingerFragment()
        fun refreshScanButtonAndTimeoutBar()

        // Lifecycle
        fun initViewPager(onPageSelected: (Int) -> Unit, onTouch: () -> Boolean)
        fun doLaunchAlert(alert: Alert)
        fun finishSuccessEnrol(result: Intent)
        fun finishSuccessAndStartMatching(intent: Intent)
        fun cancelAndFinish()

        fun showSplashScreen()

        // Fingers
        var pageAdapter: FingerPageAdapter

        // Scanning
        var scanButton: Button
        var progressBar: ProgressBar
        var timeoutBar: com.simprints.fingerprint.activities.collect.views.TimeoutBar
        var un20WakeupDialog: ProgressDialog

        // Indicators
        var indicatorLayout: LinearLayout
    }

    interface Presenter : BasePresenter {

        // Common
        val activeFingers: ArrayList<Finger>
        var currentActiveFingerNo: Int
        fun refreshDisplay()

        // Lifecycle
        fun getTitle(): String
        fun handleOnResume()
        fun handleOnPause()
        fun handleConfirmFingerprintsAndContinue()
        fun handleBackPressedWhileScanning()
        fun handleOnBackPressedToLeave()
        fun handleTryAgainFromDifferentActivity()
        fun handleException(simprintsException: SimprintsException)

        // Scanning
        var isConfirmDialogShown: Boolean
        fun isScanning(): Boolean

        // Indicators
        fun initIndicators()

        // Finger
        var isTryDifferentFingerSplashShown: Boolean
        var isNudging: Boolean
        fun resolveFingerTerminalConditionTriggered()

        fun currentFinger(): Finger
        fun viewPagerOnPageSelected(position: Int)
        fun handleMissingFingerClick()
        fun fingerHasSatisfiedTerminalCondition(finger: Finger): Boolean
        fun handleCaptureSuccess()
        fun handleScannerButtonPressed()
    }
}
