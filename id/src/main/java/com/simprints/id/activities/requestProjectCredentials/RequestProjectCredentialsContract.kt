package com.simprints.id.activities.requestProjectCredentials

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.data.secure.SecureDataManager

interface RequestProjectCredentialsContract {

    interface View : BaseView<Presenter> {
        fun userDidWantToOpenScanQRApp()
        fun updateProjectSecretInTextView(projectSecret: String)
        fun updateProjectIdInTextView(projectId: String)
        fun showErrorForInvalidProjectCredentials()
        fun dismissRequestProjectSecretActivity()
        fun showProjectDetails()
        fun hideButtonToAddProjectDetailsManually()
    }

    interface Presenter : BasePresenter {
        var secureDataManager: SecureDataManager?

        fun onScanBarcodeClicked()
        fun onActivityResultForQRScanned(potentialProjectId: String, potentialProjectSecret: String)
        fun onEnterKeyButtonClicked(potentialProjectId: String, potentialProjectSecret: String)
    }
}
