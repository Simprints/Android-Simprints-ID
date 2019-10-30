package com.simprints.id.orchestrator.cache

import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.ParcelableConverter

class StepEncoderImpl : StepEncoder {

    override fun encode(step: Step): String {
        val stepByteArray = ParcelableConverter.marshall(step)
        return stepByteArray.toString(Charsets.ISO_8859_1)
    }

    override fun decode(encodedStep: String): Step {
        val stepByteArray = encodedStep.toByteArray(Charsets.ISO_8859_1)
        return ParcelableConverter.unmarshall(stepByteArray, Step.CREATOR)
    }
}
