package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.secure.cryptography.HybridCipher

class FaceCaptureResponseEncoder(
    cipher: HybridCipher
) : ResponseEncoder(cipher) {

    override fun process(response: Step.Result?, operation: Operation): Step.Result? {
        require(response is FaceCaptureResponse)

        val capturingResult = arrayListOf<FaceCaptureResult>()
        response.capturingResult.forEach {
            it.also { item ->
                item.result?.template?.let { template ->
                    val tmpTemplateString = String(template)
                    val processedTemplate = when (operation) {
                        Operation.ENCODE -> cipher.encrypt(tmpTemplateString)
                        Operation.DECODE -> cipher.decrypt(tmpTemplateString)
                    }.toByteArray()

                    val index = item.index
                    val faceId = item.result.faceId
                    val imageRef = item.result.imageRef
                    val faceCaptureSample = FaceCaptureSample(faceId, processedTemplate, imageRef)
                    FaceCaptureResult(index, faceCaptureSample)
                }
            }
        }

        return FaceCaptureResponse(capturingResult)
    }

}
