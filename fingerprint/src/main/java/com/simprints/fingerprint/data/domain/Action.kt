package com.simprints.fingerprint.data.domain

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class Action : Parcelable {
    ENROL, IDENTIFY, VERIFY
}

fun FingerprintRequest.toAction() = when (this) {
    is FingerprintEnrolRequest -> Action.ENROL
    is FingerprintIdentifyRequest -> Action.IDENTIFY
    is FingerprintVerifyRequest -> Action.VERIFY
    else -> throw Throwable("Woops") // TODO
}
