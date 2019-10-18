package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.secure.cryptography.HybridEncrypter

abstract class ResponseEncoder(protected val encrypter: HybridEncrypter) {

    abstract fun process(response: Step.Result?, operation: Operation): Step.Result?

}
