package com.simprints.moduleapi.fingerprint

import android.os.Parcelable
import com.simprints.moduleapi.common.ISecuredImageRef

interface IFingerprintSample: Parcelable {
    val fingerIdentifier: IFingerIdentifier
    val template: ByteArray
    val templateQualityScore: Int
    val imageRef: ISecuredImageRef?
}
