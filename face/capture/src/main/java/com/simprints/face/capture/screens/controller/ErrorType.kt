package com.simprints.face.capture.screens.controller

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.toArgs
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.resources.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@Keep
@ExcludedFromGeneratedTestCoverageReports("Alert screen configuration")
internal enum class ErrorType(
    @StringRes val title: Int,
    @StringRes val message: Int,
    val alertType: AlertScreenEvent.AlertScreenPayload.AlertScreenEventType,
    val errorReason: AppErrorReason,

) {
    LICENSE_INVALID(
        R.string.configuration_licence_invalid_title,
        R.string.configuration_licence_invalid_message,
        alertType = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.FACE_LICENSE_INVALID,
        errorReason = AppErrorReason.FACE_LICENSE_INVALID,
    ),
    ;

    fun toAlertArgs() = alertConfiguration {
        color = AlertColor.Gray
        titleRes = this@ErrorType.title
        messageRes = this@ErrorType.message
        image = R.drawable.ic_exclamation
        leftButton = AlertButtonConfig.Close
        payload = bundleOf(PAYLOAD_TYPE_KEY to this@ErrorType.errorReason)
        eventType = this@ErrorType.alertType
    }.toArgs()
    companion object {
        private const val PAYLOAD_TYPE_KEY = "error_type"
    }
}
