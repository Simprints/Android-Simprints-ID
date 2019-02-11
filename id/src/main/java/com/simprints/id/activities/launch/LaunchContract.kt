package com.simprints.id.activities.launch

import android.content.Intent
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.ALERT_TYPE
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Observable
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

interface LaunchContract {

    interface View : BaseView<Presenter> {

        fun handleSetupProgress(progress: Int, detailsId: Int)
        fun handleSetupFinished()

        fun setLanguage(language: String)
        fun initTextsInButtons()
        fun initConsentTabs()
        fun setResultAndFinish(resultCode: Int, resultData: Intent?)
        fun continueToNextActivity()
        fun goToRefusalActivity()
        fun doLaunchAlert(alertType: ALERT_TYPE)
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
