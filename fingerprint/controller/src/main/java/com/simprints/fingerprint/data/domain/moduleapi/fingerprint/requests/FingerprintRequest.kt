package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests

import android.os.Parcelable

interface FingerprintRequest : Parcelable {
    companion object {
        const val BUNDLE_KEY = "FingerprintRequest"
    }
}
