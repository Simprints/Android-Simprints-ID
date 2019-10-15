package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.orchestrator.cache.model.FingerprintSample
import com.simprints.id.orchestrator.steps.Step

class FingerprintCaptureResponseEncoder(
    keystoreManager: KeystoreManager
) : ResponseEncoder(keystoreManager) {

    override fun process(response: Step.Result?, operation: Operation): Step.Result? {
        require(response is FingerprintCaptureResponse)

        val captureResult = response.captureResult.mapNotNull { item ->
            item.sample?.let { sample ->
                val tmpTemplateString = String(sample.template)
                val processedTemplate = when (operation) {
                    Operation.ENCODE -> keystoreManager.encryptString(tmpTemplateString)
                    Operation.DECODE -> keystoreManager.decryptString(tmpTemplateString)
                }.toByteArray()

                val id = sample.id
                val fingerId = sample.fingerIdentifier
                val imageRef = sample.imageRef
                val qualityScore = sample.qualityScore

                val processedSample = FingerprintSample(id, fingerId, qualityScore,
                    processedTemplate, imageRef)

                FingerprintCaptureResult(fingerId, processedSample)
            }
        }

        return FingerprintCaptureResponse(captureResult = captureResult)
    }

}
