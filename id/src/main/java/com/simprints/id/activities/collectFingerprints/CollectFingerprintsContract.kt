package com.simprints.id.activities.collectFingerprints

import android.app.ProgressDialog
import android.support.annotation.DrawableRes
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
        fun cancelAndFinish()
        fun onActionForward()
        fun doLaunchAlert(alertType: ALERT_TYPE)

        // Sync
        fun setSyncItem(enabled: Boolean, title: String, @DrawableRes icon: Int)

        // Scanning
        var pageAdapter: FingerPageAdapter
        var timeoutBar: TimeoutBar
        var un20WakeupDialog: ProgressDialog

        fun setScanButtonListeners(onClick: () -> Unit, onLongClick: () -> Boolean)
        fun initIndicators()
        fun refreshContinueButton(nbCollected: Int, promptContinue: Boolean)
        fun refreshFingerFragment()
        fun refreshScanButtonAndTimeoutBar()
        fun nudgeMode()
        fun refreshIndicators(): Pair<Int, Boolean>
        fun setScanButtonEnabled(enabled: Boolean)
        fun setCurrentViewPagerItem(idx: Int)
        fun initViewPager(onPageSelected: (Int) -> Unit, onTouch: () -> Boolean)
    }

    interface Presenter: BasePresenter {

        val activeFingers: ArrayList<Finger>
        var currentActiveFingerNo: Int

        fun refreshDisplay()

        // Lifecycle
        fun handleOnStart()
        fun handleOnStop()
        fun handleOnBackPressedToLeave()
        fun handleBackPressedWhileScanning()

        // Sync
        fun handleSyncPressed()

        // Scanning
        fun isScanning(): Boolean
        fun handleTryAgain()

        // Finger
        fun handleAutoAddFingerPressed()
        fun handleAddFingerPressed()
        fun currentFinger(): Finger
        fun handleUnexpectedError(error: SimprintsError)
        fun viewPagerOnPageSelected(position: Int)
    }
}
