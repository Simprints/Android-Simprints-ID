package com.simprints.fingerprint.activities.connect.request

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class ConnectScannerTaskRequest : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "ConnectScannerRequestKey"
    }
}
