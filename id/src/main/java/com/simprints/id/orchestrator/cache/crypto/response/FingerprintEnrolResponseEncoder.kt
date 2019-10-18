package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.secure.cryptography.HybridEncrypter

class FingerprintEnrolResponseEncoder(
    encrypter: HybridEncrypter
) : ResponseEncoder(encrypter) {

    override fun process(response: Step.Result?, operation: Operation): Step.Result? {
        require(response is FingerprintEnrolResponse)

        val processedGuid = when (operation) {
            Operation.ENCODE -> encrypter.encrypt(response.guid)
            Operation.DECODE -> encrypter.decrypt(response.guid)
        }

        return response.copy(guid = processedGuid)
    }

}
