package com.simprints.face.error

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.simprints.face.R
import com.simprints.id.R as IDR
import com.simprints.uicomponents.R as UIR

@Keep
enum class ErrorType(
    @StringRes val title: Int,
    @StringRes val message: Int,
    @StringRes val buttonText: Int,
    @ColorRes val backgroundColor: Int,
    @DrawableRes val mainDrawable: Int,
    var errorCode: String? = null
) {
    LICENSE_MISSING(
        R.string.error_licence_missing_title,
        R.string.error_licence_missing_message,
        R.string.error_close_button,
        IDR.color.simprints_grey,
        UIR.drawable.ic_exclamation_92dp
    ),
    LICENSE_INVALID(
        R.string.error_licence_invalid_title,
        R.string.error_licence_invalid_message,
        R.string.error_close_button,
        IDR.color.simprints_grey,
        UIR.drawable.ic_exclamation_92dp
    ),
    CONFIGURATION_ERROR(
        R.string.error_configuration_error_title,
        R.string.error_configuration_error_message,
        R.string.error_close_button,
        IDR.color.simprints_grey,
        UIR.drawable.ic_exclamation_92dp
    ),
    UNEXPECTED_ERROR(
        R.string.error_unexpected_error_title,
        R.string.error_unexpected_error_message,
        R.string.error_close_button,
        IDR.color.simprints_red,
        UIR.drawable.ic_exclamation_92dp
    )
}
