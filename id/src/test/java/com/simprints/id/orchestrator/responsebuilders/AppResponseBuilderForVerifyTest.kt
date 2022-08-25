package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.GeneralConfiguration
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class AppResponseBuilderForVerifyTest {

    companion object {
        private val fingerprintConfidenceThresholds = mapOf(
            FingerprintConfidenceThresholds.LOW to 15,
            FingerprintConfidenceThresholds.MEDIUM to 30,
            FingerprintConfidenceThresholds.HIGH to 40
        )
        private val faceConfidenceThresholds = mapOf(
            FaceConfidenceThresholds.LOW to 15,
            FaceConfidenceThresholds.MEDIUM to 30,
            FaceConfidenceThresholds.HIGH to 40
        )
    }

    private val responseBuilder =
        AppResponseBuilderForVerify(fingerprintConfidenceThresholds, faceConfidenceThresholds)

    @Test
    fun withFingerprintOnlySteps_shouldBuildAppResponse() {
        runTest {
            val modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppVerifyResponse::class.java))
        }
    }

    @Test
    fun withFaceOnlySteps_shouldBuildAppIdentifyResponse() {
        runTest {
            val modalities = listOf(GeneralConfiguration.Modality.FACE)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppVerifyResponse::class.java))
        }
    }

    @Test
    fun withFingerprintAndFaceSteps_shouldBuildAppIdentifyResponse() {
        runTest {
            val modalities = listOf(
                GeneralConfiguration.Modality.FINGERPRINT,
                GeneralConfiguration.Modality.FACE
            )
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppVerifyResponse::class.java))
        }
    }

    private fun mockRequest() = AppVerifyRequest(
        "projectId", "userId", "moduleId", "metadata", "verifyGuid"
    )

    private fun mockSteps(modalities: List<GeneralConfiguration.Modality>): List<Step> {
        val steps = arrayListOf<Step>()

        if (modalities.contains(GeneralConfiguration.Modality.FINGERPRINT)) {
            steps.add(mockFingerprintCaptureStep())
            steps.add(mockFingerprintMatchStep())
        }

        if (modalities.contains(GeneralConfiguration.Modality.FACE)) {
            steps.add(mockFaceCaptureStep())
            steps.add(mockFaceMatchStep())
        }

        return steps
    }
}
