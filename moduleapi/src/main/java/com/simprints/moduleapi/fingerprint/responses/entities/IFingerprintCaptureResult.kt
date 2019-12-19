package com.simprints.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintSample

interface IFingerprintCaptureResult : Parcelable {
    val identifier: IFingerIdentifier
    val sample: IFingerprintSample?
}
