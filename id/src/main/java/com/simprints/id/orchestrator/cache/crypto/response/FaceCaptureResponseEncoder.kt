package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.orchestrator.steps.Step

class FaceCaptureResponseEncoder(
    keystoreManager: KeystoreManager
) : ResponseEncoder(keystoreManager) {

    override fun process(response: Step.Result?, operation: Operation): Step.Result? {
        require(response is FaceCaptureResponse)

        val capturingResult = arrayListOf<FaceCaptureResult>()
        response.capturingResult.forEach {
            it.also { item ->
                item.result?.template?.let { template ->
                    val tmpTemplateString = String(template)
                    val processedTemplate = when (operation) {
                        Operation.ENCODE -> keystoreManager.encryptString(tmpTemplateString)
                        Operation.DECODE -> keystoreManager.decryptString(tmpTemplateString)
                    }.toByteArray()

                    val faceId = item.result.faceId
                    val imageRef = item.result.imageRef
                    val faceSample = FaceCaptureSample(faceId, processedTemplate, imageRef)
                    capturingResult.add(item.copy(result = faceSample))
                }
            }
        }

        return response.copy(capturingResult = capturingResult)
    }

}
