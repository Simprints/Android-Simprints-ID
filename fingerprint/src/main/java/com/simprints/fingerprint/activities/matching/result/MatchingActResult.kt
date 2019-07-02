package com.simprints.fingerprint.activities.matching.result

import android.os.Parcelable
import com.simprints.fingerprint.activities.ActResult

interface MatchingActResult : ActResult, Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingResultBundleKey"
    }
}
