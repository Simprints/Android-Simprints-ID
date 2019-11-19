package com.simprints.fingerprint.activities.matching.request

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
class MatchingTaskRequest(val probeFingerprintSamples: List<Fingerprint>,
                          val queryForCandidates: Serializable) : TaskRequest, Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingRequestBundleKey"
    }
}
