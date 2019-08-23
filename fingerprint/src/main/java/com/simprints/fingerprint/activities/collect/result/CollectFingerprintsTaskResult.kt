package com.simprints.fingerprint.activities.collect.result

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.fingerprint.data.domain.person.Person
import kotlinx.android.parcel.Parcelize

@Parcelize
class CollectFingerprintsTaskResult(val probe: Person): TaskResult, Parcelable {
    companion object {
        const val BUNDLE_KEY = "CollectResultBundleKey"
    }
}

