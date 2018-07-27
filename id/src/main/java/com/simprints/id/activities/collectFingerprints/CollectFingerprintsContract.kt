package com.simprints.id.activities.collectFingerprints

import android.app.ProgressDialog
import android.content.Intent
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Finger
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.tools.TimeoutBar


interface CollectFingerprintsContract {

    interface View : BaseView<Presenter> {

        // Common
        var viewPager: ViewPagerCustom

        // Refresh Display
        fun refreshFingerFragment()
        fun refreshScanButtonAndTimeoutBar()

        // Lifecycle
        fun initViewPager(onPageSelected: (Int) -> Unit, onTouch: () -> Boolean)
        fun doLaunchAlert(alertType: ALERT_TYPE)
        fun finishSuccessEnrol(result: Intent)
        fun finishSuccessAndStartMatching(intent: Intent)
        fun cancelAndFinish()

        // Fingers
        var tryDifferentFingerSplash: android.view.View
        var pageAdapter: FingerPageAdapter

        // Sync
        var syncItem: MenuItem

        // Scanning
        var scanButton: Button
        var progressBar: ProgressBar
        var timeoutBar: TimeoutBar
        var un20WakeupDialog: ProgressDialog

        // Indicators
        var indicatorLayout: LinearLayout
    }

    interface Presenter: BasePresenter {

        // Common
        val activeFingers: ArrayList<Finger>
        var currentActiveFingerNo: Int
        fun refreshDisplay()

        // Lifecycle
        fun getTitle(): String
        fun handleOnStart()
        fun handleOnStop()
        fun handleConfirmFingerprintsAndContinue()
        fun handleBackPressedWhileScanning()
        fun handleOnBackPressedToLeave()
        fun handleTryAgainFromDifferentActivity()
        fun handleUnexpectedError(error: SimprintsError)

        // Sync
        fun handleSyncPressed()

        // Scanning
        var isConfirmDialogShown: Boolean
        fun isScanning(): Boolean

        // Indicators
        fun initIndicators()

        // Finger
        var isTryDifferentFingerSplashShown: Boolean
        var isNudging: Boolean
        fun handleAutoAddFingerPressed()
        fun handleAddFingerPressed()
        fun currentFinger(): Finger
        fun viewPagerOnPageSelected(position: Int)
        fun checkScannedFingersAndCreateMapToShowDialog()
        fun showSplashAndAddNewFingerIfNecessary()
        fun doNudgeIfNecessary()
    }
}
