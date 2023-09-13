package com.simprints.id.orchestrator.responsebuilders

import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppRefusalFormResponse
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.exceptions.unexpected.MissingCaptureResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.DecisionPolicy
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
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
    fun withRefusalStep_shouldBuildAppExitFormResponse() {
        runTest {
            val modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT)
            val steps = listOf(mockCoreExitFormResponse())

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppRefusalFormResponse::class.java))
        }
    }

    @Test
    fun noCaptureResponse_shouldThrow() {
        runTest {
            val modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT)
            val steps = listOf<Step>()

            assertThrows<MissingCaptureResponse> {
                responseBuilder.buildAppResponse(
                    modalities, mockRequest(), steps, "sessionId"
                )
            }
        }
    }

    @Test
    fun withFingerprintOnlySteps_shouldBuildAppVerifyResponse() {
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
    fun withFaceOnlySteps_shouldBuildAppVerifyResponse() {
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
    fun withFingerprintHigherConfidenceAndFaceSteps_shouldBuildAppVerifyResponseWithFingerprint() {
        runTest {
            val modalities = listOf(
                GeneralConfiguration.Modality.FINGERPRINT,
                GeneralConfiguration.Modality.FACE
            )
            val steps = arrayListOf<Step>()
            steps.add(mockFingerprintCaptureStep())
            val fingerprintMatchStep = mockFingerprintMatchStep(true)
            steps.add(fingerprintMatchStep)
            steps.add(mockFaceCaptureStep())
            steps.add(mockFaceMatchStep(false))

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppVerifyResponse::class.java))
            val actualGuid = (response as? AppVerifyResponse)?.matchingResult?.guidFound
            val expectedGuid = (fingerprintMatchStep.getResult() as? FingerprintMatchResponse)?.result?.first()?.personId
            assertThat(actualGuid, (equalTo(expectedGuid)))
        }
    }

    @Test
    fun withFaceHigherConfidenceAndFingerprintSteps_shouldBuildAppVerifyResponseWithFace() {
        runTest {
            val modalities = listOf(
                GeneralConfiguration.Modality.FINGERPRINT,
                GeneralConfiguration.Modality.FACE
            )
            val steps = arrayListOf<Step>()
            steps.add(mockFingerprintCaptureStep())
            steps.add(mockFingerprintMatchStep(false))
            steps.add(mockFaceCaptureStep())
            val faceMatchStep = mockFaceMatchStep(true)
            steps.add(faceMatchStep)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppVerifyResponse::class.java))
            val actualGuid = (response as? AppVerifyResponse)?.matchingResult?.guidFound
            val expectedGuid = (faceMatchStep.getResult() as? FaceMatchResponse)?.result?.first()?.guidFound
            assertThat(actualGuid, (equalTo(expectedGuid)))
        }
    }

    private fun mockRequest() = AppVerifyRequest(
        projectId = "projectId",
        userId = "userId".asTokenizedRaw(),
        moduleId = "moduleId".asTokenizedRaw(),
        metadata = "metadata",
        verifyGuid = "verifyGuid"
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
