package com.simprints.face.configuration.data

import android.os.Bundle
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.toArgs
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.infra.resources.R as IDR

@Keep
@ExcludedFromGeneratedTestCoverageReports("Alert screen configuration")
internal enum class ErrorType(
    @StringRes val title: Int,
    @StringRes val message: Int,
    val alertType: AlertScreenEventType,
    val errorReason: IAppErrorReason,

    var customTitle: String? = null,
    var customMessage: String? = null,
) {

    LICENSE_INVALID(
        IDR.string.error_licence_invalid_title,
        IDR.string.error_licence_invalid_message,
        alertType = AlertScreenEventType.FACE_LICENSE_INVALID,
        errorReason = IAppErrorReason.FACE_LICENSE_INVALID,
    ),
    BACKEND_MAINTENANCE_ERROR(
        IDR.string.error_backend_maintenance_title,
        IDR.string.error_backend_maintenance_message,
        alertType = AlertScreenEventType.BACKEND_MAINTENANCE_ERROR,
        errorReason = IAppErrorReason.BACKEND_MAINTENANCE_ERROR,
    ),
    CONFIGURATION_ERROR(
        IDR.string.error_configuration_error_title,
        IDR.string.error_configuration_error_message,
        alertType = AlertScreenEventType.FACE_LICENSE_MISSING,
        errorReason = IAppErrorReason.FACE_CONFIGURATION_ERROR,
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


    companion object {
        private const val PAYLOAD_TYPE_KEY = "error_type"

        fun reasonFromPayload(extras: Bundle): IAppErrorReason = extras.getString(PAYLOAD_TYPE_KEY)
            ?.let { IAppErrorReason.valueOf(it) }
            ?: IAppErrorReason.UNEXPECTED_ERROR
    }
}
