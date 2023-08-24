package com.simprints.feature.clientapi.mappers

import com.simprints.feature.alert.AlertConfigurationBuilder
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

class AlertConfigurationMapper @Inject constructor() {

    fun buildAlertConfig(clientApiError: ClientApiError): AlertConfigurationBuilder = alertConfiguration {
        color = getBackgroundColor(clientApiError)
        titleRes = getTitle(clientApiError)
        image = IDR.drawable.ic_alert_default
        messageRes = getMessage(clientApiError)
        messageIcon = getMessageIcon(clientApiError)
        eventType = getEventType(clientApiError)
        leftButton = AlertButtonConfig.Close
    }

    private fun getBackgroundColor(clientApiError: ClientApiError) = when (clientApiError) {
        ClientApiError.ROOTED_DEVICE -> AlertColor.Red
        else -> AlertColor.Yellow
    }

    private fun getTitle(clientApiError: ClientApiError) = when (clientApiError) {
        ClientApiError.ROOTED_DEVICE -> IDR.string.rooted_device_title
        else -> IDR.string.configuration_error_title
    }

    private fun getMessage(clientApiError: ClientApiError) = when (clientApiError) {
        ClientApiError.INVALID_STATE_FOR_INTENT_ACTION -> IDR.string.invalid_intentAction_message
        ClientApiError.INVALID_METADATA -> IDR.string.invalid_metadata_message
        ClientApiError.INVALID_MODULE_ID -> IDR.string.invalid_moduleId_message
        ClientApiError.INVALID_PROJECT_ID -> IDR.string.invalid_projectId_message
        ClientApiError.INVALID_SELECTED_ID -> IDR.string.invalid_selectedId_message
        ClientApiError.INVALID_SESSION_ID -> IDR.string.invalid_sessionId_message
        ClientApiError.INVALID_USER_ID -> IDR.string.invalid_userId_message
        ClientApiError.INVALID_VERIFY_ID -> IDR.string.invalid_verifyId_message
        ClientApiError.ROOTED_DEVICE -> IDR.string.rooted_device_message
    }

    private fun getMessageIcon(clientApiError: ClientApiError) = when (clientApiError) {
        ClientApiError.ROOTED_DEVICE -> IDR.drawable.ic_alert_hint_cog
        else -> null
    }

    private fun getEventType(clientApiError: ClientApiError) = when (clientApiError) {
        ClientApiError.INVALID_STATE_FOR_INTENT_ACTION -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_INTENT_ACTION
        ClientApiError.INVALID_METADATA -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_METADATA
        ClientApiError.INVALID_MODULE_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_MODULE_ID
        ClientApiError.INVALID_PROJECT_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_PROJECT_ID
        ClientApiError.INVALID_SELECTED_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_SELECTED_ID
        ClientApiError.INVALID_SESSION_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_SESSION_ID
        ClientApiError.INVALID_USER_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_USER_ID
        ClientApiError.INVALID_VERIFY_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_VERIFY_ID
        ClientApiError.ROOTED_DEVICE -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.UNEXPECTED_ERROR
    }
}
