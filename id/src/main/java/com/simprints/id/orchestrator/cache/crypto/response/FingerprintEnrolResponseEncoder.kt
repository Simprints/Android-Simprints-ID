package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.secure.cryptography.HybridCipher

class FingerprintEnrolResponseEncoder(
    cipher: HybridCipher
) : ResponseEncoder(cipher) {

    override fun process(response: Step.Result?, operation: Operation): Step.Result? {
        require(response is FingerprintEnrolResponse)

        val processedGuid = when (operation) {
            Operation.ENCODE -> cipher.encrypt(response.guid)
            Operation.DECODE -> cipher.decrypt(response.guid)
        }

        return response.copy(guid = processedGuid)
    }

}
