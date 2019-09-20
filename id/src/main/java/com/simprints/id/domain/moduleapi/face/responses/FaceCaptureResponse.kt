package com.simprints.id.domain.moduleapi.face.responses

import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.fromModuleApiToDomain
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceCaptureResponse(val capturingResult: List<FaceCaptureResult>) : FaceResponse

fun IFaceCaptureResponse.fromModuleApiToDomain() = FaceCaptureResponse(capturingResult.map { it.fromModuleApiToDomain() })
