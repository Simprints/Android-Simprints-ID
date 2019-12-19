package com.simprints.fingerprint.activities.matching.result

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.matching.MatchResult
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingTaskResult(val results: List<MatchResult>) : TaskResult, Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingResultBundleKey"
    }
}
