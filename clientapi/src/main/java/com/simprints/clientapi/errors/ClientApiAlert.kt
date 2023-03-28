package com.simprints.clientapi.errors

import com.simprints.clientapi.R
import com.simprints.feature.alert.AlertConfigurationBuilder
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.resources.R as IDR

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

    fun toAlertConfig(): AlertConfigurationBuilder = alertConfiguration {
        color = getBackgroundColor()
        titleRes = getTitle()
        image = IDR.drawable.ic_alert_default
        messageRes = getMessage()
        messageIcon = getMessageIcon()
        eventType = getEventType()
        leftButton = AlertButtonConfig.Close
    }

    private fun getBackgroundColor() = when (this) {
        ROOTED_DEVICE -> AlertColor.Red
        else -> AlertColor.Yellow
    }

    private fun getTitle() = when (this) {
        ROOTED_DEVICE -> R.string.rooted_device_title
        else -> R.string.configuration_error_title
    }

    private fun getMessage() = when (this) {
        INVALID_STATE_FOR_INTENT_ACTION -> R.string.invalid_intentAction_message
        INVALID_METADATA -> R.string.invalid_metadata_message
        INVALID_MODULE_ID -> R.string.invalid_moduleId_message
        INVALID_PROJECT_ID -> R.string.invalid_projectId_message
        INVALID_SELECTED_ID -> R.string.invalid_selectedId_message
        INVALID_SESSION_ID -> R.string.invalid_sessionId_message
        INVALID_USER_ID -> R.string.invalid_userId_message
        INVALID_VERIFY_ID -> R.string.invalid_verifyId_message
        ROOTED_DEVICE -> R.string.rooted_device_message
    }

    private fun getMessageIcon() = when (this) {
        ROOTED_DEVICE -> IDR.drawable.ic_alert_hint_cog
        else -> null
    }

    private fun getEventType(): AlertScreenEventType = when (this) {
        INVALID_STATE_FOR_INTENT_ACTION -> AlertScreenEventType.INVALID_INTENT_ACTION
        INVALID_METADATA -> AlertScreenEventType.INVALID_METADATA
        INVALID_MODULE_ID -> AlertScreenEventType.INVALID_MODULE_ID
        INVALID_PROJECT_ID -> AlertScreenEventType.INVALID_PROJECT_ID
        INVALID_SELECTED_ID -> AlertScreenEventType.INVALID_SELECTED_ID
        INVALID_SESSION_ID -> AlertScreenEventType.INVALID_SESSION_ID
        INVALID_USER_ID -> AlertScreenEventType.INVALID_USER_ID
        INVALID_VERIFY_ID -> AlertScreenEventType.INVALID_VERIFY_ID
        ROOTED_DEVICE -> AlertScreenEventType.UNEXPECTED_ERROR
    }
}
