package com.simprints.fingerprint.moduleapi

import com.simprints.fingerprint.data.domain.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.requests.FingerprintVerifyRequest
import com.simprints.moduleapi.fingerprint.IFingerprintEnrolRequest
import com.simprints.moduleapi.fingerprint.IFingerprintIdentifyRequest
import com.simprints.moduleapi.fingerprint.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.IFingerprintVerifyRequest
import java.security.InvalidParameterException

object AppAdapter {

    fun toDomainFingerprintRequest(iFingerprintEnrolRequest: IFingerprintRequest): FingerprintRequest =
        when(iFingerprintEnrolRequest) {
            is IFingerprintEnrolRequest -> FingerprintEnrolRequest(iFingerprintEnrolRequest)
            is IFingerprintVerifyRequest -> FingerprintVerifyRequest(iFingerprintEnrolRequest)
            is IFingerprintIdentifyRequest -> FingerprintIdentifyRequest(iFingerprintEnrolRequest)
            else -> throw InvalidParameterException("Invalid Fingerprint Request") //StopShip
        }
}
