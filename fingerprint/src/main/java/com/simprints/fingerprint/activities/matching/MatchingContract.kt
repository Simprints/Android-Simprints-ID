package com.simprints.fingerprint.activities.matching


import android.content.Intent
import com.simprints.fingerprint.activities.BasePresenter
import com.simprints.fingerprint.activities.BaseView


interface MatchingContract {

    interface View : BaseView<Presenter> {

        fun setIdentificationProgress(progress: Int)

        fun setVerificationProgress()

        fun setIdentificationProgressLoadingStart()

        fun setIdentificationProgressMatchingStart(matchSize: Int)

        fun setIdentificationProgressReturningStart()

        fun setIdentificationProgressFinished(returnSize: Int, tier1Or2Matches: Int, tier3Matches: Int, tier4Matches: Int, matchingEndWaitTimeMillis: Int)

        fun launchAlert()

        fun makeToastMatchFailed()

        fun doSetResult(resultCode: Int, resultData: Intent)

        fun doFinish()
    }

    interface Presenter : BasePresenter {

        fun dispose()
    }
}
