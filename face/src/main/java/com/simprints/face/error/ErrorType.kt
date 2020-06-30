package com.simprints.face.error

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.simprints.face.R
import com.simprints.id.R as IDR
import com.simprints.uicomponents.R as UIR

@Keep
sealed class ErrorType(
    @StringRes val title: Int,
    @StringRes val message: Int,
    @StringRes val buttonText: Int,
    @ColorRes val backgroundColor: Int,
    @DrawableRes val mainDrawable: Int
) {
    object LicenseMissing : ErrorType(
        R.string.error_licence_missing_title,
        R.string.error_licence_missing_message,
        R.string.error_licence_missing_button,
        IDR.color.simprints_grey,
        UIR.drawable.ic_exclamation_92dp
    )

    object LicenseInvalid : ErrorType(
        R.string.error_licence_invalid_title,
        R.string.error_licence_invalid_message,
        R.string.error_licence_invalid_button,
        IDR.color.simprints_grey,
        UIR.drawable.ic_exclamation_92dp
    )
}
