package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import com.simprints.fingerprint.activities.alert.AlertError
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
        fun fromFingerprintAlertToErrorResponse(fingerprintAlert: AlertError): FingerprintErrorResponse =
            when (fingerprintAlert) {
                AlertError.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                AlertError.UNEXPECTED_ERROR -> UNEXPECTED_ERROR

                //User can not leave these alerts, so Fingerprint module should not produce any error response for them.
                AlertError.BLUETOOTH_NOT_ENABLED,
                AlertError.NOT_PAIRED,
                AlertError.MULTIPLE_PAIRED_SCANNERS,
                AlertError.DISCONNECTED,
                AlertError.LOW_BATTERY -> UNEXPECTED_ERROR
            }.run {
                FingerprintErrorResponse(this)
            }
    }
}
