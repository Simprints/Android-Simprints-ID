package com.simprints.id.activities.launch

import android.content.Intent
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView


interface LaunchContract {

    interface View : BaseView<Presenter> {

        fun handleSetupProgress(progress: Int, detailsId: Int)
        fun handleSetupFinished()

        fun setResultAndFinish(resultCode: Int, resultData: Intent?)
        fun continueToNextActivity()
        fun goToRefusalActivity()
    }

    interface Presenter : BasePresenter {

        fun isReadyToProceedToNextActivity(): Boolean
        fun tryAgainFromErrorScreen()
        fun updatePositionTracker(requestCode: Int, resultCode: Int, data: Intent?)

        fun handleOnRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
        fun handleOnBackPressed()
        fun handleOnDestroy()

        fun tearDownAppWithResult(resultCode: Int, resultData: Intent?)
        fun confirmConsentAndContinueToNextActivity()
    }
}
