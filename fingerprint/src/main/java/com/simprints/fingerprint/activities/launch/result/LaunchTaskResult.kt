package com.simprints.fingerprint.activities.launch.result

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class LaunchTaskResult : TaskResult, Parcelable {

    companion object {
        const val BUNDLE_KEY = "LaunchResultKey"
    }
}
