package com.simprints.fingerprint.orchestrator.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class OrchestratorState(
    val fingerprintTaskFlowState: FingerprintTaskFlowState
): Parcelable
