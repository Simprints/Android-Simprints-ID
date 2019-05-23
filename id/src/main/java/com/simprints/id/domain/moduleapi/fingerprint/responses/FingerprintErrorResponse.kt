package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.app.responses.AppErrorReason
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintErrorResponse(val fingerprintErrorReason: FingerprintErrorReason): FingerprintResponse {

    @IgnoredOnParcel override val type: FingerprintTypeResponse = FingerprintTypeResponse.ENROL
}

enum class FingerprintErrorReason {
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    SCANNER_LOW_BATTERY,
    UNKNOWN_BLUETOOTH_ISSUE,
    GUID_NOT_FOUND_ONLINE,
    GUID_NOT_FOUND_OFFLINE
}

fun FingerprintErrorReason.toAppErrorReason(): AppErrorReason =
    when(this) {
        FingerprintErrorReason.UNEXPECTED_ERROR -> AppErrorReason.UNEXPECTED_ERROR
        FingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED -> AppErrorReason.BLUETOOTH_NOT_SUPPORTED
        FingerprintErrorReason.SCANNER_LOW_BATTERY -> AppErrorReason.SCANNER_LOW_BATTERY
        FingerprintErrorReason.UNKNOWN_BLUETOOTH_ISSUE -> AppErrorReason.UNKNOWN_BLUETOOTH_ISSUE
        FingerprintErrorReason.GUID_NOT_FOUND_ONLINE -> AppErrorReason.GUID_NOT_FOUND_ONLINE
        FingerprintErrorReason.GUID_NOT_FOUND_OFFLINE -> AppErrorReason.GUID_NOT_FOUND_OFFLINE
    }
