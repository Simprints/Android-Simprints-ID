package com.simprints.fingerprint.activities.collect.request

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import kotlinx.android.parcel.Parcelize

@Parcelize
class CollectFingerprintsTaskRequest(val fingerprintsToCapture: List<FingerIdentifier>) : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "CollectRequestBundleKey"
    }
}
