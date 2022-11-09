package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


/**
 * This class represents an error that can occur when processing a fingerprint request
 *
 * @param reason the reason why the error occurred
 */
@Parcelize
data class FingerprintErrorResponse(val reason: FingerprintErrorReason) : FingerprintResponse {

    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.ERROR
}

/**
 * If user presses a CLOSE button, we return a FingerprintResponse.
 * If user presses BACK, an ExitForm is shown, except for UNEXPECTED_ERROR (same as CLOSE).
 */
enum class FingerprintErrorReason {
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED;

    companion object {
        fun fromFingerprintAlertToErrorResponse(fingerprintAlert: FingerprintAlert): FingerprintErrorResponse =
            when (fingerprintAlert) {
                FingerprintAlert.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                FingerprintAlert.UNEXPECTED_ERROR -> UNEXPECTED_ERROR

                //User can not leave these alerts, so Fingerprint module should not produce any error response for them.
                FingerprintAlert.BLUETOOTH_NOT_ENABLED,
                FingerprintAlert.NOT_PAIRED,
                FingerprintAlert.MULTIPLE_PAIRED_SCANNERS,
                FingerprintAlert.DISCONNECTED,
                FingerprintAlert.LOW_BATTERY -> UNEXPECTED_ERROR
            }.run {
                FingerprintErrorResponse(this)
            }
    }
}
