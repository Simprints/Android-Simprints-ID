package com.simprints.fingerprint.data.domain.matching.request

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.person.Person
import kotlinx.android.parcel.Parcelize

interface MatchingActRequest: Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingRequestBundleKey"
    }

    val language: String
    val probe: Person
}
