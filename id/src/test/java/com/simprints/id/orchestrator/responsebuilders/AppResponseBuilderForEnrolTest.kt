package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.model.IFingerprintImpl
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
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
            steps.add(mockFingerprintStep())

        if (modalities.contains(Modality.FACE))
            steps.add(mockFaceStep())

        return steps
    }

    private fun mockFingerprintStep(): Step {
        val request = FingerprintCaptureRequest(
            "projectId",
            "userId",
            "moduleId",
            "metadata",
            "language",
            mapOf(),
            true,
            "programmeName",
            "organisationName",
            "activityTitle"
        )

        return Step(
            requestCode = 123,
            activityName = "com.simprints.id.MyFingerprintActivity",
            bundleKey = "BUNDLE_KEY",
            request = request,
            result = FingerprintCaptureResponse(
                listOf(
                    IFingerprintImpl(
                        fingerId = IFingerIdentifier.LEFT_THUMB,
                        template = "template".toByteArray(),
                        qualityScore = 10
                    )
                )
            ),
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

}
