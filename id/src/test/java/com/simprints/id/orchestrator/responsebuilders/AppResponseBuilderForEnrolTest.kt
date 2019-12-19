package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.common.syntax.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

@ExperimentalCoroutinesApi
class AppResponseBuilderForEnrolTest {

    private val responseBuilder = AppResponseBuilderForEnrol(
        enrolmentHelper = mock(),
        timeHelper = mock()
    )

    @Test
    fun withFingerprintOnlySteps_shouldBuildAppEnrolResponse() {
        runBlockingTest {
            val modalities = listOf(Modality.FINGER)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppEnrolResponse::class.java))
        }
    }

    @Test
    fun withFaceOnlySteps_shouldBuildAppEnrolResponse() {
        runBlockingTest {
            val modalities = listOf(Modality.FACE)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppEnrolResponse::class.java))
        }
    }

    @Test
    fun withFingerprintAndFaceSteps_shouldBuildAppEnrolResponse() {
        runBlockingTest {
            val modalities = listOf(Modality.FINGER, Modality.FACE)
            val steps = mockSteps(modalities)

            val response = responseBuilder.buildAppResponse(
                modalities, mockRequest(), steps, "sessionId"
            )

            assertThat(response, instanceOf(AppEnrolResponse::class.java))
        }
    }

    @Test
    fun withInvalidSteps_shouldThrowException() {
        runBlockingTest {
            val modalities = emptyList<Modality>()
            val steps = mockSteps(modalities)

            assertThrows<Throwable> {
                responseBuilder.buildAppResponse(
                    modalities, mockRequest(), steps, "sessionId"
                )
            }
        }
    }

    private fun mockRequest() = AppEnrolRequest(
        "projectId", "userId", "moduleId", "metadata"
    )

    private fun mockSteps(modalities: List<Modality>): List<Step> {
        val steps = arrayListOf<Step>()

        if (modalities.contains(Modality.FINGER))
            steps.add(mockFingerprintCaptureStep())

        if (modalities.contains(Modality.FACE))
            steps.add(mockFaceCaptureStep())

        return steps
    }
}
