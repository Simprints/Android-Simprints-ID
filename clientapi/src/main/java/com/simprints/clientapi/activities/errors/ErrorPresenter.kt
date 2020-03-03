package com.simprints.clientapi.activities.errors

import com.simprints.clientapi.R
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager

class ErrorPresenter(val view: ErrorContract.View,
                     private val sessionEventsManager: ClientApiSessionEventsManager)
    : ErrorContract.Presenter {

    override suspend fun start(clientApiAlert: ClientApiAlert) {
        sessionEventsManager.addAlertScreenEvent(clientApiAlert)
        setUpView(clientApiAlert)
    }

    private fun setUpView(clientApiAlert: ClientApiAlert) {
        view.setErrorTitleText(getErrorTitle(clientApiAlert))
        with(view) {
            setErrorMessageText(getErrorMessage(clientApiAlert))
            setBackgroundColour(getBackgroundColour(clientApiAlert))
            setErrorHintVisible(isErrorHintVisible(clientApiAlert))
        }
    }

    private fun getErrorMessage(clientApiAlert: ClientApiAlert): String =
        when (clientApiAlert) {
            ClientApiAlert.INVALID_CLIENT_REQUEST -> R.string.invalid_intentAction_message
            ClientApiAlert.INVALID_METADATA -> R.string.invalid_metadata_message
            ClientApiAlert.INVALID_MODULE_ID -> R.string.invalid_moduleId_message
            ClientApiAlert.INVALID_PROJECT_ID -> R.string.invalid_projectId_message
            ClientApiAlert.INVALID_SELECTED_ID -> R.string.invalid_selectedId_message
            ClientApiAlert.INVALID_SESSION_ID -> R.string.invalid_sessionId_message
            ClientApiAlert.INVALID_USER_ID -> R.string.invalid_userId_message
            ClientApiAlert.INVALID_VERIFY_ID -> R.string.invalid_verifyId_message
            ClientApiAlert.ROOTED_DEVICE -> R.string.rooted_device_message
        }.let {
            view.getStringFromResources(it)
        }


    private fun getErrorTitle(clientApiAlert: ClientApiAlert): String = when (clientApiAlert) {
        ClientApiAlert.ROOTED_DEVICE -> R.string.rooted_device_title
        else -> R.string.configuration_error_title
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
