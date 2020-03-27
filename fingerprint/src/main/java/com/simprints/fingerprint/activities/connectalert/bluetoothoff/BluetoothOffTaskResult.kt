package com.simprints.fingerprint.activities.connectalert.bluetoothoff

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class BluetoothOffTaskResult : TaskResult, Parcelable {

    companion object {
        const val BUNDLE_KEY = "BluetoothOffTaskResultKey"
    }
}
