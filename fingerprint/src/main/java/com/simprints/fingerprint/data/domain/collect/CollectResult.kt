package com.simprints.fingerprint.data.domain.collect

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.person.Person
import kotlinx.android.parcel.Parcelize

@Parcelize
class CollectResult(val probe: Person): Parcelable {
    companion object {
        const val BUNDLE_KEY = "CollectResultBundleKey"
    }
}

