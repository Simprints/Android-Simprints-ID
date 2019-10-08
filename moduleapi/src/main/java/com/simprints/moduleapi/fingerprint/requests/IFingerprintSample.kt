package com.simprints.moduleapi.fingerprint.requests

import com.simprints.moduleapi.common.ISecuredImageRef

interface IFingerprintSample {
    val id: String
    val fingerIdentifier: IFingerIdentifier
    val template: ByteArray
    val templateQualityScore: Int
    val imageRef: ISecuredImageRef?
}
