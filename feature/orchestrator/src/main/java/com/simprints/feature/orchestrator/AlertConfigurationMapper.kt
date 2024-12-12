package com.simprints.feature.orchestrator

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.alert.AlertConfigurationBuilder
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.logincheck.LoginCheckError
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class AlertConfigurationMapper @Inject constructor() {
    fun buildAlertConfig(clientApiError: ClientApiError): AlertConfigurationBuilder = alertConfiguration {
        color = AlertColor.Yellow
        titleRes = IDR.string.orchestrator_configuration_error_title
        image = IDR.drawable.ic_alert_default
        messageRes = getMessage(clientApiError)
        eventType = getEventType(clientApiError)
        appErrorReason = AppErrorReason.UNEXPECTED_ERROR
        leftButton = AlertButtonConfig.Close
    }

    private fun getMessage(clientApiError: ClientApiError) = when (clientApiError) {
        ClientApiError.INVALID_STATE_FOR_INTENT_ACTION -> IDR.string.orcehstrator_invalid_action_error_message
        ClientApiError.INVALID_METADATA -> IDR.string.orchestrator_invalid_metadata_error_message
        ClientApiError.INVALID_MODULE_ID -> IDR.string.orchestrator_invalid_module_id_error_message
        ClientApiError.INVALID_PROJECT_ID -> IDR.string.orchestrator_invalid_project_id_error_message
        ClientApiError.INVALID_SELECTED_ID -> IDR.string.orchestrator_invalid_selected_id_error_message
        ClientApiError.INVALID_SESSION_ID -> IDR.string.orchestrator_invalid_session_id_error_message
        ClientApiError.INVALID_USER_ID -> IDR.string.orchestrator_invalid_user_id_error_message
        ClientApiError.INVALID_VERIFY_ID -> IDR.string.orchestrator_invalid_verify_id_error_message
    }

    fun buildAlertConfig(loginCheckError: LoginCheckError): AlertConfigurationBuilder = alertConfiguration {
        color = getBackgroundColor(loginCheckError)
        titleRes = getTitle(loginCheckError)
        image = IDR.drawable.ic_alert_default
        messageRes = getMessage(loginCheckError)
        messageIcon = getMessageIcon(loginCheckError)
        appErrorReason = getAppReason(loginCheckError)
        eventType = getEventType(loginCheckError)
        leftButton = AlertButtonConfig.Close
    }

    private fun getBackgroundColor(loginCheckError: LoginCheckError) = when (loginCheckError) {
        LoginCheckError.PROJECT_ENDING,
        LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED,
        LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES,
        LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP,
        LoginCheckError.INTEGRITY_SERVICE_ERROR,
        LoginCheckError.ROOTED_DEVICE,
        -> AlertColor.Red

        else -> AlertColor.Yellow
    }

    private fun getTitle(loginCheckError: LoginCheckError) = when (loginCheckError) {
        LoginCheckError.PROJECT_PAUSED -> IDR.string.orchestrator_project_paused_title
        LoginCheckError.PROJECT_ENDING -> IDR.string.orchestrator_project_ending_title
        LoginCheckError.ROOTED_DEVICE -> IDR.string.orchestrator_rooted_device_error_title
        LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES -> IDR.string.orchestrator_missing_google_play_services_alert_title
        LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED -> IDR.string.orchestrator_outdated_google_play_services_alert_title
        LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP -> IDR.string.orchestrator_missing_or_outdated_google_play_store_app_alert_title
        LoginCheckError.INTEGRITY_SERVICE_ERROR -> IDR.string.orchestrator_generic_error_title
        LoginCheckError.UNEXPECTED_LOGIN_ERROR -> IDR.string.orchestrator_generic_error_title
        LoginCheckError.DIFFERENT_PROJECT_ID -> IDR.string.orchestrator_configuration_error_title
    }

    private fun getMessage(loginCheckError: LoginCheckError) = when (loginCheckError) {
        LoginCheckError.DIFFERENT_PROJECT_ID -> IDR.string.orchestrator_project_id_error_message
        LoginCheckError.PROJECT_PAUSED -> IDR.string.orchestrator_project_paused_body
        LoginCheckError.PROJECT_ENDING -> IDR.string.orchestrator_project_ending_body
        LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES -> IDR.string.orchestrator_missing_google_play_services_alert_message
        LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED -> IDR.string.orchestrator_outdated_google_play_services_alert_message
        LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP -> IDR.string.orchestrator_missing_or_outdated_google_play_store_app_alert_message
        LoginCheckError.INTEGRITY_SERVICE_ERROR -> IDR.string.orchestrator_unexpected_error_message
        LoginCheckError.UNEXPECTED_LOGIN_ERROR -> IDR.string.orchestrator_unexpected_error_message
        LoginCheckError.ROOTED_DEVICE -> IDR.string.orcehstrator_rooted_device_error_message
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
        LoginCheckError.ROOTED_DEVICE,
        -> AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.UNEXPECTED_ERROR
    }

    private fun getAppReason(loginCheckError: LoginCheckError) = when (loginCheckError) {
        LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES,
        LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED,
        LoginCheckError.INTEGRITY_SERVICE_ERROR,
        LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP,
        LoginCheckError.UNEXPECTED_LOGIN_ERROR,
        -> AppErrorReason.UNEXPECTED_ERROR

        LoginCheckError.DIFFERENT_PROJECT_ID -> AppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN
        LoginCheckError.PROJECT_PAUSED -> AppErrorReason.PROJECT_PAUSED
        LoginCheckError.PROJECT_ENDING -> AppErrorReason.PROJECT_ENDING
        LoginCheckError.ROOTED_DEVICE -> AppErrorReason.ROOTED_DEVICE
    }
}
