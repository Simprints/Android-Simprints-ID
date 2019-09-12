package com.simprints.id.orchestrator.cache

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
        converter.recycle()
        return String(converter.toBytes())
    }

    override fun decode(encodedStep: String?): Step? {
        return encodedStep?.let {
            val converter = ParcelableConverter(it.toByteArray())
            val parcel = converter.getParcel()
            converter.recycle()
            val stepWithEncodedResult = Step.createFromParcel(parcel)
            processStep(stepWithEncodedResult, Operation.DECODE)
        }
    }

    private fun processStep(step: Step, operation: Operation): Step {
        val result = step.result
        return step.also {
            it.result = when (result) {
                is FaceCaptureResponse -> handleFaceCaptureResponse(result, operation)
                is FingerprintEnrolResponse -> handleFingerprintEnrolResponse(result, operation)
                else -> result
            }
        }
    }

    private fun handleFaceCaptureResponse(
        response: FaceCaptureResponse,
        operation: Operation
    ): FaceCaptureResponse {
        val capturingResult = response.capturingResult.map {
            it.apply {
                result?.template?.let { template ->
                    val tmpTemplateString = String(template)
                    val processedTemplate = when (operation) {
                        Operation.ENCODE -> keystoreManager.encryptString(tmpTemplateString)
                        Operation.DECODE -> keystoreManager.decryptString(tmpTemplateString)
                    }.toByteArray()

                    val faceSample = result.copy(template = processedTemplate)
                    copy(result = faceSample)
                }
            }
        }

        return response.copy(capturingResult = capturingResult)
    }

    private fun handleFingerprintEnrolResponse(
        response: FingerprintEnrolResponse,
        operation: Operation
    ): FingerprintEnrolResponse {
        val processedGuid = when (operation) {
            Operation.ENCODE -> keystoreManager.encryptString(response.guid)
            Operation.DECODE -> keystoreManager.decryptString(response.guid)
        }

        return response.copy(guid = processedGuid)
    }

    private enum class Operation {
        ENCODE,
        DECODE
    }

}
