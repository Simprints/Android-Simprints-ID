package com.simprints.id.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.id.domain.modal.ModalResponse

interface FingerprintResponse: Parcelable, ModalResponse {
    companion object {
        const val BUNDLE_KEY = "FingerprintResponseBundleKey"
    }
}
