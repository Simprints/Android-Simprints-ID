package com.simprints.id.domain.moduleapi.face

import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceMatchRequest
import java.io.Serializable

interface FaceRequestFactory {

    fun buildCaptureRequest(nFaceSamplesToCapture: Int): FaceCaptureRequest

    fun buildFaceMatchRequest(probeFaceSamples: List<FaceSample>,
                              queryForCandidates: Serializable): FaceMatchRequest
}
