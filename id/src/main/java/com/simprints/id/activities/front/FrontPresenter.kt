package com.simprints.id.activities.front

import com.simprints.id.data.secure.SecureDataManager

internal class FrontPresenter(val view: FrontContract.View, override val secureManager: SecureDataManager) : FrontContract.Presenter {

    init {
        view.setPresenter(this)
    }

    override fun start() {
    }

    override fun doSecurityChecks(): Boolean {
        if (secureManager.getProjectSecretOrEmpty() == "") {
            view.openRequestAPIActivity()
            return false
        }
        return false
    }
}
