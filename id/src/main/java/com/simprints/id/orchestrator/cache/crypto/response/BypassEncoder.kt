package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.secure.cryptography.HybridEncrypter

class BypassEncoder(
    encrypter: HybridEncrypter
): ResponseEncoder(encrypter) {

    override fun process(response: Step.Result?, operation: Operation): Step.Result? = response

}
