package com.simprints.moduleapi.fingerprint.requests

import android.os.Parcelable
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

interface IFingerprintSample: Parcelable {
    val id: String
    val fingerIdentifier: IFingerIdentifier
    val template: ByteArray
    val templateQualityScore: Int
    val imageRef: ISecuredImageRef?
}
