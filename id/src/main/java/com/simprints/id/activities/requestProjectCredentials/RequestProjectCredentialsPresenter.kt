package com.simprints.id.activities.requestProjectCredentials

import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.unsafe.ProjectCredentialsNonValidError
import com.simprints.id.tools.Log

/**
 * Created by fabiotuzza on 18/01/2018.
 */
@Suppress("UnnecessaryVariable")
class RequestProjectCredentialsPresenter(val view: RequestProjectCredentialsContract.View) : RequestProjectCredentialsContract.Presenter {

    override var secureDataManager: SecureDataManager? = null

    init {
        view.setPresenter(this)
    }

    override fun start() {
        Log.d("", "RequestProjectCredentialsPresenter started")
    }

    override fun onScanBarcodeClicked() {
        view.userDidWantToOpenScanQRApp()
    }

    override fun onEnterKeyButtonClicked(potentialProjectId: String, potentialProjectSecret: String) {
        if (tryToSaveProjectDetails(potentialProjectId, potentialProjectSecret)){
            view.dismissRequestProjectSecretActivity()
        }
    }

    override fun onActivityResultForQRScanned(potentialProjectId: String, potentialProjectSecret: String) {
        view.hideButtonToAddProjectDetailsManually()
        view.showProjectDetails()
        view.updateProjectIdInTextView(potentialProjectId)
        view.updateProjectSecretInTextView(potentialProjectSecret)
    }

    private fun tryToSaveProjectDetails(potentialProjectId: String, potentialProjectSecret: String):Boolean {
        if (secureDataManager == null) { throw Exception("Dependencies not injected!") }
        return try {
            secureDataManager!!.projectId = potentialProjectId
            secureDataManager!!.projectSecret = potentialProjectSecret
            true
        } catch (e: ProjectCredentialsNonValidError){
            view.showErrorForInvalidProjectCredentials()
            false
        }
    }
}
