package com.simprints.id.orchestrator.responsebuilders

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.orchestrator.steps.Step
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

@ExperimentalCoroutinesApi
class AppResponseBuilderForIdentifyTest {

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
        private const val RETURN_ID_COUNT = 10
    }

    private val responseBuilder =
        AppResponseBuilderForIdentify(fingerprintConfidenceThresholds, faceConfidenceThresholds, RETURN_ID_COUNT)

    @Test
    fun withFingerprintOnlyStepsWithHighMatch_shouldBuildAppResponseBasedOnThresholds() {
        runBlockingTest {
            val modalities = listOf(FINGER)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppIdentifyResponse::class.java))
            with(response as AppIdentifyResponse) {
                assertThat(identifications.all {
                    it.confidence >= fingerprintConfidenceThresholds.getValue(FingerprintConfidenceThresholds.HIGH)
                }).isTrue()
            }
        }
    }

    @Test
    fun withFingerprintOnlyStepsWithoutHighMatch_shouldBuildAppResponseBasedOnThresholds() {
        runBlockingTest {
            val modalities = listOf(FINGER)
            val steps = mockSteps(modalities, includeHighMatch = false)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppIdentifyResponse::class.java))
            with(response as AppIdentifyResponse) {
                assertThat(identifications.all {
                    it.confidence >= fingerprintConfidenceThresholds.getValue(FingerprintConfidenceThresholds.LOW)
                }).isTrue()
            }
        }
    }

    @Test
    fun withFaceOnlyStepsWithHighMatch_shouldBuildAppIdentifyResponseBasedOnThresholds() {
        runBlockingTest {
            val modalities = listOf(FACE)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppIdentifyResponse::class.java))
            with(response as AppIdentifyResponse) {
                assertThat(identifications.all {
                    it.confidence >= faceConfidenceThresholds.getValue(FaceConfidenceThresholds.HIGH)
                }).isTrue()
            }
        }
    }

    @Test
    fun withFaceOnlyStepsWithoutHighMatch_shouldBuildAppIdentifyResponseBasedOnThresholds() {
        runBlockingTest {
            val modalities = listOf(FACE)
            val steps = mockSteps(modalities, includeHighMatch = false)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppIdentifyResponse::class.java))
            with(response as AppIdentifyResponse) {
                assertThat(identifications.all {
                    it.confidence >= faceConfidenceThresholds.getValue(FaceConfidenceThresholds.LOW)
                }).isTrue()
            }
        }
    }

    @Test
    fun withFingerprintAndFaceSteps_shouldBuildAppIdentifyResponse() {
        runBlockingTest {
            val modalities = listOf(FINGER, FACE)
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


    private fun mockSteps(modalities: List<Modality>, includeHighMatch: Boolean = true): List<Step> {
        val steps = arrayListOf<Step>()

        if (modalities.contains(FINGER)) {
            steps.add(mockFingerprintCaptureStep())
            steps.add(mockFingerprintMatchStep(includeHighMatch))
        }

        if (modalities.contains(FACE)) {
            steps.add(mockFaceCaptureStep())
            steps.add(mockFaceMatchStep(includeHighMatch))
        }

        return steps
    }
}
