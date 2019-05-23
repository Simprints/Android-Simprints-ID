package com.simprints.clientapi.activities.errors

import com.simprints.id.R as Rid
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.AlertScreenEvent
import com.simprints.clientapi.tools.ClientApiTimeHelper

class ErrorPresenter(val view: ErrorContract.View,
                     private val clientApiTimeHelper: ClientApiTimeHelper,
                     private val clientApiSessionEventsManager: ClientApiSessionEventsManager)
    : ErrorContract.Presenter {

    override fun start(clientApiAlert: ClientApiAlert) {

        clientApiSessionEventsManager.addSessionEvent(AlertScreenEvent(clientApiTimeHelper.now(), clientApiAlert))

        val errorMessage = getErrorMessage(clientApiAlert)
        view.setErrorMessageText(errorMessage)
    }

    private fun getErrorMessage(clientApiAlert: ClientApiAlert): String =
        when(clientApiAlert) {
            ClientApiAlert.INVALID_CLIENT_REQUEST -> Rid.string.invalid_intentAction_message
            ClientApiAlert.INVALID_METADATA -> Rid.string.invalid_metadata_message
            ClientApiAlert.INVALID_MODULE_ID -> Rid.string.invalid_moduleId_message
            ClientApiAlert.INVALID_PROJECT_ID -> Rid.string.invalid_projectId_message
            ClientApiAlert.INVALID_SELECTED_ID -> Rid.string.invalid_selectedId_message
            ClientApiAlert.INVALID_SESSION_ID -> Rid.string.invalid_sessionId_message
            ClientApiAlert.INVALID_USER_ID -> Rid.string.invalid_userId_message
            ClientApiAlert.INVALID_VERIFY_ID -> Rid.string.invalid_verifyId_message
        }.let {
            view.getStringFromResources(it)
        }

    override fun start() {}

    override fun handleCloseClick() = view.closeActivity()
}
