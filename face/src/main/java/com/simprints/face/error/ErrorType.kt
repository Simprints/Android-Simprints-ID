package com.simprints.face.error

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.simprints.face.R
import com.simprints.face.controllers.core.events.model.FaceAlertType
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.infra.resources.R as IDR

@Keep
enum class ErrorType(
    @StringRes val title: Int,
    @StringRes val message: Int,

    val backgroundColor: AlertColor = AlertColor.Gray,
    val alertType: FaceAlertType,
    var customTitle: String? = null,
    var customMessage: String? = null,
) {
    LICENSE_MISSING(
        R.string.error_licence_missing_title,
        R.string.error_licence_missing_message,
        alertType = FaceAlertType.FACE_LICENSE_MISSING,
    ),
    LICENSE_INVALID(
        R.string.error_licence_invalid_title,
        R.string.error_licence_invalid_message,
        alertType = FaceAlertType.FACE_LICENSE_INVALID,
    ),
    BACKEND_MAINTENANCE_ERROR(
        R.string.error_backend_maintenance_title,
        R.string.error_backend_maintenance_message,
        alertType = FaceAlertType.BACKEND_MAINTENANCE_ERROR,
    ),
    CONFIGURATION_ERROR(
        R.string.error_configuration_error_title,
        R.string.error_configuration_error_message,
        alertType = FaceAlertType.FACE_LICENSE_MISSING,
    ),
    UNEXPECTED_ERROR(
        R.string.error_unexpected_error_title,
        R.string.error_unexpected_error_message,
        backgroundColor = AlertColor.Red,
        alertType = FaceAlertType.UNEXPECTED_ERROR,
    ),
    ;

    fun toAlertConfiguration() = alertConfiguration {
        color = this@ErrorType.backgroundColor
        title = this@ErrorType.customTitle
        titleRes = this@ErrorType.title
        message = this@ErrorType.customMessage
        messageRes = this@ErrorType.message
        image = R.drawable.ic_exclamation_92dp
        leftButton = AlertButtonConfig(
            text = null,
            textRes = IDR.string.close,
            resultKey = AlertContract.ALERT_BUTTON_PRESSED_BACK,
            closeOnClick = true,
        )
        eventType = this@ErrorType.alertType.fromDomainToCore()
    }
}
