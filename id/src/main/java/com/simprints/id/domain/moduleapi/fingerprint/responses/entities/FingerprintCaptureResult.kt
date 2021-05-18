package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.db.subject.domain.fromModuleApiToDomain
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintCaptureResult
import kotlinx.parcelize.Parcelize

@Parcelize
data class FingerprintCaptureResult(
    val identifier: FingerIdentifier,
    val sample: FingerprintCaptureSample?
) : Parcelable

fun IFingerprintCaptureResult.fromModuleApiToDomain(): FingerprintCaptureResult =
    FingerprintCaptureResult(identifier.fromModuleApiToDomain(), sample?.fromModuleApiToDomain())
