package com.simprints.id.orchestrator.cache.crypto.response

import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.orchestrator.steps.Step

class BypassEncoder(
    keystoreManager: KeystoreManager
): ResponseEncoder(keystoreManager) {

    override fun process(response: Step.Result?, operation: Operation): Step.Result? = response

}
