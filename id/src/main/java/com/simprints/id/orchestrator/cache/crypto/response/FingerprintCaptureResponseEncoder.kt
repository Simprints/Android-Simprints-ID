package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.cache.model.Fingerprint
import com.simprints.id.orchestrator.steps.Step

class FingerprintCaptureResponseEncoder(
    keystoreManager: KeystoreManager
) : ResponseEncoder(keystoreManager) {

    override fun process(response: Step.Result?, operation: Operation): Step.Result? {
        require(response is FingerprintCaptureResponse)

        val fingerprints = response.fingerprints.map { item ->
            val tmpTemplateString = String(item.template)
            val processedTemplate = when (operation) {
                Operation.ENCODE -> keystoreManager.encryptString(tmpTemplateString)
                Operation.DECODE -> keystoreManager.decryptString(tmpTemplateString)
            }.toByteArray()

            val fingerId = item.fingerId
            val qualityScore = item.qualityScore
            Fingerprint(fingerId, processedTemplate, qualityScore)
        }

        return FingerprintCaptureResponse(fingerprints)
    }

}
