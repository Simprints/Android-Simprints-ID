package com.simprints.id.activities.front

import com.simprints.id.data.secure.SecureDataManager

/**
 * Created by fabiotuzza on 24/01/2018.
 */
internal class FrontPresenter(val view: FrontContract.View) : FrontContract.Presenter {

    override var secureManager: SecureDataManager? = null

    init {
        view.setPresenter(this)
    }

    override fun start() {
    }

    override fun doSecurityChecks(): Boolean {
        if (secureManager == null) { throw Exception("Dependencies not injected!") }
        if (secureManager!!.getProjectSecretOrEmpty() == "") {
            view.openRequestAPIActivity()
            return false
        }
        return false
    }
}
