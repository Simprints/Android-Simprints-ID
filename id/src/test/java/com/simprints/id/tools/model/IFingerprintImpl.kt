package com.simprints.id.tools.model

import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.responses.IFingerprint
import kotlinx.android.parcel.Parcelize

@Parcelize
class IFingerprintImpl(override val fingerId: IFingerIdentifier,
                       override val template: ByteArray,
                       override val qualityScore: Int) : IFingerprint
