package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse
import com.simprints.id.orchestrator.steps.Step
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

@ExperimentalCoroutinesApi
class AppResponseBuilderForIdentifyTest {

    private val responseBuilder = AppResponseBuilderForIdentify()

    @Test
    fun withFingerprintOnlySteps_shouldBuildAppResponse() {
        runBlockingTest {
            val modalities = listOf(Modality.FINGER)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppIdentifyResponse::class.java))
        }
    }

    @Test
    fun withFaceOnlySteps_shouldBuildAppIdentifyResponse() {
        runBlockingTest {
            val modalities = listOf(Modality.FACE)
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


    private fun mockSteps(modalities: List<Modality>): List<Step> {
        val steps = arrayListOf<Step>()

        if (modalities.contains(Modality.FINGER)) {
            steps.add(mockFingerprintCaptureStep())
            steps.add(mockFingerprintMatchStep())
        }

        if (modalities.contains(Modality.FACE)) {
            steps.add(mockFaceCaptureStep())
            steps.add(mockFaceMatchStep())
        }

        return steps
    }
}
