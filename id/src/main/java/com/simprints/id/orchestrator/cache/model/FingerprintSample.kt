package com.simprints.id.orchestrator.cache.model

import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintSample
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintSample(
    override val id: String,
    override val fingerIdentifier: IFingerIdentifier,
    override val imageRef: ISecuredImageRef?,
    override val qualityScore: Int,
    override val template: ByteArray
) : IFingerprintSample
