package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.orchestrator.steps.Step

class FingerprintEnrolResponseEncoder(
    keystoreManager: KeystoreManager
) : ResponseEncoder(keystoreManager) {

    override fun process(response: Step.Result, operation: Operation): Step.Result {
        require(response is FingerprintEnrolResponse)

        val processedGuid = when (operation) {
            Operation.ENCODE -> keystoreManager.encryptString(response.guid)
            Operation.DECODE -> keystoreManager.decryptString(response.guid)
        }

        return response.copy(guid = processedGuid)
    }

}
