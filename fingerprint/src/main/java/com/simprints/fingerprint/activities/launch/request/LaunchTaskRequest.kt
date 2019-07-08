package com.simprints.fingerprint.activities.launch.request

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import com.simprints.fingerprint.data.domain.Action
import kotlinx.android.parcel.Parcelize

@Parcelize
class LaunchTaskRequest(
    val projectId: String,
    val action: Action, // To know which version of the consent text to show
    val language: String,
    val logoExists: Boolean,
    val programName: String,
    val organizationName: String,
    val verifyGuid: String? = null
) : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "LaunchRequestKey"
    }
}
