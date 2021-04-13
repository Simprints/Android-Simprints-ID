package com.simprints.fingerprint.activities.connect.request

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import kotlinx.parcelize.Parcelize

@Parcelize
class ConnectScannerTaskRequest(val connectMode: ConnectMode) : TaskRequest, Parcelable {

    enum class ConnectMode {
        INITIAL_CONNECT,
        RECONNECT
    }

    companion object {
        const val BUNDLE_KEY = "ConnectScannerRequestKey"
    }
}
