package com.simprints.fingerprint.activities.collect.request

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.data.domain.Action
import kotlinx.android.parcel.Parcelize

@Parcelize
class CollectFingerprintsTaskRequest(val projectId: String,
                                     val userId: String,
                                     val moduleId: String,
                                     val action: Action, // To know which title to show
                                     val language: String,
                                     val fingerStatus: Map<FingerIdentifier, Boolean>) : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "CollectRequestBundleKey"
    }
}
