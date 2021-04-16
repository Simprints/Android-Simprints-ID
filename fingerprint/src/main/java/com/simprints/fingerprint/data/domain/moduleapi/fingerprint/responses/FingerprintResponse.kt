package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel

interface FingerprintResponse: Parcelable {
    @IgnoredOnParcel val type: FingerprintResponseType
}

enum class FingerprintResponseType {
    CAPTURE,
    MATCH,
    CONFIGURATION,
    REFUSAL,
    ERROR
}
