package com.simprints.id.activities.requestProjectKey

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.data.secure.SecureDataManager

/**
 * Created by fabiotuzza on 18/01/2018.
 */
interface RequestProjectKeyContract {

    interface View : BaseView<Presenter> {
        fun userDidWantToOpenScanQRApp() {}
        fun updateProjectKeyInTextView(projectKey: String)
        fun showErrorForInvalidKey()
        fun dismissRequestProjectKeyActivity()
    }

    interface Presenter : BasePresenter {
        var secureDataManager: SecureDataManager?

        fun onScanBarcodeClicked()
        fun onActivityResultForQRScanned(potentialProjectKey: String)
        fun onEnterKeyButtonClicked(potentialProjectKey: String)
    }
}
