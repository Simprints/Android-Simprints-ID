package com.simprints.id.activities.requestProjectCredentials

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView

interface RequestProjectCredentialsContract {

    interface View : BaseView<Presenter> {
        fun openScanQRApp()
        fun updateProjectSecretInTextView(projectSecret: String)
        fun updateProjectIdInTextView(projectId: String)
        fun showErrorForInvalidProjectCredentials()
        fun dismissRequestProjectSecretActivity()
        fun showProjectDetails()
        fun hideButtonToAddProjectDetailsManually()
    }

    interface Presenter : BasePresenter {

        fun userDidWantToOpenScanQRApp()
        fun onActivityResultForQRScanned(potentialProjectId: String, potentialProjectSecret: String)
        fun userDidWantToEnterNewProjectCredentials(potentialProjectId: String, potentialProjectSecret: String)
    }
}
