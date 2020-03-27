package com.simprints.fingerprint.activities.connectalert.bluetoothoff

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class BluetoothOffTaskRequest : TaskRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "BluetoothOffTaskRequestKey"
    }
}
