package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.secure.cryptography.HybridCipher

abstract class ResponseEncoder(protected val cipher: HybridCipher) {

    abstract fun process(response: Step.Result?, operation: Operation): Step.Result?

}
