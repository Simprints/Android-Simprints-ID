package com.simprints.clientapi.activities.errors

import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager

class ErrorPresenter(val view: ErrorContract.View,
                     private val clientApiSessionEventsManager: ClientApiSessionEventsManager,
                     private val clientApiCrashReportManager: ClientApiCrashReportManager)
    : ErrorContract.Presenter {

    override fun start() {
        val errorMessage = view.getErrorMessage()
        view.setErrorMessageText(errorMessage)
    }

    override fun handleCloseClick() = view.closeActivity()

}
