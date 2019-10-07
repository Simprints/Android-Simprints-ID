package com.simprints.id.orchestrator.cache.model

import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintCaptureResult
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintSample
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintCaptureResult(
    override val identifier: IFingerIdentifier,
    override val sample: IFingerprintSample?
) : IFingerprintCaptureResult
