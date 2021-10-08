package com.simprints.id.domain.moduleapi.face.responses

import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.domain.moduleapi.face.responses.entities.fromModuleApiToDomain
import com.simprints.moduleapi.face.responses.IFaceMatchResponse
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceMatchResponse(val result: List<FaceMatchResult>) : FaceResponse {
    @IgnoredOnParcel
    override val type: FaceResponseType = FaceResponseType.MATCH
}

fun IFaceMatchResponse.fromModuleApiToDomain() = FaceMatchResponse(result.map { it.fromModuleApiToDomain() })
