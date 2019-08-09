package com.simprints.id.domain.moduleapi.face.requests

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.face.ModuleApiToDomainFaceRequest
import com.simprints.id.orchestrator.steps.Step.Request

interface FaceRequest: Parcelable, Request

fun FaceRequest.fromDomainToModuleApi() = ModuleApiToDomainFaceRequest.fromDomainToModuleApiFaceRequest(this)
