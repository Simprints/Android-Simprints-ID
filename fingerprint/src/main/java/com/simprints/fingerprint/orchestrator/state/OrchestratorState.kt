package com.simprints.fingerprint.orchestrator.state

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class OrchestratorState(
    val fingerprintTaskFlowState: FingerprintTaskFlowState?
): Parcelable
