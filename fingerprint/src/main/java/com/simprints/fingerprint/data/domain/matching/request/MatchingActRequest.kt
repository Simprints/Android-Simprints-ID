package com.simprints.fingerprint.data.domain.matching.request

import android.os.Parcelable
import com.simprints.id.domain.fingerprint.Person

interface MatchingActRequest: Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingRequestBundleKey"
    }

    val language: String
    val probe: Person
}
