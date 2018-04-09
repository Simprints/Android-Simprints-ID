package com.simprints.id.activities.about


import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView

interface AboutContract {

    interface View : BaseView<Presenter> {

        fun setVersionData(appVersion: String, libsimprintsVersion: String, scannerVersion: String)

        fun setDbCountData(userCount: String, moduleCount: String, globalCount: String)

        fun setRecoveryInProgress()

        fun setSuccessRecovering()

        fun setRecoveringFailed()
    }

    interface Presenter : BasePresenter {

        fun recoverDb()
    }
}
