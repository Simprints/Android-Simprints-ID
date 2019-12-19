package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.person.domain.fromModuleApiToDomain
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintCaptureResult
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FingerprintCaptureResult(
    val identifier: FingerIdentifier,
    val sample: FingerprintCaptureSample?
) : Parcelable

fun IFingerprintCaptureResult.fromModuleApiToDomain(): FingerprintCaptureResult =
    FingerprintCaptureResult(identifier.fromModuleApiToDomain(), sample?.fromModuleApiToDomain())
