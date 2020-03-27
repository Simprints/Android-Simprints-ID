package com.simprints.fingerprint.activities.connectalert.nfcpair

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class NfcPairTaskRequest : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "NfcPairTaskRequestKey"
    }
}
