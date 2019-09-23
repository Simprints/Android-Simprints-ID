package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.common.syntax.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
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
            assertThat((response as AppEnrolResponse).guid, `is`(EXPECTED_GUID_FINGERPRINT))
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
            assertThat((response as AppEnrolResponse).guid, `is`(EXPECTED_GUID_FINGERPRINT))
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
            steps.add(mockFingerprintStep())

        if (modalities.contains(Modality.FACE))
            steps.add(mockFaceStep())

        return steps
    }

    private fun mockFingerprintStep(): Step {
        val request = FingerprintEnrolRequest(
            "projectId",
            "userId",
            "moduleId",
            "metadata",
            "language",
            mapOf(),
            true,
            "programmeName",
            "organisationName"
        )

        return Step(
            requestCode = 123,
            activityName = "com.simprints.id.MyFingerprintActivity",
            bundleKey = "BUNDLE_KEY",
            request = request,
            result = FingerprintEnrolResponse(EXPECTED_GUID_FINGERPRINT),
            status = Step.Status.COMPLETED
        )
    }

    private fun mockFaceStep(): Step {
        val request = FaceCaptureRequest(nFaceSamplesToCapture = 2)
        val response = FaceCaptureResponse(
            listOf(
                FaceCaptureResult(
                    index = 0,
                    result = FaceCaptureSample("faceId", "faceId".toByteArray(), null)
                )
            )
        )

        return Step(
            requestCode = 321,
            activityName = "com.simprints.id.MyFaceActivity",
            bundleKey = "BUNDLE_KEY",
            request = request,
            result = response,
            status = Step.Status.COMPLETED
        )
    }

    private companion object {
        const val EXPECTED_GUID_FINGERPRINT = "expected-guid"
    }

}
