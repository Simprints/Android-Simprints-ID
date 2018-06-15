package com.simprints.id.activities.collectFingerprints

import android.app.ProgressDialog
import android.support.annotation.DrawableRes
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Finger
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
        var timeoutBar: TimeoutBar
        var un20WakeupDialog: ProgressDialog

        fun setScanButtonListeners(onClick: () -> Unit, onLongClick: () -> Boolean)
        fun initIndicators()
    }

    interface Presenter: BasePresenter {

        val activeFingers: ArrayList<Finger>
        var currentActiveFingerNo: Int

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
    }
}
