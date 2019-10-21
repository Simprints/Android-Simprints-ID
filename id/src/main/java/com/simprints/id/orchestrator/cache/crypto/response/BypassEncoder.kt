package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.secure.cryptography.HybridCipher

class BypassEncoder(
    cipher: HybridCipher
): ResponseEncoder(cipher) {

    override fun process(response: Step.Result?, operation: Operation): Step.Result? = response

}
