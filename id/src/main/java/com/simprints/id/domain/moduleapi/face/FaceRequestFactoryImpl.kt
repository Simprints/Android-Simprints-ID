package com.simprints.id.domain.moduleapi.face

import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceMatchRequest
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import java.io.Serializable

class FaceRequestFactoryImpl: FaceRequestFactory {

    override fun buildCaptureRequest(nFaceSamplesToCapture: Int) = FaceCaptureRequest(nFaceSamplesToCapture = nFaceSamplesToCapture)

    override fun buildFaceMatchRequest(
        probeFaceSamples: List<FaceCaptureSample>,
        queryForCandidates: Serializable
    ): FaceMatchRequest = FaceMatchRequest(probeFaceSamples, queryForCandidates)
}
