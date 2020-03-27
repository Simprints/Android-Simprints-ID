package com.simprints.fingerprint.activities.connectalert.turnonscanner

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class TurnOnScannerTaskRequest : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "NfcOffTaskRequestKey"
    }
}
