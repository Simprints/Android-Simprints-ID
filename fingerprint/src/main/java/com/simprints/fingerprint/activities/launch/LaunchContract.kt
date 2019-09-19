package com.simprints.fingerprint.activities.launch

import android.content.Intent
import com.simprints.fingerprint.activities.BasePresenter
import com.simprints.fingerprint.activities.BaseView
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Observable

interface LaunchContract {

    interface View : BaseView<Presenter> {

        fun handleSetupProgress(progress: Int, detailsId: Int)
        fun handleSetupFinished()

        fun setLogoVisibility(visible: Boolean)
        fun setResultAndFinish(resultCode: Int, resultData: Intent?)
        fun continueToNextActivity()
        fun goToRefusalActivity()
        fun doLaunchAlert(fingerprintAlert: FingerprintAlert)
        fun doVibrate()
        fun showDialogForScannerErrorConfirmation(scannerId: String)
        fun dismissScannerErrorConfirmationDialog()

        fun setTextToGeneralConsent(generalConsentText: String)
        fun addParentalConsentTabWithText(parentalConsentText: String)
        fun isCurrentTabParental(): Boolean
        fun requestPermissions(permissions: ArrayList<String>): Observable<Permission>
    }

    interface Presenter : BasePresenter {

        fun handleOnDestroy()
        fun handleOnResume()
        fun handleOnPause()

        fun tearDownAppWithResult(resultCode: Int, resultData: Intent?)
        fun confirmConsentAndContinueToNextActivity()
        fun handleDeclinePressed()
        fun handleOnBackPressed()
        fun tryAgainFromErrorScreen()
        fun onActivityResult()
        fun handleScannerDisconnectedYesClick()
        fun handleScannerDisconnectedNoClick()
    }
}
