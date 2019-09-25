package com.simprints.id.orchestrator.cache.crypto.step

import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.orchestrator.cache.crypto.response.BypassEncoder
import com.simprints.id.orchestrator.cache.crypto.response.FaceCaptureResponseEncoder
import com.simprints.id.orchestrator.cache.crypto.response.FingerprintEnrolResponseEncoder
import com.simprints.id.orchestrator.cache.crypto.response.Operation
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.ParcelableConverter

class StepEncoderImpl(private val keystoreManager: KeystoreManager) : StepEncoder {

    override fun encode(step: Step): String {
        val stepCopy = step.copy()
        val stepWithEncodedResult = processStep(stepCopy, Operation.ENCODE)
        val converter = ParcelableConverter(stepWithEncodedResult)
        val encodedString = String(converter.toBytes())
        converter.recycle()
        return encodedString
    }

    override fun decode(encodedStep: String): Step {
        val converter = ParcelableConverter(encodedStep.toByteArray())
        val parcel = converter.toParcel()
        val stepWithEncodedResult = Step.createFromParcel(parcel)
        converter.recycle()
        return processStep(stepWithEncodedResult, Operation.DECODE)
    }

    private fun processStep(step: Step, operation: Operation): Step {
        val result = step.result
        return step.also {
            val responseEncoder = when (result) {
                is FingerprintEnrolResponse -> FingerprintEnrolResponseEncoder(keystoreManager)
                is FaceCaptureResponse -> FaceCaptureResponseEncoder(keystoreManager)
                else -> BypassEncoder(keystoreManager)
            }
            it.result = responseEncoder.process(result, operation)
        }
    }

}
