package com.simprints.id.domain.moduleapi.fingerprint.requests

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.fingerprint.DomainToModuleApiFingerprintRequest
import com.simprints.id.orchestrator.steps.Step.Request

interface FingerprintRequest: Parcelable, Request

fun FingerprintRequest.fromDomainToModuleApi() = DomainToModuleApiFingerprintRequest.fromDomainToModuleApiFingerprintRequest(this)
