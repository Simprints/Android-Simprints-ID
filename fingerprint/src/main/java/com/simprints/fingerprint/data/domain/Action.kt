package com.simprints.fingerprint.data.domain

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForFingerprintException
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class Action(val activityTitle: String) : Parcelable {
    IDENTIFY("Identification"),
    VERIFY("Verification")
}

fun FingerprintRequest.toAction() = when (this) {
    is FingerprintIdentifyRequest -> Action.IDENTIFY
    is FingerprintVerifyRequest -> Action.VERIFY
    else -> throw InvalidRequestForFingerprintException("Could not find Action for FingerprintRequest")
}
