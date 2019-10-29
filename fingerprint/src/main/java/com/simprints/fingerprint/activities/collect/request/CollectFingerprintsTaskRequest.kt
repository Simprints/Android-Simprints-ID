package com.simprints.fingerprint.activities.collect.request

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import kotlinx.android.parcel.Parcelize

@Parcelize
class CollectFingerprintsTaskRequest(val projectId: String,
                                     val userId: String,
                                     val moduleId: String,
                                     val fingerprintsToCapture: List<FingerIdentifier>) : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "CollectRequestBundleKey"
    }
}
