package com.simprints.fingerprint.orchestrator.state

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintTaskFlowState(
    val fingerprintRequest: FingerprintRequest,
    val currentTaskIndex: Int,
    val taskResults: MutableMap<String, TaskResult>
): Parcelable
