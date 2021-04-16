package com.simprints.fingerprint.activities.connect.result

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.parcelize.Parcelize

@Parcelize
class ConnectScannerTaskResult : TaskResult, Parcelable {

    companion object {
        const val BUNDLE_KEY = "ConnectScannerResultKey"
    }
}
