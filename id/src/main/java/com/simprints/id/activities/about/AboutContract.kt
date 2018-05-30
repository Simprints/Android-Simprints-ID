package com.simprints.id.activities.about

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView


interface AboutContract {

    interface View : BaseView<Presenter> {

        fun setVersionData(appVersion: String, libsimprintsVersion: String, scannerVersion: String)

        fun setUserCount(userCount: String)

        fun setProjectCount(projectCount: String)

        fun setModuleCount(moduleCount: String)

        fun setRecoveryInProgress()

        fun setSuccessRecovering()

        fun setRecoveringFailed()

    }

    interface Presenter : BasePresenter {

        fun recoverDb()

    }
}
