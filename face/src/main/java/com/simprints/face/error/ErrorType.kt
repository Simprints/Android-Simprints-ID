package com.simprints.face.error

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.simprints.face.R
import com.simprints.infra.resources.R as CR

@Keep
enum class ErrorType(
    @StringRes val title: Int,
    @StringRes val message: Int,
    @StringRes val buttonText: Int,
    @ColorRes val backgroundColor: Int,
    @DrawableRes val mainDrawable: Int,
    var errorCode: String? = null,
    var estimatedOutage: Long? = null
) {
    LICENSE_MISSING(
        R.string.error_licence_missing_title,
        R.string.error_licence_missing_message,
        R.string.error_close_button,
        CR.color.simprints_grey,
        R.drawable.ic_exclamation_92dp
    ),
    LICENSE_INVALID(
        R.string.error_licence_invalid_title,
        R.string.error_licence_invalid_message,
        R.string.error_close_button,
        CR.color.simprints_grey,
        R.drawable.ic_exclamation_92dp
    ),
    BACKEND_MAINTENANCE_ERROR(
        R.string.error_backend_maintenance_title,
        R.string.error_backend_maintenance_message,
        R.string.error_close_button,
        CR.color.simprints_grey,
        R.drawable.ic_exclamation_92dp
    ),
    CONFIGURATION_ERROR(
        R.string.error_configuration_error_title,
        R.string.error_configuration_error_message,
        R.string.error_close_button,
        CR.color.simprints_grey,
        R.drawable.ic_exclamation_92dp
    ),
    UNEXPECTED_ERROR(
        R.string.error_unexpected_error_title,
        R.string.error_unexpected_error_message,
        R.string.error_close_button,
        CR.color.simprints_red,
        R.drawable.ic_exclamation_92dp
    )
}
