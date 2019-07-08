package com.simprints.fingerprint.activities.refusal.request

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class RefusalTaskRequest : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "RefusalResultKey"
    }
}
