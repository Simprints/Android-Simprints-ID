package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel

interface FingerprintResponse: Parcelable {
    @IgnoredOnParcel val type: FingerprintResponseType
}

enum class FingerprintResponseType {
    CAPTURE,
    @Deprecated("To be replaced with CAPTURE") ENROL,
    VERIFY,
    IDENTIFY,
    REFUSAL,
    ERROR
}
