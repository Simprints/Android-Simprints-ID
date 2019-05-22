package com.simprints.clientapi.activities.errors

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView


interface ErrorContract {

    interface View : BaseView<Presenter> {

        fun setErrorMessageText(message: String)

        fun closeActivity()
        fun getStringFromResources(res: Int): String
    }

    interface Presenter : BasePresenter {

        fun start(clientApiAlert: ClientApiAlert)
        fun handleCloseClick()
    }

}
