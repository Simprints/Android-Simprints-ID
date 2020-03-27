package com.simprints.fingerprint.activities.connectalert.nfcpair

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class NfcPairTaskResult : TaskResult, Parcelable {

    companion object {
        const val BUNDLE_KEY = "NfcPairTaskResultKey"
    }
}
