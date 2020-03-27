package com.simprints.fingerprint.activities.connectalert.nfcoff

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class NfcOffTaskResult : TaskResult, Parcelable {

    companion object {
        const val BUNDLE_KEY = "NfcOffTaskResultKey"
    }
}
