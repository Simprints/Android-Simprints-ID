package com.simprints.id.orchestrator.cache.crypto

import com.simprints.id.orchestrator.steps.Step

interface StepEncoder {

    fun encode(step: Step): String
    fun decode(encodedStep: String): Step

}
