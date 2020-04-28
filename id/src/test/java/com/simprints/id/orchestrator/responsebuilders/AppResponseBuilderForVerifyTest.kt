package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.orchestrator.steps.Step
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

@ExperimentalCoroutinesApi
class AppResponseBuilderForVerifyTest {

    private val responseBuilder = AppResponseBuilderForVerify()

    @Test
    fun withFingerprintOnlySteps_shouldBuildAppResponse() {
        runBlockingTest {
            val modalities = listOf(FINGER)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppVerifyResponse::class.java))
        }
    }

    @Test
    fun withFaceOnlySteps_shouldBuildAppIdentifyResponse() {
        runBlockingTest {
            val modalities = listOf(FACE)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppVerifyResponse::class.java))
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

            assertThat(response, instanceOf(AppVerifyResponse::class.java))
        }
    }

    private fun mockRequest() = AppVerifyRequest(
        "projectId", "userId", "moduleId", "metadata", "verifyGuid"
    )

    private fun mockSteps(modalities: List<Modality>): List<Step> {
        val steps = arrayListOf<Step>()

        if (modalities.contains(FINGER)) {
            steps.add(mockFingerprintCaptureStep())
            steps.add(mockFingerprintMatchStep())
        }

        if (modalities.contains(FACE)) {
            steps.add(mockFaceCaptureStep())
            steps.add(mockFaceMatchStep())
        }

        return steps
    }
}
