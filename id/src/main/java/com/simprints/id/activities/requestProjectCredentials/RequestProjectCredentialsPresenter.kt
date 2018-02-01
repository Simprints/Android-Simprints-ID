package com.simprints.id.activities.requestProjectCredentials

import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.unsafe.ProjectCredentialsNonValidError
import com.simprints.id.tools.Log

@Suppress("UnnecessaryVariable")
class RequestProjectCredentialsPresenter(val view: RequestProjectCredentialsContract.View, val secureDataManager: SecureDataManager) : RequestProjectCredentialsContract.Presenter {

    init {
        view.setPresenter(this)
    }

    override fun start() {
        Log.d("", "RequestProjectCredentialsPresenter started")
    }

    override fun userDidWantToOpenScanQRApp() {
        view.openScanQRApp()
    }

    override fun userDidWantToEnterNewProjectCredentials(potentialProjectId: String, potentialProjectSecret: String) {
        tryToSaveProjectDetails(potentialProjectId, potentialProjectSecret)
    }

    override fun onActivityResultForQRScanned(potentialProjectId: String, potentialProjectSecret: String) {
        view.hideButtonToAddProjectDetailsManually()
        view.showProjectDetails()
        view.updateProjectIdInTextView(potentialProjectId)
        view.updateProjectSecretInTextView(potentialProjectSecret)
    }

    private fun tryToSaveProjectDetails(potentialProjectId: String, potentialProjectSecret: String) {
        try {
            secureDataManager.projectId = potentialProjectId
            secureDataManager.projectSecret = potentialProjectSecret
            view.dismissRequestProjectSecretActivity()
        } catch (e: ProjectCredentialsNonValidError){
            view.showErrorForInvalidProjectCredentials()
        }
    }
}
