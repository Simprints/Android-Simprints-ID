package com.simprints.id.activities.collectFingerprints

import android.app.ProgressDialog
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Finger
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.tools.TimeoutBar


interface CollectFingerprintsContract {

    interface View : BaseView<Presenter> {

        // Lifecycle
        fun initViewPager(onPageSelected: (Int) -> Unit, onTouch: () -> Boolean)
        fun doLaunchAlert(alertType: ALERT_TYPE)
        fun finishSuccessEnrol(result: Intent)
        fun finishSuccessAndStartMatching(intent: Intent)
        fun cancelAndFinish()

        // Fingers
        var tryDifferentFingerSplash: android.view.View

        // Sync
        var syncItem: MenuItem

        // Scanning
        var scanButton: Button
        var pageAdapter: FingerPageAdapter
        var timeoutBar: TimeoutBar
        var un20WakeupDialog: ProgressDialog

        // Indicators
        var viewPager: ViewPagerCustom
        var indicatorLayout: LinearLayout

        // Refresh Display
        fun refreshFingerFragment()
        fun refreshScanButtonAndTimeoutBar()
    }

    interface Presenter: BasePresenter {

        val activeFingers: ArrayList<Finger>
        var currentActiveFingerNo: Int
        var isConfirmDialogShown: Boolean

        fun getTitle(): String
        fun refreshDisplay()

        // Lifecycle
        fun handleOnStart()
        fun handleOnStop()
        fun handleConfirmFingerprintsAndContinue()
        fun handleBackPressedWhileScanning()
        fun handleOnBackPressedToLeave()
        fun handleUnexpectedError(error: SimprintsError)

        // Sync
        fun handleSyncPressed()

        // Scanning
        fun isScanning(): Boolean
        fun handleTryAgainFromDifferentActivity()

        // Indicators
        fun initIndicators()

        // Finger
        fun handleAutoAddFingerPressed()
        fun handleAddFingerPressed()
        fun currentFinger(): Finger
        fun viewPagerOnPageSelected(position: Int)
        fun checkScannedFingersAndCreateMapToShowDialog()
        fun showSplashAndAddNewFingerIfNecessary()
        fun doNudgeIfNecessary()
    }
}
