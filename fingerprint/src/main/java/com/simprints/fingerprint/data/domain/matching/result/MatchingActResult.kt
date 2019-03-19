package com.simprints.fingerprint.data.domain.matching.result

import android.os.Parcelable

interface MatchingActResult: Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingResultBundleKey"
    }
}
