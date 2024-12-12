package com.simprints.feature.login.screens.qrscanner

import androidx.annotation.Keep
import java.io.Serializable

@Keep
internal class QrScannerResult(
    val content: String?,
    val error: QrScannerError?,
) : Serializable {
    @Keep
    enum class QrScannerError {
        NoPermission,
        CameraNotAvailable,
        UnknownError,
    }
}
