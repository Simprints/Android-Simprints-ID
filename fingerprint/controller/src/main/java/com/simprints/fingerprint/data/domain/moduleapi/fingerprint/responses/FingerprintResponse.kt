package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import kotlinx.parcelize.IgnoredOnParcel

/**
 * This interface represents the a response that can be returned when processing a fingerprint request
 * @see FingerprintRequest
 */
interface FingerprintResponse: Parcelable {
    @IgnoredOnParcel val type: FingerprintResponseType
}

// The enum class representing the different types of responses
enum class FingerprintResponseType {
    CAPTURE,
    MATCH,
    CONFIGURATION,
    REFUSAL,
    ERROR
}
