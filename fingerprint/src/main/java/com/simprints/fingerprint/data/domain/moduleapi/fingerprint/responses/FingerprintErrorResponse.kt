package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintErrorResponse(val reason: FingerprintErrorReason) : FingerprintResponse {

    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.ERROR
}

/**
 * If user presses a CLOSE button, we return a FingerprintResponse.
 * If user presses BACK, an ExitForm is shown, except for UNEXPECTED_ERROR and GUID_NOT_FOUND_ONLINE (same as CLOSE).
 */
enum class FingerprintErrorReason {
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE,
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    SCANNER_LOW_BATTERY,
    UNKNOWN_BLUETOOTH_ISSUE;

    companion object {
        fun fromFingerprintAlertToErrorResponse(fingerprintAlert: FingerprintAlert): FingerprintErrorResponse =
            when (fingerprintAlert) {
                FingerprintAlert.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                FingerprintAlert.GUID_NOT_FOUND_OFFLINE -> GUID_NOT_FOUND_OFFLINE
                FingerprintAlert.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                FingerprintAlert.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                FingerprintAlert.LOW_BATTERY -> SCANNER_LOW_BATTERY
                else -> UNEXPECTED_ERROR
            }.run {
                FingerprintErrorResponse(this)
            }
    }
}
