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

        val capturingResult = response.capturingResult.mapNotNull { item ->
            item.result?.template?.let { template ->
                val tmpTemplateString = String(template)
                val processedTemplate = when (operation) {
                    Operation.ENCODE -> keystoreManager.encryptString(tmpTemplateString)
                    Operation.DECODE -> keystoreManager.decryptString(tmpTemplateString)
                }.toByteArray()

                val index = item.index
                val faceId = item.result.faceId
                val imageRef = item.result.imageRef
                val faceCaptureSample = FaceCaptureSample(faceId, processedTemplate, imageRef)
                FaceCaptureResult(index, faceCaptureSample)
            }
        }

        return FaceCaptureResponse(capturingResult)
    }

}
