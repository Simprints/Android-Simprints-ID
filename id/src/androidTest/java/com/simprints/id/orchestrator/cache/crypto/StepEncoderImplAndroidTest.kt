package com.simprints.id.orchestrator.cache.crypto

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.orchestrator.steps.Step
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.koin.core.context.stopKoin
import java.util.*

class StepEncoderImplAndroidTest {

    private val stepEncoder by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val keystoreManager = KeystoreManagerImpl(context)
        StepEncoderImpl(keystoreManager)
    }

    @Test
    fun shouldEncodeFingerprintEnrolStepToString() {
        val fingerprintEnrolRequest = mockFingerprintEnrolRequest()
        val fingerprintEnrolResult = mockFingerprintEnrolResponse()
        val step = buildStep(fingerprintEnrolRequest, fingerprintEnrolResult)
        val encodedString = stepEncoder.encode(step)

        assertThat(encodedString, notNullValue())
        assertThat(encodedString, not(equalTo("")))
    }

    @Test
    fun shouldEncodeFaceCaptureStepToString() {
        val faceCaptureRequest = mockFaceCaptureRequest()
        val faceCaptureResponse = mockFaceCaptureResponse()
        val step = buildStep(faceCaptureRequest, faceCaptureResponse)
        val encodedString = stepEncoder.encode(step)

        assertThat(encodedString, notNullValue())
        assertThat(encodedString, not(equalTo("")))
    }

    @Test
    fun shouldDecodeStringToFingerprintEnrolStep() {
        val fingerprintEnrolRequest = mockFingerprintEnrolRequest()
        val fingerprintEnrolResult = mockFingerprintEnrolResponse()
        val step = buildStep(fingerprintEnrolRequest, fingerprintEnrolResult)
        val encodedString = stepEncoder.encode(step)
        val decodedStep = stepEncoder.decode(encodedString)

        with(decodedStep) {
            assertThat(requestCode, `is`(REQUEST_CODE))
            assertThat(activityName, `is`(ACTIVITY_NAME))
            assertThat(bundleKey, `is`(BUNDLE_KEY))
            assertThat(request, `is`(fingerprintEnrolRequest))
            assertThat(getStatus(), `is`(Step.Status.COMPLETED))
            assertThat(result, `is`(fingerprintEnrolResult))
        }
    }

    @Test
    fun shouldDecodeStringToFaceCaptureStep() {
        val faceCaptureRequest = mockFaceCaptureRequest()
        val faceCaptureResponse = mockFaceCaptureResponse()
        val step = buildStep(faceCaptureRequest, faceCaptureResponse)
        val encodedString = stepEncoder.encode(step)
        val decodedStep = stepEncoder.decode(encodedString)

        with(decodedStep) {
            assertThat(requestCode, `is`(REQUEST_CODE))
            assertThat(activityName, `is`(ACTIVITY_NAME))
            assertThat(bundleKey, `is`(BUNDLE_KEY))
            assertThat(request, `is`(faceCaptureRequest))
            assertThat(getStatus(), `is`(Step.Status.COMPLETED))
            assertThat(result, instanceOf(FaceCaptureResponse::class.java))
            require(result is FaceCaptureResponse)
            validateFaceCaptureResponse(result as FaceCaptureResponse,
                faceCaptureResponse as FaceCaptureResponse)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun buildStep(request: Step.Request, result: Step.Result): Step = Step(
        requestCode = REQUEST_CODE,
        activityName = ACTIVITY_NAME,
        bundleKey = BUNDLE_KEY,
        request = request,
        result = result,
        status = Step.Status.COMPLETED
    )

    private fun mockFingerprintEnrolRequest(): Step.Request = FingerprintEnrolRequest(
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

    private fun mockFaceCaptureRequest(): Step.Request = FaceCaptureRequest(
        nFaceSamplesToCapture = 3
    )

    private fun mockFingerprintEnrolResponse(): Step.Result {
        return FingerprintEnrolResponse(UUID.randomUUID().toString())
    }

    private fun mockFaceCaptureResponse(): Step.Result {
        val template = "abcd1234".toByteArray()

        return FaceCaptureResponse(listOf(
            FaceCaptureResult(
                index = 1,
                result = FaceCaptureSample(
                    "face_id",
                    template,
                    SecuredImageRef("uri")
                )
            )
        ))
    }

    private fun validateFaceCaptureResponse(response: FaceCaptureResponse,
                                            expected: FaceCaptureResponse) {
        with(response) {
            capturingResult.forEachIndexed { index, item ->
                val expectedItem = expected.capturingResult[index]
                assertThat(item.index, `is`(expectedItem.index))

                item.result?.let { sample ->
                    val expectedSample = expectedItem.result
                    assertThat(sample.faceId, `is`(expectedSample?.faceId))
                    assertThat(sample.imageRef, `is`(expectedSample?.imageRef))
                    expectedSample?.template?.let { expectedTemplate ->
                        assertThat(sample.template.contentEquals(expectedTemplate), `is`(true))
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 123
        private const val ACTIVITY_NAME = "com.simprints.id.MyActivity"
        private const val BUNDLE_KEY = "BUNDLE_KEY"
    }

}
