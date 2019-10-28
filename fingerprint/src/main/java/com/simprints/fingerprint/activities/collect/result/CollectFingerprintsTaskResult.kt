package com.simprints.fingerprint.activities.collect.result

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class CollectFingerprintsTaskResult(val fingerprints: List<Fingerprint>): TaskResult, Parcelable {
    companion object {
        const val BUNDLE_KEY = "CollectResultBundleKey"
    }
}

