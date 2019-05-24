package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse.Reason.*
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

fun FingerprintErrorReason.toAppErrorReason(): AppErrorResponse.Reason =
    when(this) {
        FingerprintErrorReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
        FingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
        FingerprintErrorReason.SCANNER_LOW_BATTERY -> SCANNER_LOW_BATTERY
        FingerprintErrorReason.UNKNOWN_BLUETOOTH_ISSUE -> UNKNOWN_BLUETOOTH_ISSUE
        FingerprintErrorReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
        FingerprintErrorReason.GUID_NOT_FOUND_OFFLINE -> GUID_NOT_FOUND_OFFLINE
    }
