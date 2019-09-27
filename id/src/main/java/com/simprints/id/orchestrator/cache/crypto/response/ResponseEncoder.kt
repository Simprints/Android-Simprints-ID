package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.orchestrator.steps.Step

abstract class ResponseEncoder(protected val keystoreManager: KeystoreManager) {

    abstract fun process(response: Step.Result?, operation: Operation): Step.Result?

}
