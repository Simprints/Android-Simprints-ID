package com.simprints.fingerprint.activities.launch.request

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import com.simprints.fingerprint.data.domain.Action
import kotlinx.android.parcel.Parcelize

@Parcelize
class LaunchTaskRequest(
    val language: String
) : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "LaunchRequestKey"
    }
}
