package com.simprints.fingerprint.activities.matching.request

import android.os.Parcelable
import com.simprints.fingerprint.activities.ActRequest
import com.simprints.fingerprint.data.domain.person.Person

interface MatchingActRequest : ActRequest, Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingRequestBundleKey"
    }

    val language: String
    val probe: Person
}
