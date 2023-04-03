package com.simprints.clientapi.errors

import com.simprints.feature.alert.AlertConfigurationBuilder
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.resources.R

enum class ClientApiAlert {
    INVALID_STATE_FOR_INTENT_ACTION,
    INVALID_METADATA,
    INVALID_MODULE_ID,
    INVALID_PROJECT_ID,
    INVALID_SELECTED_ID,
    INVALID_SESSION_ID,
    INVALID_USER_ID,
    INVALID_VERIFY_ID,
    ROOTED_DEVICE,
    ;

    companion object {

        fun ClientApiAlert.toAlertConfig(): AlertConfigurationBuilder = alertConfiguration {
            color = getBackgroundColor(this@toAlertConfig)
            titleRes = getTitle(this@toAlertConfig)
            image = R.drawable.ic_alert_default
            messageRes = getMessage(this@toAlertConfig)
            messageIcon = getMessageIcon(this@toAlertConfig)
            eventType = getEventType(this@toAlertConfig)
            leftButton = AlertButtonConfig.Close
        }

        private fun getBackgroundColor(clientApiAlert: ClientApiAlert) = when (clientApiAlert) {
            ROOTED_DEVICE -> AlertColor.Red
            else -> AlertColor.Yellow
        }

        private fun getTitle(clientApiAlert: ClientApiAlert) = when (clientApiAlert) {
            ROOTED_DEVICE -> com.simprints.clientapi.R.string.rooted_device_title
            else -> com.simprints.clientapi.R.string.configuration_error_title
        }

        private fun getMessage(clientApiAlert: ClientApiAlert) = when (clientApiAlert) {
            INVALID_STATE_FOR_INTENT_ACTION -> com.simprints.clientapi.R.string.invalid_intentAction_message
            INVALID_METADATA -> com.simprints.clientapi.R.string.invalid_metadata_message
            INVALID_MODULE_ID -> com.simprints.clientapi.R.string.invalid_moduleId_message
            INVALID_PROJECT_ID -> com.simprints.clientapi.R.string.invalid_projectId_message
            INVALID_SELECTED_ID -> com.simprints.clientapi.R.string.invalid_selectedId_message
            INVALID_SESSION_ID -> com.simprints.clientapi.R.string.invalid_sessionId_message
            INVALID_USER_ID -> com.simprints.clientapi.R.string.invalid_userId_message
            INVALID_VERIFY_ID -> com.simprints.clientapi.R.string.invalid_verifyId_message
            ROOTED_DEVICE -> com.simprints.clientapi.R.string.rooted_device_message
        }

        private fun getMessageIcon(clientApiAlert: ClientApiAlert) = when (clientApiAlert) {
            ROOTED_DEVICE -> R.drawable.ic_alert_hint_cog
            else -> null
        }

        private fun getEventType(clientApiAlert: ClientApiAlert) = when (clientApiAlert) {
            INVALID_STATE_FOR_INTENT_ACTION -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_INTENT_ACTION
            INVALID_METADATA -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_METADATA
            INVALID_MODULE_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_MODULE_ID
            INVALID_PROJECT_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_PROJECT_ID
            INVALID_SELECTED_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_SELECTED_ID
            INVALID_SESSION_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_SESSION_ID
            INVALID_USER_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_USER_ID
            INVALID_VERIFY_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.INVALID_VERIFY_ID
            ROOTED_DEVICE -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.UNEXPECTED_ERROR
        }
    }
}
