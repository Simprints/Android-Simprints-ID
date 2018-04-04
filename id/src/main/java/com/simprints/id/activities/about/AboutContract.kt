package com.simprints.id.activities.about


import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView

interface AboutContract {

    interface View : BaseView<Presenter> {

        fun setVersionData(appVersion: String, libsimprintsVersion: String, scannerVersion: String)

        fun setDbCountData(userCount: String, moduleCount: String, globalCount: String)

        fun setStartRecovering()

        fun setSuccessRecovering()

        fun setRecoveringFailed()

        fun setRecoveryAvailability(recoveryRunning: Boolean)
    }

    interface Presenter : BasePresenter {

        fun recoverDb()
    }
}
