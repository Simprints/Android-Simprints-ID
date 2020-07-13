package com.simprints.id.domain.moduleapi.face

import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceConfigurationRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceMatchRequest
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import java.io.Serializable

interface FaceRequestFactory {

    fun buildCaptureRequest(nFaceSamplesToCapture: Int): FaceCaptureRequest

    fun buildFaceMatchRequest(probeFaceSamples: List<FaceCaptureSample>,
                              queryForCandidates: Serializable): FaceMatchRequest

    fun buildFaceConfigurationRequest(projectId: String, deviceId: String): FaceConfigurationRequest

}
