package com.simprints.fingerprint.activities.collect.result

import android.os.Parcelable
import com.simprints.fingerprint.activities.ActResult
import com.simprints.fingerprint.data.domain.person.Person
import kotlinx.android.parcel.Parcelize

@Parcelize
class CollectFingerprintsActResult(val probe: Person): ActResult, Parcelable {
    companion object {
        const val BUNDLE_KEY = "CollectResultBundleKey"
    }
}

