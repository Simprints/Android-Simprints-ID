package com.simprints.id.activities.collectFingerprints

import android.app.ProgressDialog
import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.view.MenuItem
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
        var buttonContinue: Boolean
        fun initViewPager(onPageSelected: (Int) -> Unit, onTouch: () -> Boolean)
        fun doLaunchAlert(alertType: ALERT_TYPE)
        fun finishSuccessEnrol(result: Intent)
        fun finishSuccessAndStartMatching(intent: Intent)
        fun cancelAndFinish()

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
        fun refreshContinueButton(nbCollected: Int, promptContinue: Boolean)
        fun refreshFingerFragment()
        fun refreshScanButtonAndTimeoutBar()
    }

    interface Presenter: BasePresenter {

        val activeFingers: ArrayList<Finger>
        var currentActiveFingerNo: Int

        fun getTitle(): String
        fun refreshDisplay()

        // Lifecycle
        fun handleOnStart()
        fun handleOnStop()
        fun onActionForward()
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
    }
}
