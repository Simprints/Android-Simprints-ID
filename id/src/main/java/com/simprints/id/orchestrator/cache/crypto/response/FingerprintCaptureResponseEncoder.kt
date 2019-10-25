package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.orchestrator.cache.model.FingerprintSample
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.secure.cryptography.HybridCipher

class FingerprintCaptureResponseEncoder(
    cipher: HybridCipher
) : ResponseEncoder(cipher) {

    override fun process(response: Step.Result?, operation: Operation): Step.Result? {
        require(response is FingerprintCaptureResponse)

        val captureResult = response.captureResult.mapNotNull { item ->
            item.sample?.let { sample ->
                val tmpTemplateString = String(sample.template)
                val processedTemplate = when (operation) {
                    Operation.ENCODE -> cipher.encrypt(tmpTemplateString)
                    Operation.DECODE -> cipher.decrypt(tmpTemplateString)
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
