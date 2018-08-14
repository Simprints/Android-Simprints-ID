package com.simprints.id.activities.launch

import android.content.Intent
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.ALERT_TYPE


interface LaunchContract {

    interface View : BaseView<Presenter> {

        fun handleSetupProgress(progress: Int, detailsId: Int)
        fun handleSetupFinished()

        fun setLanguage(language: String)
        fun setResultAndFinish(resultCode: Int, resultData: Intent?)
        fun continueToNextActivity()
        fun goToRefusalActivity()
        fun doLaunchAlert(alertType: ALERT_TYPE)
        fun doVibrateIfNecessary(doVibrate: Boolean)

        fun setTextToGeneralConsent(generalConsentText: String)
        fun addParentalConsentTabWithText(parentalConsentText: String)
    }

    interface Presenter : BasePresenter {

        fun isReadyToProceedToNextActivity(): Boolean
        fun tryAgainFromErrorScreen()
        fun updatePositionTracker(requestCode: Int, resultCode: Int, data: Intent?)

        fun handleOnRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
        fun handleOnBackOrDeclinePressed()
        fun handleOnDestroy()

        fun tearDownAppWithResult(resultCode: Int, resultData: Intent?)
        fun confirmConsentAndContinueToNextActivity()
    }
}
