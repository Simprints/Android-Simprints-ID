package com.simprints.fingerprint.data.domain.matching.result

import android.os.Parcelable

interface MatchingResult: Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingResultBundleKey"
    }
}
