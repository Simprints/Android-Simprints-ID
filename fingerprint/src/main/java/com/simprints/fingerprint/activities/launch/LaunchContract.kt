package com.simprints.fingerprint.activities.launch

import android.content.Intent
import com.simprints.fingerprint.activities.BasePresenter
import com.simprints.fingerprint.activities.BaseView
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Observable

interface LaunchContract {

    interface View : BaseView<Presenter> {

        fun handleSetupProgress(progress: Int, detailsId: Int)
        fun handleSetupFinished()

        fun setLanguage(language: String)
        fun initTextsInButtons()
        fun initConsentTabs()
        fun setLogoVisibility(visible: Boolean)
        fun setResultAndFinish(resultCode: Int, resultData: Intent?)
        fun continueToNextActivity()
        fun goToRefusalActivity()
        fun doLaunchAlert(alert: FingerprintAlert)
        fun doVibrateIfNecessary(doVibrate: Boolean)

        fun setTextToGeneralConsent(generalConsentText: String)
        fun addParentalConsentTabWithText(parentalConsentText: String)
        fun isCurrentTabParental(): Boolean
        fun requestPermissions(permissions: ArrayList<String>): Observable<Permission>
    }

    interface Presenter : BasePresenter {

        fun tryAgainFromErrorScreen()

        fun handleOnDestroy()
        fun handleOnResume()
        fun handleOnPause()

        fun tearDownAppWithResult(resultCode: Int, resultData: Intent?)
        fun confirmConsentAndContinueToNextActivity()
        fun handleDeclinePressed()
        fun handleOnBackPressed()
    }
}
