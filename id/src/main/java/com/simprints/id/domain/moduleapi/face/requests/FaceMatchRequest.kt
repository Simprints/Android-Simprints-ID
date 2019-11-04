package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.face.responses.entities.fromDomainToModuleApi
import com.simprints.moduleapi.face.requests.IFaceMatchRequest
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class FaceMatchRequest (
    val probeFaceSamples: List<FaceCaptureSample>,
    val queryForCandidates: Serializable,
    override val type: FaceRequestType = FaceRequestType.MATCH
) : FaceRequest

fun FaceMatchRequest.fromDomainToModuleApi(): IFaceMatchRequest =
    IFaceMatchRequestImpl(
        probeFaceSamples.map { it.fromDomainToModuleApi() },
        queryForCandidates
    )

@Parcelize
private data class IFaceMatchRequestImpl(
    override val probeFaceSamples: List<IFaceSample>,
    override val queryForCandidates: Serializable
) : IFaceMatchRequest
