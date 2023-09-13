package com.simprints.feature.orchestrator

import android.os.Bundle
import androidx.core.os.bundleOf
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.alert.AlertConfigurationBuilder
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.logincheck.LoginCheckError
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.moduleapi.app.responses.IAppErrorReason
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI code")
class AlertConfigurationMapper @Inject constructor() {

    fun buildAlertConfig(clientApiError: ClientApiError): AlertConfigurationBuilder = alertConfiguration {
        color = AlertColor.Yellow
        titleRes = IDR.string.configuration_error_title
        image = IDR.drawable.ic_alert_default
        messageRes = getMessage(clientApiError)
        eventType = getEventType(clientApiError)
        leftButton = AlertButtonConfig.Close

        payload = bundleOf(
            PAYLOAD_TYPE_KEY to ClientApiError::name,
            PAYLOAD_KEY to clientApiError.name,
        )
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
    }


    fun buildAlertConfig(loginCheckError: LoginCheckError): AlertConfigurationBuilder = alertConfiguration {
        color = getBackgroundColor(loginCheckError)
        titleRes = getTitle(loginCheckError)
        image = IDR.drawable.ic_alert_default
        messageRes = getMessage(loginCheckError)
        messageIcon = getMessageIcon(loginCheckError)
        eventType = getEventType(loginCheckError)
        leftButton = AlertButtonConfig.Close

        payload = bundleOf(
            PAYLOAD_TYPE_KEY to LoginCheckError::name.name,
            PAYLOAD_KEY to loginCheckError.name,
        )
    }

    private fun getBackgroundColor(loginCheckError: LoginCheckError) = when (loginCheckError) {
        LoginCheckError.PROJECT_ENDING,
        LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED,
        LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES,
        LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP,
        LoginCheckError.INTEGRITY_SERVICE_ERROR,
        LoginCheckError.ROOTED_DEVICE -> AlertColor.Red

        else -> AlertColor.Yellow
    }

    private fun getTitle(loginCheckError: LoginCheckError) = when (loginCheckError) {
        LoginCheckError.PROJECT_PAUSED -> IDR.string.project_paused_title
        LoginCheckError.PROJECT_ENDING -> IDR.string.project_ending_title
        LoginCheckError.ROOTED_DEVICE -> IDR.string.rooted_device_title
        LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES -> IDR.string.missing_google_play_services_alert_title
        LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED -> IDR.string.outdated_google_play_services_alert_title
        LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP -> IDR.string.missing_or_outdated_google_play_store_app_alert_title
        LoginCheckError.INTEGRITY_SERVICE_ERROR -> IDR.string.error_occurred_title
        LoginCheckError.UNEXPECTED_LOGIN_ERROR -> IDR.string.error_occurred_title
        LoginCheckError.DIFFERENT_PROJECT_ID -> IDR.string.configuration_error_title
    }

    private fun getMessage(loginCheckError: LoginCheckError) = when (loginCheckError) {
        LoginCheckError.DIFFERENT_PROJECT_ID -> IDR.string.different_projectId_message
        LoginCheckError.PROJECT_PAUSED -> IDR.string.project_paused_body
        LoginCheckError.PROJECT_ENDING -> IDR.string.project_ending_body
        LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES -> IDR.string.missing_google_play_services_alert_message
        LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED -> IDR.string.outdated_google_play_services_alert_message
        LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP -> IDR.string.missing_or_outdated_google_play_store_app_alert_message
        LoginCheckError.INTEGRITY_SERVICE_ERROR -> IDR.string.unforeseen_error_message
        LoginCheckError.UNEXPECTED_LOGIN_ERROR -> IDR.string.unforeseen_error_message
        LoginCheckError.ROOTED_DEVICE -> IDR.string.rooted_device_message
    }

    private fun getMessageIcon(clientApiError: LoginCheckError) = when (clientApiError) {
        LoginCheckError.DIFFERENT_PROJECT_ID -> IDR.drawable.ic_alert_hint_key
        LoginCheckError.ROOTED_DEVICE -> IDR.drawable.ic_alert_hint_cog
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
    }


    private fun getEventType(loginCheckError: LoginCheckError) = when (loginCheckError) {
        LoginCheckError.DIFFERENT_PROJECT_ID -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.DIFFERENT_PROJECT_ID
        LoginCheckError.PROJECT_PAUSED -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.PROJECT_PAUSED
        LoginCheckError.PROJECT_ENDING -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.PROJECT_ENDING
        LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.MISSING_GOOGLE_PLAY_SERVICES
        LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.GOOGLE_PLAY_SERVICES_OUTDATED
        LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP
        LoginCheckError.INTEGRITY_SERVICE_ERROR,
        LoginCheckError.UNEXPECTED_LOGIN_ERROR,
        LoginCheckError.ROOTED_DEVICE -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.UNEXPECTED_ERROR
    }


    companion object {

        fun reasonFromPayload(extras: Bundle): IAppErrorReason {
            val type = extras.getString(PAYLOAD_TYPE_KEY) ?: return IAppErrorReason.UNEXPECTED_ERROR
            val payload = extras.getString(PAYLOAD_KEY) ?: return IAppErrorReason.UNEXPECTED_ERROR

            return when (type) {
                ClientApiError::name.name -> IAppErrorReason.UNEXPECTED_ERROR
                LoginCheckError::name.name -> when (LoginCheckError.valueOf(payload)) {
                    LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES,
                    LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED,
                    LoginCheckError.INTEGRITY_SERVICE_ERROR,
                    LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP,
                    LoginCheckError.UNEXPECTED_LOGIN_ERROR,
                    -> IAppErrorReason.UNEXPECTED_ERROR

                    LoginCheckError.DIFFERENT_PROJECT_ID -> IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN
                    LoginCheckError.PROJECT_PAUSED -> IAppErrorReason.PROJECT_PAUSED
                    LoginCheckError.PROJECT_ENDING -> IAppErrorReason.PROJECT_ENDING
                    LoginCheckError.ROOTED_DEVICE -> IAppErrorReason.ROOTED_DEVICE
                }

                else -> IAppErrorReason.UNEXPECTED_ERROR
            }
        }

        private const val PAYLOAD_TYPE_KEY = "alert_payload_type"
        private const val PAYLOAD_KEY = "alert_payload"
    }
}

