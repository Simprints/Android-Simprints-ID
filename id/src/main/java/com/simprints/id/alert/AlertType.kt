package com.simprints.id.alert

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.resources.R as IDR

enum class AlertType(
    @StringRes val title: Int,
    @StringRes val message: Int,
    val backgroundColor: AlertColor = AlertColor.Gray,
    @DrawableRes val mainDrawable: Int = IDR.drawable.ic_alert_default,
    @DrawableRes val hintDrawable: Int? = null,
) {

    DIFFERENT_PROJECT_ID(
        title = IDR.string.configuration_error_title,
        message = IDR.string.different_projectId_message,
        backgroundColor = AlertColor.Yellow,
        hintDrawable = IDR.drawable.ic_alert_hint_key,
    ),

    DIFFERENT_USER_ID(
        title = IDR.string.configuration_error_title,
        message = IDR.string.different_userId_message,
        backgroundColor = AlertColor.Yellow,
        hintDrawable = IDR.drawable.ic_alert_hint_key,
    ),

    MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP(
        title = IDR.string.missing_or_outdated_google_play_store_app_alert_title,
        message = IDR.string.missing_or_outdated_google_play_store_app_alert_message,
        backgroundColor = AlertColor.Red,
    ),

    INTEGRITY_SERVICE_ERROR(
        title = IDR.string.error_occurred_title,
        message = IDR.string.unforeseen_error_message,
        backgroundColor = AlertColor.Red,
    ),

    UNEXPECTED_ERROR(
        title = IDR.string.error_occurred_title,
        message = IDR.string.unforeseen_error_message,
        backgroundColor = AlertColor.Red,
    ),

    GOOGLE_PLAY_SERVICES_OUTDATED(
        title = IDR.string.outdated_google_play_services_alert_title,
        message = IDR.string.outdated_google_play_services_alert_message,
        backgroundColor = AlertColor.Red,
    ),

    MISSING_GOOGLE_PLAY_SERVICES(
        title = IDR.string.missing_google_play_services_alert_title,
        message = IDR.string.missing_google_play_services_alert_message,
        backgroundColor = AlertColor.Red,
    );

    fun toAlertConfig(customMessage: String? = null) = alertConfiguration {
        color = this@AlertType.backgroundColor
        titleRes = this@AlertType.title
        image = this@AlertType.mainDrawable
        message = customMessage
        messageRes = this@AlertType.message.takeIf { customMessage == null }
        messageIcon = this@AlertType.hintDrawable
        leftButton = AlertButtonConfig.Close

        eventType = this@AlertType.fromAlertToAlertTypeEvent()
        payload = bundleOf(PAYLOAD_KEY to this@AlertType.name)
    }

    companion object {

        fun fromPayload(result: AlertResult) = result.payload
            .getString(PAYLOAD_KEY)
            ?.let { AlertType.valueOf(it) }
            ?: UNEXPECTED_ERROR

        fun AlertType.fromAlertToAlertTypeEvent() = when (this) {
            DIFFERENT_PROJECT_ID -> AlertScreenEventType.DIFFERENT_PROJECT_ID
            DIFFERENT_USER_ID -> AlertScreenEventType.DIFFERENT_USER_ID
            INTEGRITY_SERVICE_ERROR -> AlertScreenEventType.INTEGRITY_SERVICE_ERROR
            UNEXPECTED_ERROR -> AlertScreenEventType.UNEXPECTED_ERROR
            GOOGLE_PLAY_SERVICES_OUTDATED -> AlertScreenEventType.GOOGLE_PLAY_SERVICES_OUTDATED
            MISSING_GOOGLE_PLAY_SERVICES -> AlertScreenEventType.MISSING_GOOGLE_PLAY_SERVICES
            MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP -> AlertScreenEventType.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP
        }

        internal const val PAYLOAD_KEY = "alert_payload"
    }
}
