package com.simprints.clientapi.activities.errors

class ErrorPresenter(val view: ErrorContract.View, val errorMessage: String)
    : ErrorContract.Presenter {

    override fun start() {
        view.setErrorMessageText(errorMessage)
    }

    override fun handleCloseClick() = view.closeActivity()

}
