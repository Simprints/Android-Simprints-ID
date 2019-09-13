package com.simprints.id.orchestrator.cache.crypto

import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
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

    override fun decode(encodedStep: String?): Step? {
        return encodedStep?.let {
            val converter = ParcelableConverter(it.toByteArray())
            val parcel = converter.getParcel()
            val stepWithEncodedResult = Step.createFromParcel(parcel)
            converter.recycle()
            processStep(stepWithEncodedResult, Operation.DECODE)
        }
    }

    private fun processStep(step: Step, operation: Operation): Step {
        val result = step.result
        return step.also {
            val responseProcessor = when (result) {
                is FaceCaptureResponse -> FaceCaptureResponseEncoder(keystoreManager)
                is FingerprintEnrolResponse -> FingerprintEnrolResponseEncoder(keystoreManager)
                else -> null
            }
            it.result = responseProcessor?.process(result, operation)
        }
    }

}
