package com.simprints.id.domain.moduleapi.face.responses

import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.fromModuleApiToDomain
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceCaptureResponse(
    val capturingResult: List<FaceCaptureResult>,
    override val type: FaceResponseType = FaceResponseType.CAPTURE
) : FaceResponse

fun IFaceCaptureResponse.fromModuleApiToDomain() =
    FaceCaptureResponse(capturingResult.map { it.fromModuleApiToDomain() })
