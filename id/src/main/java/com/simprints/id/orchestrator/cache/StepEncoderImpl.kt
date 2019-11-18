package com.simprints.id.orchestrator.cache

import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.ParcelableConverter

class StepEncoderImpl : StepEncoder {

    companion object {
        private val charsetsForStepToByteArray = Charsets.ISO_8859_1
    }
    override fun encode(step: Step): String {
        val stepByteArray = ParcelableConverter.marshall(step)
        return stepByteArray.toString(charsetsForStepToByteArray)
    }

    override fun decode(encodedStep: String): Step {
        val stepByteArray = encodedStep.toByteArray(charsetsForStepToByteArray)
        return ParcelableConverter.unmarshall(stepByteArray, Step.CREATOR)
    }
}
