package com.simprints.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

interface IFingerprint: Parcelable {
    val fingerId: IFingerIdentifier
    val template: ByteArray
    val qualityScore: Int
}
