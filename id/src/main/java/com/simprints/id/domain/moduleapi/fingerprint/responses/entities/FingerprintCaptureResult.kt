package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintCaptureResult
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintSample
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintCaptureResult(val identifier: IFingerIdentifier,
                                    val sample: IFingerprintSample?) : Parcelable

fun IFingerprintCaptureResult.fromModuleApiToDomain(): FingerprintCaptureResult =
    FingerprintCaptureResult(identifier, sample)
