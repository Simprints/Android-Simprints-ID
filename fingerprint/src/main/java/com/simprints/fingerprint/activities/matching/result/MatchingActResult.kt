package com.simprints.fingerprint.activities.matching.result

import android.os.Parcelable

interface MatchingActResult: Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingResultBundleKey"
    }
}
