package com.simprints.id.domain.moduleapi.fingerprint.requests

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.face.DomainToModuleApiFaceRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.fingerprint.DomainToModuleApiFingerprintRequest
import com.simprints.id.orchestrator.steps.Step.Request

interface FingerprintRequest: Parcelable, Request

fun Request.fromDomainToModuleApi(): Parcelable =
    when {
        this is FingerprintRequest -> DomainToModuleApiFingerprintRequest.fromDomainToModuleApiFingerprintRequest(this)
        this is FaceRequest -> DomainToModuleApiFaceRequest.fromDomainToModuleApiFaceRequest(this)
        else -> throw Throwable("Invalid Request")
    }
