package com.simprints.fingerprint.orchestrator.state

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.Orchestrator
import kotlinx.parcelize.Parcelize

/**
 * This class represents the state of the orchestrator which controls the flow of tasks being executed
 * @see Orchestrator
 *
 * @property fingerprintTaskFlowState  the state of the flow of tasks to be executed
 */
@Parcelize
class OrchestratorState(
    val fingerprintTaskFlowState: FingerprintTaskFlowState
): Parcelable
