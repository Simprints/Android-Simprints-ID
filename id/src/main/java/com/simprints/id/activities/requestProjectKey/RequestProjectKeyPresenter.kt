package com.simprints.id.activities.requestProjectKey

import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.unsafe.ProjectKeyNonValid
import com.simprints.id.tools.Log

/**
 * Created by fabiotuzza on 18/01/2018.
 */
@Suppress("UnnecessaryVariable")
class RequestProjectKeyPresenter(val view: RequestProjectKeyContract.View) : RequestProjectKeyContract.Presenter {

    override var secureDataManager: SecureDataManager? = null

    init {
        view.setPresenter(this)
    }

    override fun start() {
        Log.d("", "RequestProjectKeyPresenter started")
    }

    override fun onScanBarcodeClicked() {
        view.userDidWantToOpenScanQRApp()
    }

    override fun onEnterKeyButtonClicked(potentialProjectKey: String) {
        if (tryToSaveProject(potentialProjectKey)){
            view.dismissRequestProjectKeyActivity()
        }
    }

    override fun onActivityResultForQRScanned(potentialProjectKey: String) {
        view.updateProjectKeyInTextView(potentialProjectKey)
    }

    private fun tryToSaveProject(potentialProjectKey: String):Boolean {
        if (secureDataManager == null) { throw Exception("Dependencies not injected!") }

        return try {
            secureDataManager!!.projectKey = potentialProjectKey
            true
        } catch (e: ProjectKeyNonValid){
            view.showErrorForInvalidKey()
            false
        }
    }
}
