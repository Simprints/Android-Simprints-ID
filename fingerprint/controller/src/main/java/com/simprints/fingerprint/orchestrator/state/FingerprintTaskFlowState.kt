package com.simprints.fingerprint.orchestrator.state

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.parcelize.Parcelize

/**
 * This class represents the state of a current task's flow being executed.
 *
 * @property fingerprintRequest  the fingerprint request that was triggered for this flow
 * @property currentTaskIndex  the index of the currently running task within the task-flow list of tasks
 * @property taskResults  the list of results from the corresponding list of tasks that complete the flow
 */
@Parcelize
class FingerprintTaskFlowState(
    val fingerprintRequest: FingerprintRequest,
    val currentTaskIndex: Int,
    val taskResults: MutableMap<String, TaskResult>
): Parcelable
