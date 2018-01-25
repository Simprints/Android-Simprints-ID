package com.simprints.id.activities.front


import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.data.secure.SecureDataManager

interface FrontContract {

    interface View : BaseView<Presenter> {
        fun openRequestAPIActivity()
    }

    interface Presenter : BasePresenter {
        var secureManager: SecureDataManager?
        fun doSecurityChecks(): Boolean
    }
}
