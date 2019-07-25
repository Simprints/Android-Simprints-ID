package com.simprints.id.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.id.domain.modality.ModalityResponse

interface FingerprintResponse: Parcelable, ModalityResponse {
    val type: FingerprintTypeResponse

    companion object {
        const val BUNDLE_KEY = "FingerprintResponseBundleKey"
    }
}

enum class FingerprintTypeResponse {
    ENROL,
    IDENTIFY,
    VERIFY,
    REFUSAL,
    ERROR
}
