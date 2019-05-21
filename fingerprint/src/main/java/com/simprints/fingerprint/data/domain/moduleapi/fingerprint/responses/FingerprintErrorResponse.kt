package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintErrorResponse(val error: FingerprintErrorType) : FingerprintResponse {

    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.ERROR
}

enum class FingerprintErrorType {
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    SCANNER_LOW_BATTERY,
    UNKNOWN_BLUETOOTH_ISSUE
}
