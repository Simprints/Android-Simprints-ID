package com.simprints.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

interface IFingerprintSample : Parcelable {
    val id: String
    val fingerIdentifier: IFingerIdentifier
    val template: ByteArray
    val qualityScore: Int
    val imageRef: ISecuredImageRef?
}
