package com.simprints.fingerprint.activities.launch

import android.content.Intent
import com.simprints.fingerprint.activities.BasePresenter
import com.simprints.fingerprint.activities.BaseView
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Observable

interface LaunchContract {

    interface View : BaseView<Presenter> {

        fun handleSetupProgress(progress: Int, detailsId: Int)

        fun setLanguage(language: String)
        fun setResultAndFinish(resultCode: ResultCode, resultData: Intent?)
        fun continueToNextActivity()
        fun goToRefusalActivity()
        fun doLaunchAlert(fingerprintAlert: FingerprintAlert)
        fun doVibrate()
        fun showDialogForScannerErrorConfirmation(scannerId: String)
        fun dismissScannerErrorConfirmationDialog()

    }

    interface Presenter : BasePresenter {

        fun handleDeclinePressed()
        fun handleOnBackPressed()
        fun tryAgainFromErrorOrRefusal()
        fun handleScannerDisconnectedYesClick()
        fun handleScannerDisconnectedNoClick()
        fun logScannerErrorDialogShownToCrashReport()
    }
}
