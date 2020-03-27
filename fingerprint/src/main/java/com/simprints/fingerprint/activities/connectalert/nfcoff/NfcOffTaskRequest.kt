package com.simprints.fingerprint.activities.connectalert.nfcoff

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class NfcOffTaskRequest : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "NfcOffTaskRequestKey"
    }
}
