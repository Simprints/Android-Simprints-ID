package com.simprints.moduleapi.face.requests

import com.simprints.moduleapi.face.responses.entities.IFaceSample
import java.io.Serializable

interface IFaceMatchRequest: IFaceRequest {
    val probeFaceSamples: List<IFaceSample>
    val queryForCandidates: Serializable
}
