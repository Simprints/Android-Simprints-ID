package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.DecisionPolicy
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class AppResponseBuilderForVerifyTest {

    companion object {
        private val fingerprintConfidenceDecisionPolicy = DecisionPolicy(15, 30, 40)
        private val faceConfidenceDecisionPolicy = DecisionPolicy(15, 30, 40)
    }

    private val projectConfiguration = mockk<ProjectConfiguration> {
        every { face } returns mockk {
            every { decisionPolicy } returns faceConfidenceDecisionPolicy
        }
        every { fingerprint } returns mockk {
            every { decisionPolicy } returns fingerprintConfidenceDecisionPolicy
        }
    }
    private val responseBuilder =
        AppResponseBuilderForVerify(projectConfiguration)

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
