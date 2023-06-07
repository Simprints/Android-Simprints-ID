package com.simprints.feature.login.screens.qrscanner

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
internal class QrScannerResult(
    val content: String?,
    val error: QrScannerError?,
) : Parcelable {

    @Keep
    enum class QrScannerError {
        NoPermission,
        CameraNotAvailable,
        UnknownError,
        ;
    }
}

