package com.simprints.fingerprint.activities.matching.request

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import com.simprints.fingerprint.data.domain.person.Person

interface MatchingTaskRequest : TaskRequest, Parcelable {
    companion object {
        const val BUNDLE_KEY = "MatchingRequestBundleKey"
    }

    val language: String
    val probe: Person
}
