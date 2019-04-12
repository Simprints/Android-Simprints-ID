package com.simprints.id.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.id.domain.modality.ModalityResponse

interface FingerprintResponse: Parcelable, ModalityResponse {
    companion object {
        const val BUNDLE_KEY = "FingerprintResponseBundleKey"
    }
}
