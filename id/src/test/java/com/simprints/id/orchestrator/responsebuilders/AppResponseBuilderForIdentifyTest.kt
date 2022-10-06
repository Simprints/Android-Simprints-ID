package com.simprints.id.orchestrator.responsebuilders

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
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

class AppResponseBuilderForIdentifyTest {

    companion object {
        private val fingerprintConfidenceDecisionPolicy = DecisionPolicy(15, 30, 40)
        private val faceConfidenceDecisionPolicy = DecisionPolicy(15, 30, 40)
        private const val RETURN_ID_COUNT = 10
    }

    private val projectConfiguration = mockk<ProjectConfiguration> {
        every { identification } returns mockk {
            every { maxNbOfReturnedCandidates } returns RETURN_ID_COUNT
        }
        every { face } returns mockk {
            every { decisionPolicy } returns faceConfidenceDecisionPolicy
        }
        every { fingerprint } returns mockk {
            every { decisionPolicy } returns fingerprintConfidenceDecisionPolicy
        }
    }
    private val responseBuilder = AppResponseBuilderForIdentify(projectConfiguration)

    @Test
    fun withFingerprintOnlyStepsWithHighMatch_shouldBuildAppResponseBasedOnThresholds() {
        runTest {
            val modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppIdentifyResponse::class.java))
            with(response as AppIdentifyResponse) {
                assertThat(identifications.all {
                    it.confidence >= fingerprintConfidenceDecisionPolicy.high
                }).isTrue()
            }
        }
    }

    @Test
    fun withFingerprintOnlyStepsWithoutHighMatch_shouldBuildAppResponseBasedOnThresholds() {
        runTest {
            val modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT)
            val steps = mockSteps(modalities, includeHighMatch = false)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppIdentifyResponse::class.java))
            with(response as AppIdentifyResponse) {
                assertThat(identifications.all {
                    it.confidence >= fingerprintConfidenceDecisionPolicy.low
                }).isTrue()
            }
        }
    }

    @Test
    fun withFaceOnlyStepsWithHighMatch_shouldBuildAppIdentifyResponseBasedOnThresholds() {
        runTest {
            val modalities = listOf(GeneralConfiguration.Modality.FACE)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppIdentifyResponse::class.java))
            with(response as AppIdentifyResponse) {
                assertThat(identifications.all {
                    it.confidence >= faceConfidenceDecisionPolicy.high
                }).isTrue()
            }
        }
    }

    @Test
    fun withFaceOnlyStepsWithoutHighMatch_shouldBuildAppIdentifyResponseBasedOnThresholds() {
        runTest {
            val modalities = listOf(GeneralConfiguration.Modality.FACE)
            val steps = mockSteps(modalities, includeHighMatch = false)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppIdentifyResponse::class.java))
            with(response as AppIdentifyResponse) {
                assertThat(identifications.all {
                    it.confidence >= faceConfidenceDecisionPolicy.low
                }).isTrue()
            }
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

            assertThat(response, instanceOf(AppIdentifyResponse::class.java))
        }
    }

    private fun mockRequest() = AppIdentifyRequest(
        "projectId", "userId", "moduleId", "metadata"
    )


    private fun mockSteps(
        modalities: List<GeneralConfiguration.Modality>,
        includeHighMatch: Boolean = true
    ): List<Step> {
        val steps = arrayListOf<Step>()

        if (modalities.contains(GeneralConfiguration.Modality.FINGERPRINT)) {
            steps.add(mockFingerprintCaptureStep())
            steps.add(mockFingerprintMatchStep(includeHighMatch))
        }

        if (modalities.contains(GeneralConfiguration.Modality.FACE)) {
            steps.add(mockFaceCaptureStep())
            steps.add(mockFaceMatchStep(includeHighMatch))
        }

        return steps
    }
}
