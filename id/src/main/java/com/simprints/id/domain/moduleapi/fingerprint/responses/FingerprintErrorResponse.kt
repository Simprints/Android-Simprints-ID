package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse.Reason.*
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintErrorResponse(
    val fingerprintErrorReason: FingerprintErrorReason
): FingerprintResponse {

    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.ENROL
}

enum class FingerprintErrorReason {
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    GUID_NOT_FOUND_ONLINE
}

fun FingerprintErrorReason.toAppErrorReason(): AppErrorResponse.Reason =
    when(this) {
        FingerprintErrorReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
        FingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
        FingerprintErrorReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
    }
