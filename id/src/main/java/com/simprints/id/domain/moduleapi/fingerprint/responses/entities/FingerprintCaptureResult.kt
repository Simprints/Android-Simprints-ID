package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.fingerprint.models.fromModuleApiToDomain
import com.simprints.infra.config.domain.models.Finger
import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintCaptureResult
import kotlinx.parcelize.Parcelize

@Parcelize
data class FingerprintCaptureResult(
    val identifier: Finger,
    val sample: FingerprintCaptureSample?
) : Parcelable

fun IFingerprintCaptureResult.fromModuleApiToDomain(): FingerprintCaptureResult =
    FingerprintCaptureResult(identifier.fromModuleApiToDomain(), sample?.fromModuleApiToDomain())
