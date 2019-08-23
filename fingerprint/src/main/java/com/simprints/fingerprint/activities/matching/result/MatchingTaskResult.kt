package com.simprints.fingerprint.activities.matching.result

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskResult

interface MatchingTaskResult : TaskResult, Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingResultBundleKey"
    }
}
