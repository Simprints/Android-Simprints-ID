package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse.Reason.*
import com.simprints.moduleapi.fingerprint.responses.IFingerprintErrorReason
import com.simprints.moduleapi.fingerprint.responses.IFingerprintErrorResponse
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class FingerprintErrorResponse(
    val fingerprintErrorReason: FingerprintErrorReason
): FingerprintResponse {

    @IgnoredOnParcel override val type: FingerprintResponseType = FingerprintResponseType.ENROL
}

fun IFingerprintErrorResponse.fromModuleApiToDomain() =
    FingerprintErrorResponse(error.fromModuleApiToDomain())


enum class FingerprintErrorReason {
    UNEXPECTED_ERROR,
    BLUETOOTH_NOT_SUPPORTED,
    GUID_NOT_FOUND_ONLINE,
    BLUETOOTH_NO_PERMISSION,
    FINGERPRINT_CONFIGURATION_ERROR
}

fun IFingerprintErrorReason.fromModuleApiToDomain(): FingerprintErrorReason =
    when(this) {
        IFingerprintErrorReason.UNEXPECTED_ERROR -> FingerprintErrorReason.UNEXPECTED_ERROR
        IFingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED -> FingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED
        IFingerprintErrorReason.FINGERPRINT_CONFIGURATION_ERROR -> FingerprintErrorReason.FINGERPRINT_CONFIGURATION_ERROR
        IFingerprintErrorReason.BLUETOOTH_NO_PERMISSION -> FingerprintErrorReason.BLUETOOTH_NO_PERMISSION
    }

fun FingerprintErrorReason.toAppErrorReason(): AppErrorResponse.Reason =
    when(this) {
        FingerprintErrorReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
        FingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
        FingerprintErrorReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
        FingerprintErrorReason.FINGERPRINT_CONFIGURATION_ERROR -> FINGERPRINT_CONFIGURATION_ERROR
        FingerprintErrorReason.BLUETOOTH_NO_PERMISSION -> BLUETOOTH_NO_PERMISSION
    }