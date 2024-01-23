package com.simprints.feature.setup.data

import android.os.Bundle
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.toArgs
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.resources.R as IDR

@Keep
@ExcludedFromGeneratedTestCoverageReports("Alert screen configuration")
internal enum class ErrorType(
    @StringRes val title: Int,
    @StringRes val message: Int,
    val alertType: AlertScreenEventType,
    val errorReason: AppErrorReason,

    var customTitle: String? = null,
    var customMessage: String? = null,
) {


    BACKEND_MAINTENANCE_ERROR(
        IDR.string.configuration_backend_maintenance_title,
        IDR.string.configuration_backend_maintenance_message,
        alertType = AlertScreenEventType.BACKEND_MAINTENANCE_ERROR,
        errorReason = AppErrorReason.BACKEND_MAINTENANCE_ERROR,
    ),
    CONFIGURATION_ERROR(
        IDR.string.configuration_generic_error_title,
        IDR.string.configuration_generic_error_message,
        alertType = AlertScreenEventType.FACE_LICENSE_MISSING,
        errorReason = AppErrorReason.FACE_CONFIGURATION_ERROR,
    ),
    ;

    fun toAlertArgs() = alertConfiguration {
        color = AlertColor.Gray
        title = this@ErrorType.customTitle
        titleRes = this@ErrorType.title
        message = this@ErrorType.customMessage
        messageRes = this@ErrorType.message
        image = IDR.drawable.ic_exclamation
        leftButton = AlertButtonConfig.Close
        payload = bundleOf(PAYLOAD_TYPE_KEY to this@ErrorType.errorReason)
        eventType = this@ErrorType.alertType
    }.toArgs()


    @ExcludedFromGeneratedTestCoverageReports("Inner code of excluded file")
    companion object {
        private const val PAYLOAD_TYPE_KEY = "error_type"

        fun reasonFromPayload(extras: Bundle): AppErrorReason = extras.getString(PAYLOAD_TYPE_KEY)
            ?.let { AppErrorReason.valueOf(it) }
            ?: AppErrorReason.UNEXPECTED_ERROR
    }
}
