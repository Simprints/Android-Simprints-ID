package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests

import android.os.Parcelable
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier

interface FingerprintRequest : Parcelable {
    companion object {
        const val BUNDLE_KEY = "FingerprintRequest"
    }

    val language: String
    val fingerStatus: Map<FingerIdentifier, Boolean>
}
