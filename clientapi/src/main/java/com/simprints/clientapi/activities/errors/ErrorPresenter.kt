package com.simprints.clientapi.activities.errors

import com.simprints.clientapi.R
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.extensions.doInBackground
import com.simprints.id.R as Rid

class ErrorPresenter(val view: ErrorContract.View,
                     private val sessionEventsManager: ClientApiSessionEventsManager)
    : ErrorContract.Presenter {

    override fun start(clientApiAlert: ClientApiAlert) {
        sessionEventsManager.addAlertScreenEvent(clientApiAlert).doInBackground()
        setUpView(clientApiAlert)
    }

    private fun setUpView(clientApiAlert: ClientApiAlert) {
        with(view) {
            setErrorTitleText(getErrorTitle(clientApiAlert))
            setErrorMessageText(getErrorMessage(clientApiAlert))
            setBackgroundColour(getBackgroundColour(clientApiAlert))
            setErrorHintVisible(isErrorHintVisible(clientApiAlert))
        }
    }

    private fun getErrorTitle(clientApiAlert: ClientApiAlert): String = when (clientApiAlert) {
        ClientApiAlert.ROOTED_DEVICE -> R.string.rooted_device_title
        else -> R.string.configuration_error_title
    }.let(view::getStringFromResources)

    private fun getErrorMessage(clientApiAlert: ClientApiAlert): String = when (clientApiAlert) {
        ClientApiAlert.INVALID_CLIENT_REQUEST -> Rid.string.invalid_intentAction_message
        ClientApiAlert.INVALID_METADATA -> Rid.string.invalid_metadata_message
        ClientApiAlert.INVALID_MODULE_ID -> Rid.string.invalid_moduleId_message
        ClientApiAlert.INVALID_PROJECT_ID -> Rid.string.invalid_projectId_message
        ClientApiAlert.INVALID_SELECTED_ID -> Rid.string.invalid_selectedId_message
        ClientApiAlert.INVALID_SESSION_ID -> Rid.string.invalid_sessionId_message
        ClientApiAlert.INVALID_USER_ID -> Rid.string.invalid_userId_message
        ClientApiAlert.INVALID_VERIFY_ID -> Rid.string.invalid_verifyId_message
        ClientApiAlert.ROOTED_DEVICE -> R.string.rooted_device_message
    }.let(view::getStringFromResources)

    private fun getBackgroundColour(clientApiAlert: ClientApiAlert): Int = when (clientApiAlert) {
        ClientApiAlert.ROOTED_DEVICE -> R.color.alert_red
        else -> R.color.alert_yellow
    }.let(view::getColourFromResources)

    private fun isErrorHintVisible(clientApiAlert: ClientApiAlert): Boolean {
        return clientApiAlert != ClientApiAlert.ROOTED_DEVICE
    }

    override suspend fun start() {}

    override fun handleCloseOrBackClick() = view.closeActivity()

}
