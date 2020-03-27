package com.simprints.fingerprint.activities.connectalert.turnonscanner

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class TurnOnScannerTaskResult : TaskResult, Parcelable {

    companion object {
        const val BUNDLE_KEY = "BluetoothOffTaskResultKey"
    }
}
